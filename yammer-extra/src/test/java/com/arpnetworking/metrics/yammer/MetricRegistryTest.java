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
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Tests for the MetricRegistry.
 *
 * @author Brandon Arp (barp at groupon dot com)
 */
public class MetricRegistryTest {
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void clock() {
        final MetricsRegistry metricRegistry = new MetricsRegistry(Clock.defaultClock());
        Assert.assertNotNull(metricRegistry);
    }

    @Test
    public void counter() {
        final MetricsRegistry metricRegistry = new MetricsRegistry();
        final com.yammer.metrics.core.Counter counter = metricRegistry.newCounter(name("foo"));
        Assert.assertThat(counter, CoreMatchers.instanceOf(com.arpnetworking.metrics.yammer.Counter.class));
    }

    @Test
    public void timer() {
        final MetricsRegistry metricRegistry = new MetricsRegistry();
        final com.yammer.metrics.core.Timer timer = metricRegistry.newTimer(name("foo"), TimeUnit.MINUTES, TimeUnit.MINUTES);
        Assert.assertThat(timer, CoreMatchers.instanceOf(Timer.class));
    }

    @Test
    public void meter() {
        final MetricsRegistry metricRegistry = new MetricsRegistry();
        final com.yammer.metrics.core.Meter meter = metricRegistry.newMeter(name("foo"), "somethings", TimeUnit.MINUTES);
        Assert.assertThat(meter, CoreMatchers.instanceOf(Meter.class));
    }

    @Test
    public void histogram() {
        final MetricsRegistry metricRegistry = new MetricsRegistry();
        final com.yammer.metrics.core.Histogram histogram = metricRegistry.newHistogram(name("foo"), false);
        Assert.assertThat(histogram, CoreMatchers.instanceOf(Histogram.class));
    }

    @Test
    public void closer() {
        final MetricsRegistry registry = new MetricsRegistry();
        @SuppressWarnings("unchecked")
        final Gauge<Integer> gauge = (Gauge<Integer>) (Gauge<?>) Mockito.mock(Gauge.class);
        registry.newGauge(Gauge.class, "some_gauge", gauge);
        Mockito.when(gauge.value()).thenReturn(15);
        final MetricsRegistry.Closer closer = new MetricsRegistry.Closer(_lock, _factory, _reference, registry);
        final Metrics original = Mockito.mock(Metrics.class);
        final Metrics after = Mockito.mock(Metrics.class);
        _reference.set(original);

        Mockito.when(_factory.create()).thenReturn(after);

        closer.run();
        Mockito.verifyZeroInteractions(_factory);
        Mockito.verify(_lock).writeLocked(_callback.capture());
        Assert.assertSame(original, _reference.get());
        _callback.getValue().accept(original);

        Mockito.verify(original).setGauge("some_gauge", 15d);
        Mockito.verify(original).close();
        Assert.assertSame(after, _reference.get());
        Mockito.verifyZeroInteractions(after);
    }

    @Test
    public void closerException() {
        final MetricsRegistry registry = new MetricsRegistry();
        @SuppressWarnings("unchecked")
        final Gauge<Integer> gauge = (Gauge<Integer>) (Gauge<?>) Mockito.mock(Gauge.class);
        registry.newGauge(Gauge.class, "some_gauge", gauge);
        Mockito.when(gauge.value()).thenThrow(new IllegalStateException());
        final MetricsRegistry.Closer closer = new MetricsRegistry.Closer(_lock, _factory, _reference, registry);
        final Metrics original = Mockito.mock(Metrics.class);
        final Metrics after = Mockito.mock(Metrics.class);
        _reference.set(original);

        Mockito.when(_factory.create()).thenReturn(after);

        closer.run();
        Mockito.verifyZeroInteractions(_factory);
        Mockito.verify(_lock).writeLocked(_callback.capture());
        Assert.assertSame(original, _reference.get());
        _callback.getValue().accept(original);

        Mockito.verify(original).close();
        Mockito.verifyNoMoreInteractions(original);
        Assert.assertSame(after, _reference.get());
        Mockito.verifyZeroInteractions(after);
    }

    @Test
    public void closerNonNumericGauge() {
        final MetricsRegistry registry = new MetricsRegistry();
        @SuppressWarnings("unchecked")
        final Gauge<String> gauge = (Gauge<String>) (Gauge<?>) Mockito.mock(Gauge.class);
        registry.newGauge(Gauge.class, "some_gauge", gauge);
        Mockito.when(gauge.value()).thenReturn("foo");
        final MetricsRegistry.Closer closer = new MetricsRegistry.Closer(_lock, _factory, _reference, registry);
        final Metrics original = Mockito.mock(Metrics.class);
        final Metrics after = Mockito.mock(Metrics.class);
        _reference.set(original);

        Mockito.when(_factory.create()).thenReturn(after);

        closer.run();
        Mockito.verifyZeroInteractions(_factory);
        Mockito.verify(_lock).writeLocked(_callback.capture());
        Assert.assertSame(original, _reference.get());
        _callback.getValue().accept(original);

        Mockito.verify(original).close();
        Mockito.verifyNoMoreInteractions(original);
        Assert.assertSame(after, _reference.get());
        Mockito.verifyZeroInteractions(after);
    }

    private MetricName name(final String name) {
        return new MetricName(Metric.class, name);
    }

    @Mock
    private SafeRefLock<Metrics> _lock;
    @Mock
    private MetricsFactory _factory;
    private AtomicReference<Metrics> _reference = new AtomicReference<>();
    @Captor
    private ArgumentCaptor<Consumer<Metrics>> _callback;
}
