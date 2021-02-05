package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data;

import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.rewriters.CommandRewriter;

public class CommandRewriter1_14 extends CommandRewriter {

    public CommandRewriter1_14(Protocol protocol) {
        super(protocol);
    }

    @Override
    @Nullable
    protected String handleArgumentType(String argumentType) {
        if (argumentType.equals("minecraft:nbt")) {
            return "minecraft:nbt_compound_tag";
        }
        return super.handleArgumentType(argumentType);
    }

}
