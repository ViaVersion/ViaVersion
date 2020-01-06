package us.myles.ViaVersion.api.rewriters;

@FunctionalInterface
public interface IdRewriteFunction {

    int rewrite(int id);
}