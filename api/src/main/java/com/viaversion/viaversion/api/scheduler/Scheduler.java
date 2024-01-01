/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.scheduler;

import java.util.concurrent.TimeUnit;

public interface Scheduler {

    /**
     * Executes the given runnable asynchronously.
     *
     * @param runnable runnable to execute
     */
    Task execute(Runnable runnable);

    /**
     * Schedules the given runnable to be executed asynchronously after the given delay.
     *
     * @param runnable runnable to execute
     * @param delay    delay before execution
     * @param timeUnit time unit of the delay
     */
    Task schedule(Runnable runnable, long delay, TimeUnit timeUnit);

    /**
     * Schedules the given runnable to be executed asynchronously after a delay and then repeatedly with a period.
     *
     * @param runnable runnable to execute
     * @param delay    delay before execution
     * @param period   period between executions
     * @param timeUnit time unit of the delay and period
     */
    Task scheduleRepeating(Runnable runnable, long delay, long period, TimeUnit timeUnit);

    /**
     * Shuts down the scheduler and awaits task termination.
     */
    void shutdown();
}
