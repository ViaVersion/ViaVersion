package us.myles.ViaVersion.api.command;

public interface ViaVersionCommand {

    void registerSubCommand(ViaSubCommand command) throws Exception;

    boolean hasSubCommand(String name);

    ViaSubCommand getSubCommand(String name);
}
