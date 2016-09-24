package us.myles.ViaVersion.boss;

import lombok.Getter;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;

@Getter
public class ViaBossBar extends CommonBoss {

    public ViaBossBar(String title, float health, BossColor color, BossStyle style) {
        super(title, health, color, style);
    }

    @Override
    public BossBar addPlayer(Object player) {
        if (player instanceof Player){
            addPlayer(((Player) player).getUniqueId());
        } else {
            throw new IllegalArgumentException("The addPlayer argument has to be a Bukkit player on this platform");
        }
        return this;
    }

    @Override
    public BossBar addPlayers(Object... players) {
        for (Object p : players)
            addPlayer(p);
        return this;
    }

    @Override
    public BossBar removePlayer(Object player) {
        if (player instanceof Player){
            removePlayer(((Player) player).getUniqueId());
        } else {
            throw new IllegalArgumentException("The removePlayer argument has to be a Bukkit player on this platform");
        }
        return this;
    }
}
