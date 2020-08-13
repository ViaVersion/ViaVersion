package us.myles.ViaVersion.sponge.platform;

import org.spongepowered.api.entity.living.player.Player;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.boss.CommonBoss;

public class SpongeBossBar extends CommonBoss<Player> {

    public SpongeBossBar(String title, float health, BossColor color, BossStyle style) {
        super(title, health, color, style);
    }

    @Override
    public BossBar addPlayer(Player player) {
        addPlayer(player.getUniqueId());
        return this;
    }

    @Override
    public BossBar addPlayers(Player... players) {
        for (Player p : players)
            addPlayer(p);
        return this;
    }

    @Override
    public BossBar removePlayer(Player player) {
        removePlayer(player.getUniqueId());
        return this;
    }
}
