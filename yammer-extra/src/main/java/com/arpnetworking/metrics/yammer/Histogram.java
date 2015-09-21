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
import com.yammer.metrics.core.HistogramIntercept;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents a Histogram that is wrapped to output ArpNetworking metrics. Each update will result in a counter sample being created.
 *
 * @author Brandon Arp (barp at groupon dot com)
 */
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class Histogram extends HistogramIntercept {
    /**
     * Creates a new {@link Histogram} with the given reservoir.
     *
     * @param name name of the metric
     * @param lock lock for the metrics reference
     * @param biased create a biased sampling
     */
    public Histogram(final String name, final SafeRefLock<Metrics> lock, final boolean biased) {
        super(biased);
        _name = name;
        _lock = lock;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final long value) {
        _lock.readLocked(metrics -> {
                    metrics.resetCounter(_name);
                    metrics.incrementCounter(_name, value);
                });
        super.update(value);
    }

    private final String _name;
    private final SafeRefLock<Metrics> _lock;
}
