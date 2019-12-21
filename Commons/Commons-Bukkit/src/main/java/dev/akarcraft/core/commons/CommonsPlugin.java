package dev.akarcraft.core.commons;

import dev.akarcraft.core.commons.commands.FlyCommands;
import me.fixeddev.ebcm.Command;
import me.fixeddev.ebcm.CommandData;
import me.fixeddev.ebcm.CommandManager;
import me.fixeddev.ebcm.ImmutableCommand;
import me.fixeddev.ebcm.SimpleCommandManager;
import me.fixeddev.ebcm.bukkit.BukkitAuthorizer;
import me.fixeddev.ebcm.bukkit.BukkitCommandManager;
import me.fixeddev.ebcm.bukkit.parameter.provider.BukkitModule;
import me.fixeddev.ebcm.parameter.provider.ParameterProviderRegistry;
import me.fixeddev.ebcm.parametric.ParametricCommandBuilder;
import me.fixeddev.ebcm.parametric.ReflectionParametricCommandBuilder;
import me.fixeddev.ebcm.part.ArgumentPart;
import me.fixeddev.ebcm.part.SubCommandPart;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

// TODO: Better name for this class
public class CommonsPlugin extends JavaPlugin {

    private CommandManager commandManager;
    private ParametricCommandBuilder parametricCommandBuilder;

    @Override
    public void onEnable() {
        createCommandManager();
        registerCommands();
    }

    private void createCommandManager() {
        ParameterProviderRegistry registry = ParameterProviderRegistry.createRegistry();
        registry.installModule(new BukkitModule());

        CommandManager commandManager = new SimpleCommandManager(new BukkitAuthorizer(), registry);

        commandManager = new BukkitCommandManager(commandManager, this.getName());
        parametricCommandBuilder = new ReflectionParametricCommandBuilder();
    }

    private void registerCommands() {
        // TODO: Create the FlyManager implementation
        FlyCommands flyCommands = new FlyCommands(null, this);
        List<Command> subCommands = parametricCommandBuilder.fromClass(flyCommands);

        Command mainCommand = ImmutableCommand.builder(CommandData.builder("fly")
                .setAliases(Arrays.asList("volar")))
                .addPart(ArgumentPart.builder("sender", CommandSender.class)
                        .setConsumedArguments(0)
                        .setRequired(true)
                        .build())
                .addPart(SubCommandPart.builder("subcommand")
                        .setCommands(subCommands)
                        .build())
                .setAction(flyCommands.getMainCommand())
                .build();

        commandManager.registerCommand(mainCommand);

    }
}
