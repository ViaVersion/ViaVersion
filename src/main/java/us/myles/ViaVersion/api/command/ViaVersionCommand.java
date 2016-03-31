package us.myles.ViaVersion.api.command;

public interface ViaVersionCommand {
    /**
     * Register your own subcommand inside ViaVersion
     *
     * @param command Your own SubCommand instance to handle it.
     * @throws Exception throws an exception when the subcommand already exists or if it's not valid, example: spacee
     */
    void registerSubCommand(ViaSubCommand command) throws Exception;

    /**
     * Check if a subcommand is registered.
     *
     * @param name Subcommand name
     * @return true if it exists
     */
    boolean hasSubCommand(String name);

    /**
     * Get subcommand instance by name
     *
     * @param name subcommand name
     * @return ViaSubCommand instance
     */
    ViaSubCommand getSubCommand(String name);
}
