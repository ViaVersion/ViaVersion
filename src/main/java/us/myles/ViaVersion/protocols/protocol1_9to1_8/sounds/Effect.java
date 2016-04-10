package us.myles.ViaVersion.protocols.protocol1_9to1_8.sounds;

import java.util.HashMap;

public class Effect {

    private static HashMap<Integer, Integer> effects;

    static {
        effects = new HashMap<>();
        addRewrite(1005, 1010); //Play music disc
        addRewrite(1003, 1005); //Iron door open
        addRewrite(1006, 1011); //Iron door close
        addRewrite(1004, 1009); //Fizz / Fire extinguished
        addRewrite(1007, 1015); //Ghast charge / warns
        addRewrite(1008, 1016); //Ghast shoot
        addRewrite(1009, 1016); //Ghast shoot (Lower volume according to wiki.vg)
        addRewrite(1010, 1019); //Zombie attacks wood door
        addRewrite(1011, 1020); //Zombie attacks metal door
        addRewrite(1012, 1021); //Zombie breaks  wood door
        addRewrite(1014, 1024); //Wither shoot
        addRewrite(1015, 1025); //Bat takeoff / aka herobrine
        addRewrite(1016, 1026); //Zombie inject
        addRewrite(1017, 1027); //Zombie villager converted
        addRewrite(1020, 1029); //Anvil break
        addRewrite(1021, 1030); //Anvil use
        addRewrite(1022, 1031); //Anvil land

    }

    public static int getNewId(int id) {
        if (!contains(id))
            return id;
        return effects.get(id);
    }

    public static boolean contains(int oldId) {
        return effects.containsKey(oldId);
    }

    private static void addRewrite(int oldId, int newId) {
        effects.put(oldId, newId);
    }
}
