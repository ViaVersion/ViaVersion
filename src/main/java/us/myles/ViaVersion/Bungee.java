package us.myles.ViaVersion;

import net.md_5.bungee.api.plugin.Plugin;

import static net.md_5.bungee.api.ChatColor.RED;

public class Bungee extends Plugin {

    @Override
    public void onEnable() {
        getLogger().info(RED + "===============================================");
        getLogger().info(RED + "ViaVersion is NOT a Bungeecord plugin");
        getLogger().info(RED + "Install this plugin on all your spigot/bukkit");
        getLogger().info(RED + "servers and use the latest Bungeecord version");
        getLogger().info(RED + "to make ViaVersion work with BungeeCord.");
        getLogger().info(RED + "===============================================");
    }
}
