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

package com.viaversion.viaversion.rewriter.meta;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MetaFilter {
    private final MetaHandler handler;
    private final EntityType type;
    private final MetaType metaType;
    private final int index;
    private final boolean filterFamily;

    public MetaFilter(@Nullable EntityType type, boolean filterFamily, @Nullable MetaType metaType, int index, MetaHandler handler) {
        Preconditions.checkNotNull(handler, "MetaHandler cannot be null");
        this.type = type;
        this.metaType = metaType;
        this.filterFamily = filterFamily;
        this.index = index;
        this.handler = handler;
    }

    /**
     * Returns the metadata index to filter, or -1.
     *
     * @return metadata index, or -1 if unset
     */
    public int index() {
        return index;
    }

    /**
     * Returns the filtered entity type if present.
     *
     * @return filtered entity type if present
     */
    public @Nullable EntityType type() {
        return type;
    }

    /**
     * Returns the meta type to filter, or null.
     *
     * @return the meta type to filter, or null if unset
     */
    public @Nullable MetaType metaType() {
        return metaType;
    }

    /**
     * Returns the metadata handler.
     *
     * @return metadata handler
     */
    public MetaHandler handler() {
        return handler;
    }

    /**
     * Returns whether entity parent types should be checked against as well.
     *
     * @return whether entity parent types should be checked against
     */
    public boolean filterFamily() {
        return filterFamily;
    }

    /**
     * Returns whether if the metadata should be handled by this filter.
     *
     * @param type     entity type
     * @param metadata metadata
     * @return whether the meta should be filtered
     */
    public boolean isFiltered(@Nullable EntityType type, Metadata metadata) {
        // Check if no specific index is filtered or the indexes are equal
        // Then check if the filter has no entity type or the type is equal to or part of the filtered parent type
        return (this.index == -1 || metadata.id() == this.index)
                && (this.type == null || matchesType(type))
                && (this.metaType == null || metadata.metaType() == this.metaType);
    }

    private boolean matchesType(EntityType type) {
        if (type == null) {
            return false;
        }
        return this.filterFamily ? type.isOrHasParent(this.type) : this.type == type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaFilter that = (MetaFilter) o;
        if (index != that.index) return false;
        if (filterFamily != that.filterFamily) return false;
        if (!handler.equals(that.handler)) return false;
        if (!Objects.equals(metaType, that.metaType)) return false;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        int result = handler.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (metaType != null ? metaType.hashCode() : 0);
        result = 31 * result + index;
        result = 31 * result + (filterFamily ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MetaFilter{" +
                "type=" + type +
                ", filterFamily=" + filterFamily +
                ", metaType=" + metaType +
                ", index=" + index +
                ", handler=" + handler +
                '}';
    }

    public static final class Builder {
        private final EntityRewriter<?, ?> rewriter;
        private EntityType type;
        private MetaType metaType;
        private int index = -1;
        private boolean filterFamily;
        private MetaHandler handler;

        public Builder(EntityRewriter<?, ?> rewriter) {
            this.rewriter = rewriter;
        }

        public Builder metaType(MetaType metaType) {
            Preconditions.checkArgument(this.metaType == null);
            this.metaType = metaType;
            return this;
        }

        /**
         * Sets the type to filter, including subtypes.
         * <p>
         * You should always register a type when accessing specific indexes,
         * even if it is the base entity type, to avoid metadata from unregistered
         * entities causing issues.
         *
         * @param type entity type to filter
         * @return this builder
         */
        public Builder type(EntityType type) {
            Preconditions.checkArgument(this.type == null);
            this.type = type;
            this.filterFamily = true;
            return this;
        }

        /**
         * Sets the type to filter, not including subtypes.
         * <p>
         * You should always register a type when accessing specific indexes,
         * even if it is the base entity type, to avoid metadata from unregistered
         * entities causing issues.
         *
         * @param type exact entity type to filter
         * @return this builder
         */
        public Builder exactType(EntityType type) {
            Preconditions.checkArgument(this.type == null);
            this.type = type;
            this.filterFamily = false;
            return this;
        }

        public Builder index(int index) {
            Preconditions.checkArgument(this.index == -1);
            this.index = index;
            return this;
        }

        public Builder handlerNoRegister(MetaHandler handler) {
            Preconditions.checkArgument(this.handler == null);
            this.handler = handler;
            return this;
        }

        /**
         * Sets the metadata handler and registers the metadata filter.
         * Should always be called last.
         *
         * @param handler metadata handler
         * @throws IllegalArgumentException if a handler has already been set
         */
        public void handler(MetaHandler handler) {
            Preconditions.checkArgument(this.handler == null);
            this.handler = handler;
            register();
        }

        public void mapMetaType(Int2ObjectFunction<MetaType> updateFunction) {
            handler((event, meta) -> {
                MetaType mappedType = updateFunction.apply(meta.metaType().typeId());
                if (mappedType != null) {
                    meta.setMetaType(mappedType);
                } else {
                    event.cancel();
                }
            });
        }

        /**
         * Sets a handler to remove metadata at the given index without affecting any other indexes and registers the filter.
         * Should always be called last.
         *
         * @param index index to cancel
         */
        public void cancel(int index) {
            this.index = index;
            handler((event, meta) -> event.cancel());
        }

        /**
         * Sets a handler to change the index. Does not do any other transformation or shifting and registers the filter.
         * Should always be called last.
         *
         * @param newIndex new index
         * @throws IllegalArgumentException if no index has been set yet
         */
        public void toIndex(int newIndex) {
            Preconditions.checkArgument(this.index != -1);
            handler((event, meta) -> event.setIndex(newIndex));
        }

        /**
         * Sets a handler incrementing every index above the given one and registers the filter.
         * Should always be called last.
         *
         * @param index index to pad
         * @throws IllegalArgumentException if the index has already been set
         */
        public void addIndex(int index) {
            Preconditions.checkArgument(this.index == -1);
            handler((event, meta) -> {
                if (event.index() >= index) {
                    event.setIndex(event.index() + 1);
                }
            });
        }

        /**
         * Sets a handler to remove metadata at the given index, decrementing every index above it and registers the filter.
         * Should always be called last.
         *
         * @param index index to remove
         * @throws IllegalArgumentException if the index has already been set
         */
        public void removeIndex(int index) {
            Preconditions.checkArgument(this.index == -1);
            handler((event, meta) -> {
                int metaIndex = event.index();
                if (metaIndex == index) {
                    event.cancel();
                } else if (metaIndex > index) {
                    event.setIndex(metaIndex - 1);
                }
            });
        }

        /**
         * Creates and registers the created MetaFilter in the linked {@link EntityRewriter} instance.
         */
        public void register() {
            rewriter.registerFilter(build());
        }

        /**
         * Returns a new metadata filter without registering it.
         *
         * @return created meta filter
         */
        public MetaFilter build() {
            return new MetaFilter(type, filterFamily, metaType, index, handler);
        }
    }
}
