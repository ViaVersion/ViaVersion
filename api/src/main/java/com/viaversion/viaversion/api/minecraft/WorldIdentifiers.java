package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.api.connection.StorableObject;

public class WorldIdentifiers implements StorableObject {
    public static String overworldDefault = "minecraft:overworld";
    public static String netherDefault = "minecraft:the_nether";
    public static String endDefault = "minecraft:the_end";

    public String overworld;
    public String nether;
    public String end;

    public WorldIdentifiers(String overworld){
        this(overworld, netherDefault, endDefault);
    }

    public WorldIdentifiers(String overworld, String nether, String end){
        this.overworld = overworld;
        this.nether = nether;
        this.end = end;
    }
}
