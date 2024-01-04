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
package com.viaversion.viaversion.compatibility.unsafe;

import com.viaversion.viaversion.compatibility.YamlCompat;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public final class Yaml2Compat implements YamlCompat {

    @Override
    public Representer createRepresenter(DumperOptions dumperOptions) {
        return new Representer(dumperOptions);
    }

    @Override
    public SafeConstructor createSafeConstructor() {
        return new CustomSafeConstructor();
    }

    private static final class CustomSafeConstructor extends SafeConstructor {

        public CustomSafeConstructor() {
            super(new LoaderOptions());
            yamlClassConstructors.put(NodeId.mapping, new ConstructYamlMap());
            yamlConstructors.put(Tag.OMAP, new ConstructYamlOmap());
        }
    }
}
