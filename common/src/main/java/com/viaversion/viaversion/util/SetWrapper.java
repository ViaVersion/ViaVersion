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
package com.viaversion.viaversion.util;

import com.google.common.collect.ForwardingSet;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SetWrapper<E> extends ForwardingSet<E> {

    private final Set<E> set;
    private final Consumer<E> addListener;

    public SetWrapper(Set<E> set, Consumer<E> addListener) {
        this.set = set;
        this.addListener = addListener;
    }

    @Override
    public boolean add(@NonNull E element) {
        addListener.accept(element);

        return super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        for (E element : collection) {
            addListener.accept(element);
        }

        return super.addAll(collection);
    }

    @Override
    protected Set<E> delegate() {
        return originalSet();
    }

    public Set<E> originalSet() {
        return this.set;
    }
}
