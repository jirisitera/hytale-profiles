package com.japicraft;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.japicraft.command.ProfileCommand;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class Profiles extends JavaPlugin {
    public Profiles(@NonNullDecl JavaPluginInit init) {
        super(init);
    }
    @Override
    public void setup() {
        this.getCommandRegistry().registerCommand(new ProfileCommand());
    }
}
