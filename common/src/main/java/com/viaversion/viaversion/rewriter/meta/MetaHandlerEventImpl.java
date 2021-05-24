/*
 * This file is part of ViaBackwards - https://github.com/ViaVersion/ViaBackwards
 * Copyright (C) 2016-2021 ViaVersion and contributors
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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetaHandlerEventImpl implements MetaHandlerEvent {
    private final UserConnection connection;
    private final EntityType entityType;
    private final int entityId;
    private final List<Metadata> metadataList;
    private final Metadata meta;
    private List<Metadata> extraData;
    private int index;
    private boolean cancel;

    public MetaHandlerEventImpl(UserConnection connection, EntityType entityType, int entityId, int index, Metadata meta, List<Metadata> metadataList) {
        this.connection = connection;
        this.entityType = entityType;
        this.entityId = entityId;
        this.index = index;
        this.meta = meta;
        this.metadataList = metadataList;
    }

    @Override
    public @Nullable Metadata getMetaByIndex(int index) {
        for (Metadata meta : metadataList) {
            if (index == meta.id()) {
                return meta;
            }
        }
        return null;
    }

    @Override
    public UserConnection user() {
        return connection;
    }

    @Override
    public int entityId() {
        return entityId;
    }

    @Override
    public EntityType entityType() {
        return entityType;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
        meta.setId(index);
    }

    @Override
    public Metadata meta() {
        return meta;
    }

    @Override
    public void cancel() {
        this.cancel = true;
    }

    @Override
    public boolean cancelled() {
        return cancel;
    }

    @Override
    public List<Metadata> metadataList() {
        return Collections.unmodifiableList(metadataList);
    }

    @Override
    public @Nullable List<Metadata> extraMeta() {
        return extraData;
    }

    @Override
    public void createExtraMeta(Metadata metadata) {
        (extraData != null ? extraData : (extraData = new ArrayList<>())).add(metadata);
    }

    @Override
    public void clearExtraMeta() {
        extraData = null;
    }
}
