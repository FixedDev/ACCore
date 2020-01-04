package dev.akarcraft.core.commons.fly;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import dev.akarcraft.core.commons.datamanager.DataManager;
import es.akarcraft.core.api.actionbar.ActionBar;
import es.akarcraft.core.api.bucket.Bucket;
import es.akarcraft.core.api.bucket.factory.BucketFactory;
import es.akarcraft.core.api.bucket.partitioning.PartitioningStrategies;
import es.akarcraft.core.api.fly.FlyManager;
import es.akarcraft.core.api.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FlyManagerImpl implements FlyManager, Listener {

    private DataManager<UUID, UserFly> dataManager;
    private ConcurrentMap<UUID, UserFly> localCache;

    private Bucket<UUID> usersFlying;

    private ActionBar actionBar;

    public FlyManagerImpl(DataManager<UUID, UserFly> dataManager, Plugin plugin, ActionBar actionBar) {
        this.dataManager = dataManager;
        localCache = new ConcurrentHashMap<>();

        usersFlying = BucketFactory.newConcurrentBucket(20, PartitioningStrategies.lowestSize());
        this.actionBar = actionBar;

        Bukkit.getScheduler().runTaskTimer(plugin, new UpdatePlayerFlyTimeTask(),1,1);
    }

    @Override
    public Duration getFlyTime(OfflinePlayer player) {
        UserFly userFly = localCache.computeIfAbsent(player.getUniqueId(), uuid -> dataManager.getObject(uuid).orElse(null));

        if (userFly == null) {
            return Duration.ZERO;
        }

        return userFly.getTimeLeft();
    }

    @Override
    public void setFlyTime(OfflinePlayer player, Duration time) {
        UserFly userFly = localCache.computeIfAbsent(player.getUniqueId(), uuid -> dataManager.getObject(uuid).orElse(new UserFly(player)));

        userFly.setTimeLeft(time);

        if (time.getSeconds() <= 0) {
            userFly.setFlying(false);
            usersFlying.remove(player.getUniqueId());
        }

        dataManager.save(userFly);
    }

    @Override
    public void toggleFly(OfflinePlayer player) {
        UserFly userFly = localCache.computeIfAbsent(player.getUniqueId(), uuid -> dataManager.getObject(uuid).orElse(new UserFly(player)));

        userFly.setFlying(!userFly.isFlying());

        if (!usersFlying.contains(player.getUniqueId())) {
            usersFlying.add(player.getUniqueId());
        } else {
            usersFlying.remove(player.getUniqueId());
        }


        dataManager.save(userFly);
    }

    @Override
    public boolean isFlyEnabled(OfflinePlayer player) {
        UserFly userFly = localCache.computeIfAbsent(player.getUniqueId(), uuid -> dataManager.getObject(uuid).orElse(null));

        if (userFly == null) {
            return false;
        }

        return userFly.isFlying();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Futures.addCallback(dataManager.getObjectAsync(player.getUniqueId()), new FutureCallback<UserFly>() {
            @Override
            public void onSuccess(UserFly userFly) {
                if (userFly == null) {
                    userFly = new UserFly(player);
                }

                if (userFly.isFlying() && !userFly.getTimeLeft().equals(Duration.ZERO)) {
                    player.setFlying(true);
                    player.setAllowFlight(true);

                    usersFlying.add(userFly.getPlayerId());
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        localCache.remove(event.getPlayer().getUniqueId());
        usersFlying.remove(event.getPlayer().getUniqueId());
    }

    private class UpdatePlayerFlyTimeTask implements Runnable {

        @Override
        public void run() {
            Set<UUID> playersSet = usersFlying.asCycle().next();

            for (UUID uuid : playersSet) {
                UserFly userFly = localCache.get(uuid);

                userFly.updateTimeLeft();
                localCache.put(uuid, userFly);

                Duration timeLeft = userFly.getTimeLeft();

                if (timeLeft.isZero() || timeLeft.isNegative()) {
                    userFly.setFlying(false);

                    usersFlying.remove(uuid);

                    userFly.getPlayer().ifPresent(player -> {
                        actionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', "&fEl tiempo de vuelo ha terminado."));
                    });
                } else {
                    userFly.getPlayer().ifPresent(player -> {
                        actionBar.sendActionBar(player, ChatColor.translateAlternateColorCodes('&', "&fTiempo restante de vuelo: &b" + TimeUtils.durationToHumanTime(timeLeft)));
                    });
                }


            }
        }
    }
}
