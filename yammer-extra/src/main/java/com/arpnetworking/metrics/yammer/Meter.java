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
import com.yammer.metrics.core.MeterIntercept;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Meter that is wrapped to output ArpNetworking Metrics.  Each call to mark will result in a counter sample being created.
 *
 * @author Brandon Arp (barp at groupon dot com)
 */
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class Meter extends MeterIntercept {
    /**
     * Public constructor.
     *
     * @param name name of the metric
     * @param lock lock for the metrics reference
     * @param tickThread background thread for updating the rates
     * @param eventType the plural name of the event the meter is measuring (e.g., {@code "requests"})
     * @param rateUnit the rate unit of the new meter
     * @param clock the clock to use for the meter ticks
     */
    public Meter(
            final String name,
            final SafeRefLock<Metrics> lock,
            final ScheduledExecutorService tickThread,
            final String eventType,
            final TimeUnit rateUnit,
            final Clock clock) {
        super(tickThread, eventType, rateUnit, clock);
        _name = name;
        _lock = lock;
    }

    @Override
    public void mark(final long n) {
        super.mark(n);
        _lock.readLocked(
                (metrics) -> {
                    metrics.resetCounter(_name);
                    metrics.incrementCounter(_name, n);
                });
    }


    private final String _name;
    private final SafeRefLock<Metrics> _lock;
}
