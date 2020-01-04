package dev.akarcraft.core.commons.fly;

import dev.akarcraft.core.commons.datamanager.IdObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public class UserFly implements IdObject {
    private UUID playerId;
    private Duration timeLeft;
    private boolean flying;

    // Well this is a long since the updates can be really frequent, and comparing and creating lots of LocalTime instances is really expensive
    private long lastUpdate;

    private UserFly() {
        // gson
    }

    public UserFly(UUID playerId, Duration timeLeft, boolean flying) {
        this.playerId = playerId;
        setTimeLeft(timeLeft);
        this.flying = flying;
    }

    public UserFly(UUID playerId) {
        this.playerId = playerId;
        setTimeLeft(Duration.ZERO);
        this.flying = false;
    }

    public UserFly(OfflinePlayer player) {
        this(player.getUniqueId());
    }

    @Override
    public String id() {
        return playerId.toString();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(getPlayerId()));
    }

    public Duration getTimeLeft() {
        return timeLeft;
    }

    public boolean isFlying() {
        return flying;
    }

    public void setTimeLeft(Duration timeLeft) {
        this.timeLeft = timeLeft;
        lastUpdate = System.currentTimeMillis();
    }

    public void updateTimeLeft() {
        long sinceLastUpdate = System.currentTimeMillis() - lastUpdate;
        timeLeft = timeLeft.minusMillis(sinceLastUpdate);


    }

    public void setFlying(boolean flying) {
        this.flying = flying;

        getPlayer().ifPresent(player -> {
            player.setAllowFlight(flying);
            player.setFlying(flying);
        });
    }
}
