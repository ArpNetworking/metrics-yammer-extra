/**
 * Copyright 2015 Groupon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arpnetworking.metrics.yammer;

import com.arpnetworking.metrics.Metrics;
import com.arpnetworking.metrics.MetricsFactory;
import com.arpnetworking.metrics.impl.TsdLogSink;
import com.arpnetworking.metrics.impl.TsdMetricsFactory;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * The replacement class for the MetricsRegistry.  In the case of using the shaded library
 * which replaces your Yammer dependency this class will serve as the provided MetricsRegistry.
 * In the case of using the supplementary library, you should use this class instead of the
 * Yammer-provided MetricsRegistry in your code.
 *
 * @author Brandon Arp (barp at groupon dot com)
 */
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class MetricsRegistry extends com.yammer.metrics.core.MetricsRegistry {

    /**
     * Public constructor.
     */
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    public MetricsRegistry() {
        // TODO(barp): Read the settings from a config file [#2]
        this(
                new TsdMetricsFactory.Builder()
                        .setClusterName(System.getProperty("METRICS_YAMMER_EXTRA_CLUSTER", "YammerCluster"))
                        .setServiceName(System.getProperty("METRICS_YAMMER_EXTRA_SERVICE", "YammerService"))
                        .setSinks(Collections.singletonList(
                                new TsdLogSink.Builder()
                                        .setDirectory(
                                                new File(System.getProperty("METRICS_YAMMER_EXTRA_DIRECTORY", "/tmp")))
                                        .build()))
                        .build(),
                Clock.defaultClock());
    }

    /**
     * Public constructor.
     *
     * @param metricsFactory The metrics factory to use to create metrics.
     * @param clock    a {@link Clock} instance
     */
    public MetricsRegistry(final MetricsFactory metricsFactory, final Clock clock) {
        super(clock);
        _openMetrics.set(metricsFactory.create());
        _clock = clock;
        _closingExecutor = Executors.newSingleThreadScheduledExecutor(
                (r) -> {
                    final Thread thread = new Thread(r, "metrics-closer");
                    thread.setDaemon(true);
                    return thread;
                });
        _closingExecutor.scheduleAtFixedRate(
                new Closer(_lock, metricsFactory, _openMetrics, this),
                CLOSER_PERIOD_MILLIS,
                CLOSER_PERIOD_MILLIS,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Public constructor.
     *
     * @param clock    a {@link Clock} instance
     */
    public MetricsRegistry(final Clock clock) {
        // TODO(barp): Read the settings from a config file [#2]
        this(
                new TsdMetricsFactory.Builder()
                        .setSinks(Collections.singletonList(new TsdLogSink.Builder().build()))
                        .build(),
                clock);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Counter newCounter(final MetricName metricName) {
        final Counter counter = getOrCreate(metricName.getName(), _counterBuilder);
        getOrAdd(metricName, counter);
        return counter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Histogram newHistogram(final MetricName metricName, final boolean biased) {
        final Histogram histogram = getOrCreate(metricName.getName(), (n) -> new Histogram(n, _lock, biased));
        getOrAdd(metricName, histogram);
        return histogram;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timer newTimer(
            final MetricName metricName,
            final TimeUnit durationUnit,
            final TimeUnit rateUnit) {
        final Timer timer = getOrCreate(metricName.getName(), (n) -> new Timer(n, _lock, tickPool(), durationUnit, rateUnit, _clock));
        getOrAdd(metricName, timer);
        return timer;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Meter newMeter(final MetricName metricName, final String eventType, final TimeUnit unit) {
        final Meter meter = getOrCreate(metricName.getName(), (n) -> new Meter(n, _lock, tickPool(), eventType, unit, _clock));
        getOrAdd(metricName, meter);
        return meter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Gauge<T> newGauge(final MetricName metricName, final Gauge<T> metric) {
        _gauges.put(metricName.getName(), metric);
        getOrAdd(metricName, metric);
        return metric;
    }

    /**
     * Writes the gauges to the metric instance.
     * NOTE: This is public to allow use from the Closer when shaded.
     *
     * @param metrics the metrics instance to write the gauges to
     */
    public void writeGauges(final Metrics metrics) {
        try {
            for (final Map.Entry<String, Gauge<?>> entry : _gauges.entrySet()) {
                final Object value = entry.getValue().value();
                if (value instanceof Number) {
                    metrics.setGauge(entry.getKey(), ((Number) value).doubleValue());
                }
            }
            //CHECKSTYLE.OFF: IllegalCatch - we need to catch everything
        } catch (final Exception ex) {
            //CHECKSTYLE.ON: IllegalCatch
            System.err.println(ex);
            ex.printStackTrace();

        }
    }

    private ScheduledExecutorService tickPool() {
        return newScheduledThreadPool(2, "meter-tick");
    }

    private <T extends Metric> T getOrCreate(final String name, final Function<String, T> builder) {
        @SuppressWarnings("unchecked")
        final T metric = (T) _metrics.computeIfAbsent(name, builder);
        return metric;
    }

    private final ScheduledExecutorService _closingExecutor;
    private final ConcurrentMap<String, Metric> _metrics = new ConcurrentHashMap<>();
    private final AtomicReference<Metrics> _openMetrics = new AtomicReference<>();
    private final SafeRefLock<Metrics> _lock = new SafeRefLock<>(_openMetrics, new ReentrantReadWriteLock(false));
    private final ConcurrentMap<String, Gauge<?>> _gauges = new ConcurrentHashMap<>();
    private final Clock _clock;

    // These are Functions instead of Suppliers so that we don't have to close over them in the getOrCreate function
    private final Function<String, Counter> _counterBuilder = (n) -> new Counter(n, _lock);

    private static final int CLOSER_PERIOD_MILLIS = 500;

    /**
     * Closes a metric instance in a MetricsRegistry.  Public to allow cross-package use after shading.
     *
     * @author Brandon Arp (barp at groupon dot com)
     */
    public static class Closer implements Runnable {
        /**
         * Public constructor.
         *
         * @param lock lock to acquire
         * @param factory the metrics factory
         * @param metricsRef atomic reference to the open metrics instance
         * @param registry the registry to operate on
         */
        public Closer(
                final SafeRefLock<Metrics> lock,
                final MetricsFactory factory,
                final AtomicReference<Metrics> metricsRef,
                final MetricsRegistry registry) {
            _lock = lock;
            _factory = factory;
            _metricsRef = metricsRef;
            _registry = registry;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            final Metrics metrics = _metricsRef.get();
            _lock.writeLocked(ignored -> _metricsRef.set(_factory.create()));
            _registry.writeGauges(metrics);
            metrics.close();
        }

        private final SafeRefLock<Metrics> _lock;
        private final MetricsFactory _factory;
        private final AtomicReference<Metrics> _metricsRef;
        private final MetricsRegistry _registry;
    }
}
