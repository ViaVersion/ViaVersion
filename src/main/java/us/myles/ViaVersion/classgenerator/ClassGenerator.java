package us.myles.ViaVersion.classgenerator;

import javassist.*;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.handlers.ViaDecodeHandler;
import us.myles.ViaVersion.handlers.ViaEncodeHandler;
import us.myles.ViaVersion.util.ReflectionUtil;

public class ClassGenerator {
    private static HandlerConstructor constructor = new BasicHandlerConstructor();

    public static HandlerConstructor getConstructor() {
        return constructor;
    }

    public static void generate() {
        if(!ViaVersion.getInstance().isCompatSpigotBuild()) return; // Use Basic Handler as not needed.

        try {
            ClassPool pool = ClassPool.getDefault();
            pool.insertClassPath(new LoaderClassPath(ClassGenerator.class.getClassLoader()));
            // Generate the classes
            transformSuperclass(pool, ViaDecodeHandler.class, ReflectionUtil.nms("PacketDecoder"));
            transformSuperclass(pool, ViaEncodeHandler.class, ReflectionUtil.nms("PacketEncoder"));

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

    private static Class transformSuperclass(ClassPool pool, Class input, Class superclass) {
        String newName = "us.myles.ViaVersion.classgenerator.generated." + input.getSimpleName();

        try {
            CtClass toExtend = pool.get(superclass.getName());
            CtClass generated = pool.getAndRename(input.getName(), newName);
            generated.setSuperclass(toExtend);
            // Modify constructor to call super
            if(generated.getConstructors().length != 0) {
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
            return generated.toClass(HandlerConstructor.class.getClassLoader());
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        return null;
    }
}
