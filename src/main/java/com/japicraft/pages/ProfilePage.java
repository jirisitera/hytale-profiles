package com.japicraft.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class ProfilePage extends InteractiveCustomUIPage<ProfilePage.ProfileData> {
    private static final String PROFILE_UI = "Pages/Profile.ui";
    private static final String BUTTON = "Button";
    private static final String LIKE_BUTTON = "Like" + BUTTON;
    private static final String EXIT_BUTTON = "Exit" + BUTTON;
    private static final String SEARCH_BAR = "SearchBar";
    private static final String SEARCH_STATUS = "SearchStatus";
    private static final String EMPTY_SLOT = "EditorTool_Hitbox";
    private static final String NO_ITEM = "No Item Equipped";
    private static final I18nModule i18n = I18nModule.get();
    private static final long NANOS_PER_SECOND = 1000000000L;
    public ProfilePage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, ProfileData.CODEC);
    }
    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder builder, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        builder.append(PROFILE_UI);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }
        builder.set("#TitleText.Text", "Profile of player " + player.getDisplayName());
        // show player's stats
        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap != null) {
            this.appendStat(builder, statMap, "Health", DefaultEntityStatTypes.getHealth());
            this.appendStat(builder, statMap, "Stamina", DefaultEntityStatTypes.getStamina());
            this.appendStat(builder, statMap, "Oxygen", DefaultEntityStatTypes.getOxygen());
            this.appendStat(builder, statMap, "Mana", DefaultEntityStatTypes.getMana());
            this.appendStat(builder, statMap, "Energy", DefaultEntityStatTypes.getSignatureEnergy());
            this.appendStat(builder, statMap, "Ammo", DefaultEntityStatTypes.getAmmo());
        }
        // show worn armor items
        ItemContainer armor = player.getInventory().getArmor();
        String language = playerRef.getLanguage();
        this.appendItem(builder, language, "Helmet", armor.getItemStack((short) 0));
        this.appendItem(builder, language, "Chestplate", armor.getItemStack((short) 1));
        this.appendItem(builder, language, "Gauntlets", armor.getItemStack((short) 2));
        this.appendItem(builder, language, "Boots", armor.getItemStack((short) 3));
        // show other useful data
        World world = player.getWorld();
        if (world != null) {
            builder.set("#World.Text", "Current world: " + world.getName());
        }
        builder.set("#Spawn.Text", "Time alive: " + player.getSinceLastSpawnNanos() / NANOS_PER_SECOND + " seconds");
        // register buttons
        events.addEventBinding(CustomUIEventBindingType.Activating, "#" + LIKE_BUTTON, EventData.of(BUTTON, LIKE_BUTTON));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#" + EXIT_BUTTON, EventData.of(BUTTON, EXIT_BUTTON));
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#" + SEARCH_BAR, EventData.of("@" + SEARCH_BAR, "#" + SEARCH_BAR + ".Value"));
    }
    private void appendStat(UICommandBuilder builder, EntityStatMap statMap, String name, int type) {
        EntityStatValue stat = statMap.get(type);
        if (stat == null) {
            return;
        }
        builder.set("#" + name + ".Text", name + ": " + stat.get() + "/" + stat.getMax());
    }
    private void appendItem(UICommandBuilder builder, String language, String slotName, ItemStack item) {
        boolean isSlotEmpty = item == null;
        builder.set("#" + slotName + "." + "ItemId", isSlotEmpty ? EMPTY_SLOT : item.getItemId());
        builder.set("#" + slotName + "." + "TooltipText", isSlotEmpty ? NO_ITEM : this.getItemTooltip(language, item));
    }
    private String getItemTooltip(String language, ItemStack itemStack) {
        Item item = Item.getAssetMap().getAsset(itemStack.getItemId());
        if (item == null) {
            return NO_ITEM;
        }
        return i18n.getMessage(language, item.getTranslationKey()) + " (" + Math.round(itemStack.getDurability()) + "/" + Math.round(item.getMaxDurability()) + ")";
    }
    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ProfileData data) {
        super.handleDataEvent(ref, store, data);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }
        String searchedName = data.search;
        if (searchedName != null) {
            if (searchedName.length() < 3) {
                updateSearchStatus("Name is too short.");
                return;
            }
            PlayerRef searchedPlayerRef = Universe.get().getPlayerByUsername(searchedName, NameMatching.EXACT_IGNORE_CASE);
            if (searchedPlayerRef == null) {
                updateSearchStatus("Player " + searchedName + " not found.");
            } else {
                updateSearchStatus("Found player " + searchedPlayerRef.getUsername() + "!");
            }
            return;
        }
        sendUpdate();
        String buttonName = data.button;
        if (buttonName == null) {
            return;
        }
        switch (buttonName) {
            case LIKE_BUTTON -> {
                player.sendMessage(Message.raw("You gave a like to " + player.getDisplayName() + "'s profile!"));
                player.sendMessage(Message.raw("You received a like on your profile from " + player.getDisplayName() + "!"));
            }
            case EXIT_BUTTON -> player.getPageManager().setPage(ref, store, Page.None);
        }
    }
    public void updateSearchStatus(String status) {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.set("#" + SEARCH_STATUS + ".Text", status);
        sendUpdate(uiCommandBuilder, false);
    }
    public static class ProfileData {
        public static final BuilderCodec<ProfileData> CODEC = BuilderCodec.builder(ProfileData.class, ProfileData::new)
                .append(new KeyedCodec<>(BUTTON, Codec.STRING), (d, v) -> d.button = v, d -> d.button).add()
                .append(new KeyedCodec<>("@" + SEARCH_BAR, Codec.STRING), (d, v) -> d.search = v, d -> d.search).add()
                .build();
        private String button;
        private String search;
    }
}
