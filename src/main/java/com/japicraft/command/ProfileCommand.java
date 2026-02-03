package com.japicraft.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.japicraft.pages.ProfilePage;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class ProfileCommand extends AbstractTargetPlayerCommand {
    public ProfileCommand() {
        super("profile", "Opens a player's profile.");
        this.setPermissionGroup(GameMode.Adventure);
    }
    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NullableDecl Ref<EntityStore> ref, @NonNullDecl Ref<EntityStore> ref1, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world, @NonNullDecl Store<EntityStore> store) {
        if (!commandContext.isPlayer()) {
            commandContext.sendMessage(Message.raw("This command is for players only."));
        }
        Ref<EntityStore> senderRef = commandContext.senderAsPlayerRef();
        if (senderRef == null) {
            return;
        }
        Store<EntityStore> senderStore = senderRef.getStore();
        Player playerSender = senderStore.getComponent(senderRef, Player.getComponentType());
        if (playerSender == null) {
            return;
        }
        playerSender.getPageManager().openCustomPage(senderRef, senderStore, new ProfilePage(playerRef, senderRef));
    }
}
