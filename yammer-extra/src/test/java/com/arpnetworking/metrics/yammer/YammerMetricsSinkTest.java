/**
 * Copyright 2014 Groupon.com
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

import com.arpnetworking.metrics.Quantity;
import com.arpnetworking.metrics.Sink;
import com.arpnetworking.metrics.Units;
import com.arpnetworking.metrics.impl.TsdEvent;
import com.arpnetworking.metrics.yammer.test.TestQuantity;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for <code>YammerMetricsSink</code>.
 *
 * @author Ville Koskela (vkoskela at groupon dot com)
 */
public final class YammerMetricsSinkTest {

    @Test
    public void testBuilderDefaultMetricsRegistry() {
        final YammerMetricsSink sink = new YammerMetricsSink.Builder()
                .build();
        Assert.assertSame(Metrics.defaultRegistry(), sink.getMetricsRegistry());
    }

    @Test
    public void testBuilderNullToDefaultMetricsRegistry() {
        final YammerMetricsSink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(null)
                .build();
        Assert.assertSame(Metrics.defaultRegistry(), sink.getMetricsRegistry());
    }

    @Test
    public void testBuilder() {
        final MetricsRegistry registry = Mockito.mock(MetricsRegistry.class);
        final YammerMetricsSink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(registry)
                .build();
        Assert.assertSame(registry, sink.getMetricsRegistry());
    }

    @Test
    public void testEmptyMetrics() {
        final MetricsRegistry registry = Mockito.mock(MetricsRegistry.class);
        final Sink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(registry)
                .build();

        sink.record(
                new TsdEvent.Builder()
                        .setAnnotations(Collections.<String, String>emptyMap())
                        .setTimerSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setGaugeSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setCounterSamples(Collections.<String, List<Quantity>>emptyMap())
                        .build());

        Mockito.verifyZeroInteractions(registry);
    }

    @Test
    public void testTimerSingleSample() {
        final String metricName = "timerA";
        final MetricsRegistry registry = Mockito.mock(MetricsRegistry.class);
        final Sink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(registry)
                .build();

        final Timer timer = Mockito.mock(Timer.class);
        Mockito.when(registry.newTimer(YammerMetricsSink.class, metricName)).thenReturn(timer);

        final List<Quantity> samples = new ArrayList<>();
        samples.add(TestQuantity.newInstance(1L, Units.SECOND));
        // CHECKSTYLE.OFF: IllegalInstantiation - No Gauva
        final Map<String, List<Quantity>> metrics = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        metrics.put(metricName, samples);

        sink.record(
                new TsdEvent.Builder()
                        .setAnnotations(Collections.<String, String>emptyMap())
                        .setTimerSamples(metrics)
                        .setGaugeSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setCounterSamples(Collections.<String, List<Quantity>>emptyMap())
                        .build());

        Mockito.verify(registry).newTimer(YammerMetricsSink.class, metricName);
        Mockito.verifyNoMoreInteractions(registry);

        Mockito.verify(timer).update(1, TimeUnit.SECONDS);
        Mockito.verifyNoMoreInteractions(timer);
    }

    @Test
    public void testTimerMultiSample() {
        final String metricName = "timerA";
        final MetricsRegistry registry = Mockito.mock(MetricsRegistry.class);
        final Sink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(registry)
                .build();

        final Timer timer = Mockito.mock(Timer.class);
        Mockito.when(registry.newTimer(YammerMetricsSink.class, metricName)).thenReturn(timer);

        final List<Quantity> samples = new ArrayList<>();
        samples.add(TestQuantity.newInstance(1L, Units.SECOND));
        samples.add(TestQuantity.newInstance(2L, Units.MILLISECOND));
        // CHECKSTYLE.OFF: IllegalInstantiation - No Gauva
        final Map<String, List<Quantity>> metrics = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        metrics.put(metricName, samples);

        sink.record(
                new TsdEvent.Builder()
                        .setAnnotations(Collections.<String, String>emptyMap())
                        .setTimerSamples(metrics)
                        .setGaugeSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setCounterSamples(Collections.<String, List<Quantity>>emptyMap())
                        .build());

        Mockito.verify(registry).newTimer(YammerMetricsSink.class, metricName);
        Mockito.verifyNoMoreInteractions(registry);

        Mockito.verify(timer).update(1, TimeUnit.SECONDS);
        Mockito.verify(timer).update(2, TimeUnit.MILLISECONDS);
        Mockito.verifyNoMoreInteractions(timer);
    }

    @Test
    public void testTimerNullUnit() {
        final String metricName = "timerA";
        final MetricsRegistry registry = Mockito.mock(MetricsRegistry.class);
        final Sink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(registry)
                .build();

        final Timer timer = Mockito.mock(Timer.class);
        Mockito.when(registry.newTimer(YammerMetricsSink.class, metricName)).thenReturn(timer);

        final List<Quantity> samples = new ArrayList<>();
        samples.add(TestQuantity.newInstance(1L, null));
        // CHECKSTYLE.OFF: IllegalInstantiation - No Gauva
        final Map<String, List<Quantity>> metrics = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        metrics.put(metricName, samples);

        sink.record(
                new TsdEvent.Builder()
                        .setAnnotations(Collections.<String, String>emptyMap())
                        .setTimerSamples(metrics)
                        .setGaugeSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setCounterSamples(Collections.<String, List<Quantity>>emptyMap())
                        .build());

        Mockito.verify(registry).newTimer(YammerMetricsSink.class, metricName);
        Mockito.verifyNoMoreInteractions(registry);

        Mockito.verify(timer).update(1, TimeUnit.MILLISECONDS);
        Mockito.verifyNoMoreInteractions(timer);
    }

    @Test
    public void testCounterSingleSample() {
        final String metricName = "counterA";
        final MetricsRegistry registry = Mockito.mock(MetricsRegistry.class);
        final Sink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(registry)
                .build();

        final Counter counter = Mockito.mock(Counter.class);
        Mockito.when(registry.newCounter(YammerMetricsSink.class, metricName)).thenReturn(counter);

        final List<Quantity> samples = new ArrayList<>();
        samples.add(TestQuantity.newInstance(10L, null));
        // CHECKSTYLE.OFF: IllegalInstantiation - No Gauva
        final Map<String, List<Quantity>> metrics = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        metrics.put(metricName, samples);

        sink.record(
                new TsdEvent.Builder()
                        .setAnnotations(Collections.<String, String>emptyMap())
                        .setTimerSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setGaugeSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setCounterSamples(metrics)
                        .build());

        Mockito.verify(registry).newCounter(YammerMetricsSink.class, metricName);
        Mockito.verifyNoMoreInteractions(registry);

        Mockito.verify(counter).inc(10);
        Mockito.verifyNoMoreInteractions(counter);
    }

    @Test
    public void testCounterMultiSample() {
        final String metricName = "counterA";
        final MetricsRegistry registry = Mockito.mock(MetricsRegistry.class);
        final Sink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(registry)
                .build();

        final Counter counter = Mockito.mock(Counter.class);
        Mockito.when(registry.newCounter(YammerMetricsSink.class, metricName)).thenReturn(counter);

        final List<Quantity> samples = new ArrayList<>();
        samples.add(TestQuantity.newInstance(10L, null));
        samples.add(TestQuantity.newInstance(11L, null));
        // CHECKSTYLE.OFF: IllegalInstantiation - No Gauva
        final Map<String, List<Quantity>> metrics = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        metrics.put(metricName, samples);

        sink.record(
                new TsdEvent.Builder()
                        .setAnnotations(Collections.<String, String>emptyMap())
                        .setTimerSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setGaugeSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setCounterSamples(metrics)
                        .build());

        Mockito.verify(registry).newCounter(YammerMetricsSink.class, metricName);
        Mockito.verifyNoMoreInteractions(registry);

        Mockito.verify(counter).inc(10);
        Mockito.verify(counter).inc(11);
        Mockito.verifyNoMoreInteractions(counter);
    }

    @Test
    public void testGaugeNoSample() {
        final String metricName = "gaugeA";
        final MetricsRegistry registry = Mockito.mock(MetricsRegistry.class);
        final Sink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(registry)
                .build();

        // CHECKSTYLE.OFF: IllegalInstantiation - No Gauva
        final Map<String, List<Quantity>> metrics = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        metrics.put(metricName, Collections.<Quantity>emptyList());

        sink.record(
                new TsdEvent.Builder()
                        .setAnnotations(Collections.<String, String>emptyMap())
                        .setTimerSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setGaugeSamples(metrics)
                        .setCounterSamples(Collections.<String, List<Quantity>>emptyMap())
                        .build());

        Mockito.verifyNoMoreInteractions(registry);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGaugeSingleSample() {
        final String metricName = "gaugeA";
        final MetricsRegistry registry = Mockito.mock(MetricsRegistry.class);
        final Sink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(registry)
                .build();

        final AtomicReference<Gauge<Double>> gauge = new AtomicReference<>();
        Mockito.when(
                registry.newGauge(
                        Matchers.eq(YammerMetricsSink.class),
                        Matchers.eq(metricName),
                        Matchers.any(Gauge.class)))
                .then(new GaugeCaptureAnswer(gauge));

        final List<Quantity> samples = new ArrayList<>();
        samples.add(TestQuantity.newInstance(3.84, null));
        // CHECKSTYLE.OFF: IllegalInstantiation - No Gauva
        final Map<String, List<Quantity>> metrics = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        metrics.put(metricName, samples);

        sink.record(
                new TsdEvent.Builder()
                        .setAnnotations(Collections.<String, String>emptyMap())
                        .setTimerSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setGaugeSamples(metrics)
                        .setCounterSamples(Collections.<String, List<Quantity>>emptyMap())
                        .build());

        Mockito.verify(registry).newGauge(
                Matchers.eq(YammerMetricsSink.class),
                Matchers.eq(metricName),
                Matchers.any(Gauge.class));
        Mockito.verifyNoMoreInteractions(registry);

        Assert.assertEquals(3.84, gauge.get().value(), 0.0001);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGaugeMultiSample() {
        final String metricName = "gaugeA";
        final MetricsRegistry registry = Mockito.mock(MetricsRegistry.class);
        final Sink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(registry)
                .build();

        final AtomicReference<Gauge<Double>> gauge = new AtomicReference<>();
        Mockito.when(
                registry.newGauge(
                        Matchers.eq(YammerMetricsSink.class),
                        Matchers.eq(metricName),
                        Matchers.any(Gauge.class)))
                .then(new GaugeCaptureAnswer(gauge));

        final List<Quantity> samples = new ArrayList<>();
        samples.add(TestQuantity.newInstance(3.84, null));
        samples.add(TestQuantity.newInstance(2.07, null));
        // CHECKSTYLE.OFF: IllegalInstantiation - No Gauva
        final Map<String, List<Quantity>> metrics = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        metrics.put(metricName, samples);

        sink.record(
                new TsdEvent.Builder()
                        .setAnnotations(Collections.<String, String>emptyMap())
                        .setTimerSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setGaugeSamples(metrics)
                        .setCounterSamples(Collections.<String, List<Quantity>>emptyMap())
                        .build());

        Mockito.verify(registry).newGauge(
                Matchers.eq(YammerMetricsSink.class),
                Matchers.eq(metricName),
                Matchers.any(Gauge.class));
        Mockito.verifyNoMoreInteractions(registry);

        Assert.assertEquals(2.07, gauge.get().value(), 0.0001);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGaugeMultiRecord() {
        final String metricName = "gaugeA";
        final MetricsRegistry registry = Mockito.mock(MetricsRegistry.class);
        final Sink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(registry)
                .build();

        final AtomicReference<Gauge<Double>> gauge = new AtomicReference<>();
        Mockito.when(
                registry.newGauge(
                        Matchers.eq(YammerMetricsSink.class),
                        Matchers.eq(metricName),
                        Matchers.any(Gauge.class)))
                .then(new GaugeCaptureAnswer(gauge));

        final List<Quantity> samplesA = new ArrayList<>();
        samplesA.add(TestQuantity.newInstance(3.84, null));
        final List<Quantity> samplesB = new ArrayList<>();
        samplesB.add(TestQuantity.newInstance(2.07, null));
        // CHECKSTYLE.OFF: IllegalInstantiation - No Gauva
        final Map<String, List<Quantity>> metricsA = new HashMap<>();
        final Map<String, List<Quantity>> metricsB = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        metricsA.put(metricName, samplesA);
        metricsB.put(metricName, samplesB);

        sink.record(
                new TsdEvent.Builder()
                        .setAnnotations(Collections.<String, String>emptyMap())
                        .setTimerSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setGaugeSamples(metricsA)
                        .setCounterSamples(Collections.<String, List<Quantity>>emptyMap())
                        .build());

        Mockito.verify(registry).newGauge(
                Matchers.eq(YammerMetricsSink.class),
                Matchers.eq(metricName),
                Matchers.any(Gauge.class));

        Assert.assertEquals(3.84, gauge.get().value(), 0.0001);
        Mockito.reset(registry);

        Mockito.when(
                registry.newGauge(
                        Matchers.eq(YammerMetricsSink.class),
                        Matchers.eq(metricName),
                        Matchers.any(Gauge.class)))
                .then(new ReturnGaugeAnswer(gauge));

        sink.record(
                new TsdEvent.Builder()
                        .setAnnotations(Collections.<String, String>emptyMap())
                        .setTimerSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setGaugeSamples(metricsB)
                        .setCounterSamples(Collections.<String, List<Quantity>>emptyMap())
                        .build());

        Mockito.verify(registry).newGauge(
                Matchers.eq(YammerMetricsSink.class),
                Matchers.eq(metricName),
                Matchers.any(Gauge.class));

        Mockito.verifyNoMoreInteractions(registry);

        Assert.assertEquals(2.07, gauge.get().value(), 0.0001);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDifferentGaugeType() {
        final String metricName = "gaugeA";
        final MetricsRegistry registry = Mockito.mock(MetricsRegistry.class);
        final Sink sink = new YammerMetricsSink.Builder()
                .setMetricsRegistry(registry)
                .build();

        Mockito.when(
                registry.newGauge(
                        Matchers.eq(YammerMetricsSink.class),
                        Matchers.eq(metricName),
                        Matchers.any(Gauge.class)))
                .then(new DifferentGaugeAnswer());

        final List<Quantity> samples = new ArrayList<>();
        samples.add(TestQuantity.newInstance(3.84, null));
        // CHECKSTYLE.OFF: IllegalInstantiation - No Gauva
        final Map<String, List<Quantity>> metrics = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        metrics.put(metricName, samples);

        sink.record(
                new TsdEvent.Builder()
                        .setAnnotations(Collections.<String, String>emptyMap())
                        .setTimerSamples(Collections.<String, List<Quantity>>emptyMap())
                        .setGaugeSamples(metrics)
                        .setCounterSamples(Collections.<String, List<Quantity>>emptyMap())
                        .build());

        Mockito.verify(registry).newGauge(
                Matchers.eq(YammerMetricsSink.class),
                Matchers.eq(metricName),
                Matchers.any(Gauge.class));
        Mockito.verifyNoMoreInteractions(registry);

        // NOTE: This is not a great test since it is testing for the fact that
        // no action was taken. If action were taken it would result in an
        // exception.
    }

    @Test
    public void testToTimeUnit() {
        Assert.assertNull(YammerMetricsSink.toTimeUnit(null));
        Assert.assertEquals(TimeUnit.DAYS, YammerMetricsSink.toTimeUnit(Units.DAY));
        Assert.assertEquals(TimeUnit.HOURS, YammerMetricsSink.toTimeUnit(Units.HOUR));
        Assert.assertEquals(TimeUnit.MICROSECONDS, YammerMetricsSink.toTimeUnit(Units.MICROSECOND));
        Assert.assertEquals(TimeUnit.MILLISECONDS, YammerMetricsSink.toTimeUnit(Units.MILLISECOND));
        Assert.assertEquals(TimeUnit.MINUTES, YammerMetricsSink.toTimeUnit(Units.MINUTE));
        Assert.assertEquals(TimeUnit.NANOSECONDS, YammerMetricsSink.toTimeUnit(Units.NANOSECOND));
        Assert.assertEquals(TimeUnit.SECONDS, YammerMetricsSink.toTimeUnit(Units.SECOND));
    }

    private static final class DifferentGauge extends Gauge<Double> {
        @Override
        public Double value() {
            return 99.9;
        }
    }

    private static final class ReturnGaugeAnswer implements Answer<Gauge<Double>> {

        private ReturnGaugeAnswer(final AtomicReference<Gauge<Double>> gauge) {
            _gauge = gauge;
        }

        @Override
        // CHECKSTYLE.OFF: IllegalThrows - Declared in interface.
        public Gauge<Double> answer(final InvocationOnMock invocation) throws Throwable {
            // CHECKSTYLE.ON: IllegalThrows
            return _gauge.get();
        }

        private final AtomicReference<Gauge<Double>> _gauge;
    }

    private static final class DifferentGaugeAnswer implements Answer<Gauge<Double>> {
        @Override
        // CHECKSTYLE.OFF: IllegalThrows - Declared in interface.
        public Gauge<Double> answer(final InvocationOnMock invocation) throws Throwable {
            // CHECKSTYLE.ON: IllegalThrows
            return new DifferentGauge();
        }
    }

    private static final class GaugeCaptureAnswer implements Answer<Gauge<Double>> {

        private GaugeCaptureAnswer(final AtomicReference<Gauge<Double>> gauge) {
            _gauge = gauge;
        }

        @SuppressWarnings("unchecked")
        @Override
        // CHECKSTYLE.OFF: IllegalThrows - Declared in interface.
        public Gauge<Double> answer(final InvocationOnMock invocation) throws Throwable {
            // CHECKSTYLE.ON: IllegalThrows
            _gauge.set((Gauge<Double>) invocation.getArguments()[2]);
            return _gauge.get();
        }

        private final AtomicReference<Gauge<Double>> _gauge;
    }
}
