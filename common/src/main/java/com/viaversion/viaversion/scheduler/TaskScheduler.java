/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.viaversion.viaversion.api.scheduler.Scheduler;
import com.viaversion.viaversion.api.scheduler.Task;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class TaskScheduler implements Scheduler {

    private final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("Via Async Task %d").build());
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(
            1, // Fix for https://bugs.openjdk.java.net/browse/JDK-8129861
            new ThreadFactoryBuilder().setNameFormat("Via Async Scheduler %d").build()
    );

    @Override
    public Task execute(final Runnable runnable) {
        return new SubmittedTask(executorService.submit(runnable));
    }

    @Override
    public Task schedule(final Runnable runnable, final long delay, final TimeUnit timeUnit) {
        return new ScheduledTask(scheduledExecutorService.schedule(runnable, delay, timeUnit));
    }

    @Override
    public Task scheduleRepeating(final Runnable runnable, final long delay, final long period, final TimeUnit timeUnit) {
        return new ScheduledTask(scheduledExecutorService.scheduleAtFixedRate(runnable, delay, period, timeUnit));
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
        scheduledExecutorService.shutdown();

        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
            scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }
}
