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
import com.arpnetworking.metrics.Unit;
import com.arpnetworking.metrics.Units;
import com.yammer.metrics.core.Clock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Tests for the Timer class.
 *
 * @author Brandon Arp (barp at groupon dot com)
 */
public class TimerTest {
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void setTimer() {
        final Timer timer = new Timer(
                "foo",
                _lock,
                Executors.newSingleThreadScheduledExecutor(),
                TimeUnit.MILLISECONDS,
                TimeUnit.MINUTES,
                Clock.defaultClock());
        timer.update(18, TimeUnit.MILLISECONDS);
        Mockito.verifyZeroInteractions(_metrics);
        Mockito.verify(_lock).readLocked(_delegateCaptor.capture());
        _delegateCaptor.getValue().accept(_metrics);
        Mockito.verify(_metrics).setTimer("foo", 18, Units.MILLISECOND);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void time() {
        final Timer timer = new Timer(
                "foo",
                _lock,
                Executors.newSingleThreadScheduledExecutor(),
                TimeUnit.MILLISECONDS,
                TimeUnit.MINUTES,
                Clock.defaultClock());
        timer.time().stop();
        Mockito.verifyZeroInteractions(_metrics);
        Mockito.verify(_lock).readLocked(_delegateCaptor.capture());
        _delegateCaptor.getValue().accept(_metrics);
        Mockito.verify(_metrics).setTimer(Mockito.eq("foo"), Mockito.anyLong(), Mockito.any(Unit.class));
    }

    @Mock
    private SafeRefLock<Metrics> _lock;
    @Captor
    private ArgumentCaptor<Consumer<Metrics>> _delegateCaptor;
    @Mock
    private Metrics _metrics;
}
