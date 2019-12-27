package dev.akarcraft.core.commons.commands;

import es.akarcraft.core.api.fly.FlyManager;
import es.akarcraft.core.api.util.TimeUtils;
import me.fixeddev.ebcm.CommandAction;
import me.fixeddev.ebcm.CommandContext;
import me.fixeddev.ebcm.exception.CommandException;
import me.fixeddev.ebcm.parametric.CommandClass;
import me.fixeddev.ebcm.parametric.annotation.ACommand;
import me.fixeddev.ebcm.parametric.annotation.Injected;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Optional;

public class FlyCommands implements CommandClass {
    private FlyManager flyManager;
    private Plugin plugin;

    public FlyCommands(FlyManager flyManager, Plugin plugin) {
        this.flyManager = flyManager;
        this.plugin = plugin;
    }

    public CommandAction getMainCommand() {
        return new MainCommand();
    }

    public class MainCommand implements CommandAction {
        @Override
        public boolean execute(CommandContext commandContext) throws CommandException {
            Optional<CommandSender> senderOptional = commandContext.getValue(commandContext.getParts("sender").get(0));

            if (!senderOptional.isPresent()) {
                return false;
            }

            CommandSender commandSender = senderOptional.get();

            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage(ChatColor.BLUE + "Server> " + ChatColor.WHITE + " Solo jugadores pueden ejecutar este comando.");

                return true;
            }

            Player sender = (Player) commandSender;

            if (flyManager.isFlyEnabled(sender)) {
                flyManager.toggleFly(sender);

                if (sender.hasPermission("akarcraft.fly.unlimited")) {
                    sender.sendMessage(ChatColor.BLUE + "Server> " + ChatColor.WHITE + "Has " + ChatColor.RED + "desactivado" + ChatColor.WHITE + " el modo de vuelo ilimitado.");
                } else {
                    sender.sendMessage(ChatColor.BLUE + "Server> " + ChatColor.WHITE + "Has " + ChatColor.RED + "desactivado" + ChatColor.WHITE + " el modo de vuelo.");
                }

                return true;
            }

            if (!sender.hasPermission("akarcraft.fly")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9&lServer> &fNecesitas ser &aVIP &fo superior para poder optar a fly permanente o temporlal. "
                        + "Tienes un total de &b" + flyManager.getFlyTime(sender).toHours() + " horas de vuelo &facumuladas en tu cuenta."));

                return true;
            }

            flyManager.toggleFly(sender);

            if (sender.hasPermission("akarcraft.fly.unlimited")) {
                sender.sendMessage(ChatColor.BLUE + "Server> " + ChatColor.WHITE + "Has " + ChatColor.GREEN + "activado" + ChatColor.WHITE + " el modo de vuelo ilimitado.");
            } else {
                sender.sendMessage(ChatColor.BLUE + "Server> " + ChatColor.WHITE + "Has " + ChatColor.GREEN + "activado" + ChatColor.WHITE + " el modo de vuelo.");
            }

            return true;
        }
    }

    @ACommand(names = "info", permission = "akarcraft.fly.admin")
    public boolean infoCommand(@Injected CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9&lServer> &fComandos para el modo de vuelo:"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f• &e/fly set <jugador> <tiempo> &6&l- Fijar minutos de vuelo."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f• &e/fly add <jugador> <tiempo> &6&l- Agregar minutos de vuelo."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f• &e/fly clear <jugador> &6&l- Borrar tiempo de vuelo."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f• &e/fly look <jugador> &6&l- Comprobar tiempo de vuelo."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));

        return true;
    }

    @ACommand(names = "set", permission = "akarcraft.fly.admin")
    public boolean setCommand(@Injected CommandSender sender, OfflinePlayer target, String time) {
        Duration timeToAdd = TimeUtils.parseDuration(time);

        flyManager.setFlyTime(target, timeToAdd);

        if (!target.isOnline()) {
            sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Server> " + ChatColor.RESET +
                    target.getName() + " no esta online. Se le fijará en la base de datos " + TimeUtils.durationToHumanTime(timeToAdd) + " de vuelo.");
        } else {
            sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Server> " + ChatColor.RESET +
                    target.getName() + " está online y se le han fijado sus " + TimeUtils.durationToHumanTime(timeToAdd) + " de vuelo correctamente.");
        }

        return true;
    }

    @ACommand(names = {"add", "agregar"}, permission = "akarcraft.fly.admin")
    public boolean addCommand(@Injected CommandSender sender, OfflinePlayer target, String time) {
        Duration timeToAdd = TimeUtils.parseDuration(time);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            flyManager.setFlyTime(target, flyManager.getFlyTime(target).plus(timeToAdd));
        });

        if (!target.isOnline()) {
            sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Server> " + ChatColor.RESET +
                    target.getName() + " no esta online. Se le agregará en la base de datos " + TimeUtils.durationToHumanTime(timeToAdd) + " de vuelo.");
        } else {
            sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Server> " + ChatColor.RESET +
                    target.getName() + " está online y ha recibido sus " + TimeUtils.durationToHumanTime(timeToAdd) + " de vuelo correctamente.");
        }

        return true;
    }

    @ACommand(names = "clear", permission = "akarcraft.fly.admin")
    public boolean clearCommand(@Injected CommandSender sender, OfflinePlayer target) {
        flyManager.setFlyTime(target, Duration.ZERO);

        if (!target.isOnline()) {
            sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Server> " + ChatColor.RESET +
                    target.getName() + " no esta online. Se le fijará en la base de datos 0 minutos de vuelo.");
        } else {
            sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Server> " + ChatColor.RESET +
                    target.getName() + " está online y se le han fijado sus 0 minutos de vuelo correctamente.");
        }

        return true;
    }

    @ACommand(names = {"look"}, permission = "akarcraft.fly.admin")
    public boolean lookCommand(@Injected CommandSender sender, OfflinePlayer target) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Server> " + ChatColor.RESET +
                    target.getName() + " tiene " + TimeUtils.durationToHumanTime(flyManager.getFlyTime(target)) + " minutos de vuelo.");
        });

        return true;
    }
}

