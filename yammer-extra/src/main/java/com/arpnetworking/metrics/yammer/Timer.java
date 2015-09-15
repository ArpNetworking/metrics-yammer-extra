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
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.TimerIntercept;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Timer that is wrapped to output ArpNetworking metrics.
 *
 * @author Brandon Arp (barp at groupon dot com)
 */
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class Timer extends TimerIntercept {
    /**
     * Public constructor.
     *
     * @param name name of the metric
     * @param lock lock for the metrics reference
     * @param tickThread background thread for updating the rates
     * @param durationUnit the scale unit for this timer's duration metrics
     * @param rateUnit the scale unit for this timer's rate metrics
     * @param clock the clock used to calculate duration
     */
    public Timer(
            final String name,
            final SafeRefLock<Metrics> lock,
            final ScheduledExecutorService tickThread,
            final TimeUnit durationUnit,
            final TimeUnit rateUnit,
            final Clock clock) {
        super(tickThread, durationUnit, rateUnit, clock);
        _name = name;
        _lock = lock;
        _clock = clock;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final long duration, final TimeUnit unit) {
        _lock.readLocked(metrics -> metrics.setTimer(_name, duration, unit));
        super.update(duration, unit);
    }

    @Override
    public TimerContext time() {
        return new TimerContext(this, _clock);
    }

    private final String _name;
    private final SafeRefLock<Metrics> _lock;
    private final Clock _clock;
}
