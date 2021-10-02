/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
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
package com.viaversion.viaversion.util;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.concurrent.ConcurrentSkipListMap;

public class YamlConstructor extends SafeConstructor {
    public YamlConstructor() {
        super();
        yamlClassConstructors.put(NodeId.mapping, new YamlConstructor.ConstructYamlMap());
        yamlConstructors.put(Tag.OMAP, new YamlConstructor.ConstructYamlOmap());
    }

    class Map extends Constructor.ConstructYamlMap {
        @Override
        public Object construct(Node node) {
            Object o = super.construct(node);
            if (o instanceof Map && !(o instanceof ConcurrentSkipListMap)) {
                return new ConcurrentSkipListMap<>((java.util.Map<?, ?>) o);
            }
            return o;
        }
    }

    class ConstructYamlOmap extends Constructor.ConstructYamlOmap {
        public Object construct(Node node) {
            Object o = super.construct(node);
            if (o instanceof Map && !(o instanceof ConcurrentSkipListMap)) {
                return new ConcurrentSkipListMap<>((java.util.Map<?, ?>) o);
            }
            return o;
        }
    }
}
