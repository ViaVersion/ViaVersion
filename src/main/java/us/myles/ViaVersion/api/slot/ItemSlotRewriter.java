package us.myles.ViaVersion.api.slot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;

import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.MobEffect;
import net.minecraft.server.v1_8_R3.MobEffectList;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PotionBrewer;

import io.netty.buffer.ByteBuf;

import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.util.ReflectionUtil;

public class ItemSlotRewriter {

    public static void rewrite1_9To1_8(ByteBuf input, ByteBuf output) throws CancelException {
        try {
            Object item = readItemStack(input);
            fixIdsFrom1_9To1_8(item);
            writeItemStack(item, output);
        } catch (Exception e) {
            System.out.println("Error while rewriting an item slot.");
            e.printStackTrace();
            throw new CancelException();
        }
    }

    public static void rewrite1_8To1_9(ByteBuf input, ByteBuf output) throws CancelException {
        try {
            Object item = readItemStack(input);
            fixIdsFrom1_8To1_9(item);
            writeItemStack(item, output);
        } catch (Exception e) {
            System.out.println("Error while rewriting an item slot.");
            e.printStackTrace();
            throw new CancelException();
        }
    }

    public static void fixIdsFrom1_9To1_8(Object itemstack) throws NoSuchFieldException, IllegalAccessException {
        if (itemstack != null) {
            ItemStack stack = (ItemStack) itemstack;
            int itemId = Item.getId(stack.getItem());
            if (itemId == Material.MONSTER_EGG.getId() && stack.getData() == 0) {
                NBTTagCompound tag = stack.getTag();
                int data = 0;
                if (tag != null && tag.hasKeyOfType("EntityTag", 10)) {
                    NBTTagCompound entityTag = tag.getCompound("EntityTag");
                    if (entityTag.hasKeyOfType("id", 8)) {
                        String id = entityTag.getString("id");
                        Map<String, Integer> g = (Map<String, Integer>) ReflectionUtil.getStatic(EntityTypes.class, "g", Map.class);
                        data = g.get(id);
                    }
                }
                stack.setTag(null);
                stack.setData(data);
            } else if (itemId == Material.POTION.getId() && stack.getData() == 0) {
                NBTTagCompound tag = stack.getTag();
                if (tag != null) {
                    System.out.println("in: " + tag);
                }
            }
        }
    }

    public static void fixIdsFrom1_8To1_9(Object itemstack) {
        if (itemstack != null) {
            ItemStack stack = (ItemStack) itemstack;
            int itemId = Item.getId(stack.getItem());
            if (itemId == Material.MONSTER_EGG.getId() && stack.getData() != 0) {
                NBTTagCompound tag = stack.getTag();
                if (tag == null) {
                    tag = new NBTTagCompound();
                }
                NBTTagCompound entityTag = new NBTTagCompound();
                entityTag.setString("id", EntityTypes.b(stack.getData()));
                tag.set("EntityTag", entityTag);
                stack.setTag(tag);
                stack.setData(0);
            } else if (itemId == Material.POTION.getId() && stack.getData() != 0) {
                NBTTagCompound tag = stack.getTag();
                if (tag == null) {
                    tag = new NBTTagCompound();
                    stack.setTag(tag);
                }
                try {
                    List<MobEffect> effects = PotionBrewer.getEffects(stack.getData(), true);
                    if (effects != null && effects.size() >= 1) {
                        MobEffect effect = effects.get(0);
                        MobEffectList type = MobEffectList.byId[effect.getEffectId()];
                        StringBuilder name = new StringBuilder();
                        System.out.println(effect.getDuration() + " ?>? " +type.k());
                        if (effect.getAmplifier() > 0) {
                            name.append("strong_");
                        } else if (effect.getDuration() > type.k()) {
                            name.append("long_");
                        }
                        
                        name.append(POTION_TYPE_TO_KEY.get(effect.getEffectId()));
                        System.out.println("Rewriting to: " + name.toString());
                        tag.setString("Potion", name.toString());
                    } else {
                        System.out.println("Falling back to water for subId: " + stack.getData());
                        tag.setString("Potion", "water");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Object readItemStack(ByteBuf input) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object serializer = getPacketDataSerializer(input);
        return READ_ITEM.invoke(serializer);
    }

    public static void writeItemStack(Object itemstack, ByteBuf output) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object serializer = getPacketDataSerializer(output);
        WRITE_ITEM.invoke(serializer, itemstack);
    }

    private static final Map<Integer, String> POTION_TYPE_TO_KEY = new HashMap<>();
    private static Constructor<?> SERIALIZER_CONSTRUCTOR;
    private static Method WRITE_ITEM;
    private static Method READ_ITEM;

    static {
        try {
            Class<?> list = ReflectionUtil.nms("MobEffectList");
            Map<Object, Object> map = ReflectionUtil.getStatic(list, "I", Map.class);
            for (Entry<Object, Object> e : map.entrySet()) {
                System.out.println(e.getValue());
                System.out.println(e.getValue().getClass());
                int id = ReflectionUtil.get(e.getValue(), list, "id", int.class);
                String type = ReflectionUtil.get(e.getKey(), "b", String.class);
                POTION_TYPE_TO_KEY.put(id, type);
            }
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            Class<?> serializer = ReflectionUtil.nms("PacketDataSerializer");
            Class<?> itemStack = ReflectionUtil.nms("ItemStack");
            SERIALIZER_CONSTRUCTOR = serializer.getDeclaredConstructor(ByteBuf.class);
            WRITE_ITEM = serializer.getDeclaredMethod("a", itemStack);
            READ_ITEM = serializer.getDeclaredMethod("i");
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

    private static Object getPacketDataSerializer(ByteBuf buf) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return SERIALIZER_CONSTRUCTOR.newInstance(buf);
    }
}
