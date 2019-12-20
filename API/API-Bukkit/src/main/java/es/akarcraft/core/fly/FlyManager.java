package es.akarcraft.core.fly;

import org.bukkit.OfflinePlayer;

import java.time.Duration;

public interface FlyManager {
    Duration getFlyTime(OfflinePlayer player);

    void setFlyTime(OfflinePlayer player, Duration time);

    void toggleFly(OfflinePlayer player);

    boolean isFlyEnabled(OfflinePlayer player);
}
