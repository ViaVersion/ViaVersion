package us.myles.ViaVersion.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.boss.CommonBoss;

public class BungeeBossBar extends CommonBoss<ProxiedPlayer> {

    public BungeeBossBar(String title, float health, BossColor color, BossStyle style) {
        super(title, health, color, style);
    }


    @Override
    public BossBar addPlayer(ProxiedPlayer player) {
        addPlayer(player.getUniqueId());
        return this;
    }

    @Override
    public BossBar addPlayers(ProxiedPlayer... players) {
        for (ProxiedPlayer p : players)
            addPlayer(p);
        return this;
    }

    @Override
    public BossBar removePlayer(ProxiedPlayer player) {
        removePlayer(player.getUniqueId());
        return this;
    }
}
