package us.myles.ViaVersion.classgenerator;

import javassist.*;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.handlers.ViaDecodeHandler;
import us.myles.ViaVersion.handlers.ViaEncodeHandler;
import us.myles.ViaVersion.util.ReflectionUtil;

public class ClassGenerator {
    private static HandlerConstructor constructor = new BasicHandlerConstructor();
    private static String psPackage = null;

    public static HandlerConstructor getConstructor() {
        return constructor;
    }

    public static void generate() {
        if (ViaVersion.getInstance().isCompatSpigotBuild() || ViaVersion.getInstance().isProtocolSupport()) {
            try {
                ClassPool pool = ClassPool.getDefault();
                for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
                    pool.insertClassPath(new LoaderClassPath(p.getClass().getClassLoader()));
                }

                if (ViaVersion.getInstance().isCompatSpigotBuild()) {
                    Class decodeSuper = ReflectionUtil.nms("PacketDecoder");
                    Class encodeSuper = ReflectionUtil.nms("PacketEncoder");
                    // Generate the classes
                    addSpigotCompatibility(pool, ViaDecodeHandler.class, decodeSuper);
                    addSpigotCompatibility(pool, ViaEncodeHandler.class, encodeSuper);
                } else {
                    Class decodeSuper = Class.forName(getPSPackage() + ".wrapped.WrappedDecoder");
                    Class encodeSuper = Class.forName(getPSPackage() + ".wrapped.WrappedEncoder");
                    // Generate the classes
                    addPSCompatibility(pool, ViaDecodeHandler.class, decodeSuper);
                    addPSCompatibility(pool, ViaEncodeHandler.class, encodeSuper);
                }


                // Implement Constructor
                CtClass generated = pool.makeClass("us.myles.ViaVersion.classgenerator.generated.GeneratedConstructor");
                CtClass handlerInterface = pool.get(HandlerConstructor.class.getName());

                generated.setInterfaces(new CtClass[]{handlerInterface});
                // Import required classes
                pool.importPackage("us.myles.ViaVersion.classgenerator.generated");
                pool.importPackage("us.myles.ViaVersion.classgenerator");
                pool.importPackage("us.myles.ViaVersion.api.data");
                pool.importPackage("io.netty.handler.codec");
                // Implement Methods
                generated.addMethod(CtMethod.make("public MessageToByteEncoder newEncodeHandler(UserConnection info, MessageToByteEncoder minecraftEncoder) {\n" +
                        "        return new ViaEncodeHandler(info, minecraftEncoder);\n" +
                        "    }", generated));
                generated.addMethod(CtMethod.make("public ByteToMessageDecoder newDecodeHandler(UserConnection info, ByteToMessageDecoder minecraftDecoder) {\n" +
                        "        return new ViaDecodeHandler(info, minecraftDecoder);\n" +
                        "    }", generated));

                constructor = (HandlerConstructor) generated.toClass(HandlerConstructor.class.getClassLoader()).newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (CannotCompileException e) {
                e.printStackTrace();
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static Class addSpigotCompatibility(ClassPool pool, Class input, Class superclass) {
        String newName = "us.myles.ViaVersion.classgenerator.generated." + input.getSimpleName();

        try {
            CtClass generated = pool.getAndRename(input.getName(), newName);
            if (superclass != null) {
                CtClass toExtend = pool.get(superclass.getName());
                generated.setSuperclass(toExtend);

                // If it's NMS satisfy constructor.
                if (superclass.getName().startsWith("net.minecraft")) {
                    // Modify constructor to call super
                    if (generated.getConstructors().length != 0) {
                        generated.getConstructors()[0].instrument(new ExprEditor() {
                            @Override
                            public void edit(ConstructorCall c) throws CannotCompileException {
                                if (c.isSuper()) {
                                    // Constructor for both has a stats thing.
                                    c.replace("super(null);");
                                }
                                super.edit(c);
                            }
                        });
                    }
                }
            }
            return generated.toClass(HandlerConstructor.class.getClassLoader());
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Class addPSCompatibility(ClassPool pool, Class input, Class superclass) {
        String newName = "us.myles.ViaVersion.classgenerator.generated." + input.getSimpleName();

        try {
            CtClass generated = pool.getAndRename(input.getName(), newName);
            if (superclass != null) {
                CtClass toExtend = pool.get(superclass.getName());
                generated.setSuperclass(toExtend);

                // Override setRealEncoder / setRealDecoder
                pool.importPackage(getPSPackage());
                pool.importPackage(getPSPackage() + ".wrapped");
                if (superclass.getName().endsWith("Decoder")) {
                    // Decoder
                    generated.addMethod(CtMethod.make("public void setRealDecoder(IPacketDecoder dec) {\n" +
                            "        WrappedDecoder decoder = new WrappedDecoder();" +
                            "        decoder.setRealDecoder(dec);\n" +
                            "        this.minecraftDecoder = decoder;\n" +
                            "    }", generated));
                } else {
                    // Encoder
                    generated.addMethod(CtMethod.make("public void setRealEncoder(IPacketEncoder enc) {\n" +
                            "        WrappedEncoder encoder = new WrappedEncoder();" +
                            "        encoder.setRealEncoder(enc);\n" +
                            "        this.minecraftEncoder = encoder;\n" +
                            "    }", generated));
                }
            }
            return generated.toClass(HandlerConstructor.class.getClassLoader());
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPSPackage() {
        if (psPackage == null) {
            try {
                Class.forName("protocolsupport.protocol.core.IPacketDecoder");
                psPackage = "protocolsupport.protocol.core";
            } catch (ClassNotFoundException e) {
                try {
                    Class.forName("protocolsupport.protocol.pipeline.IPacketDecoder");
                    psPackage = "protocolsupport.protocol.pipeline";
                } catch (ClassNotFoundException e1) {
                    psPackage = "unknown";
                }
            }
        }
        return psPackage;
    }
}
