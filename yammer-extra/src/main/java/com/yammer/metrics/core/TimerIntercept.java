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
package com.yammer.metrics.core;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class that exists as an intermediary to allow class construction.
 *
 * @author Brandon Arp (barp at groupon dot com)
 */
public class TimerIntercept extends Timer {
    /**
     * Public constructor.
     *
     * @param tickThread   background thread for updating the rates
     * @param durationUnit the scale unit for this timer's duration metrics
     * @param rateUnit     the scale unit for this timer's rate metrics
     * @param clock        the clock used to calculate duration
     */
    public TimerIntercept(final ScheduledExecutorService tickThread, final TimeUnit durationUnit, final TimeUnit rateUnit, final Clock
            clock) {
        super(tickThread, durationUnit, rateUnit, clock);
    }
}
