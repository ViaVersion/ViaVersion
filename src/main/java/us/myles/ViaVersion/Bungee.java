package us.myles.ViaVersion;

import net.md_5.bungee.api.plugin.Plugin;

import static net.md_5.bungee.api.ChatColor.RED;

public class Bungee extends Plugin {

    @Override
    public void onEnable() {
        getLogger().severe(RED + "===============================================");
        getLogger().severe(RED + "ViaVersion is NOT a Bungeecord plugin");
        getLogger().severe(RED + "Install this plugin on all your spigot/bukkit");
        getLogger().severe(RED + "servers and use the latest Bungeecord version");
        getLogger().severe(RED + "to make ViaVersion work with BungeeCord.");
        getLogger().severe(RED + "===============================================");
    }
}
