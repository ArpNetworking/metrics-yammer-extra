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

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.TimerContextIntercept;

/**
 * A pass-through TimerContext.
 *
 * @author Brandon Arp (barp at groupon dot com)
 */
public class TimerContext extends TimerContextIntercept {
    /**
     * Creates a new {@link TimerContext} with the current time as its starting value and with the
     * given {@link Timer}.
     *
     * @param timer the {@link Timer} to report the elapsed time to
     * @param clock the clock
     */
    TimerContext(final com.yammer.metrics.core.Timer timer, final Clock clock) {
        super(timer, clock);
    }
}
