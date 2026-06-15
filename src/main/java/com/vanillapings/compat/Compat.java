package com.vanillapings.compat;

import com.vanillapings.mixin.ArmorStandEntityAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
//? if >=1.21.3 {
import net.minecraft.world.entity.EntitySpawnReason;
//?} elif >=1.20.5 {
/*import net.minecraft.world.entity.MobSpawnType;*/
//?}
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
//? if >=1.19.4 {
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
//?} else {
/*import net.minecraft.core.Registry;
*///?}
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
//? if >=1.21.11 {
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.gamerules.GameRules;
//?} else {
/*import net.minecraft.world.level.GameRules;
*///?}
import org.jetbrains.annotations.Nullable;

/**
 * The version-sensitive Minecraft API, isolated behind small helpers whose bodies are
 * {@code //?} conditionals. Concentrating the per-version differences here keeps the
 * rest of the codebase version-agnostic.
 *
 * <p>Only server-safe API lives here; client-only version drift (e.g. the key-binding
 * category constructor) stays in the client classes that are never loaded on a server.
 */
public final class Compat {
    private Compat() {
    }

    // The id class was renamed ResourceLocation -> Identifier at 1.21.11, and its public
    // constructor was replaced by static factories (fromNamespaceAndPath / parse) at 1.21.
    //? if >=1.21.11 {
    public static Identifier id(String namespace, String path) {
    //?} else {
    /*public static ResourceLocation id(String namespace, String path) {
    *///?}
        //? if >=1.21.11 {
        return Identifier.fromNamespaceAndPath(namespace, path);
        //?} elif >=1.21 {
        /*return ResourceLocation.fromNamespaceAndPath(namespace, path);*/
        //?} else {
        /*return new ResourceLocation(namespace, path);
        *///?}
    }

    //? if >=1.21.11 {
    public static Identifier id(String id) {
    //?} else {
    /*public static ResourceLocation id(String id) {
    *///?}
        //? if >=1.21.11 {
        return Identifier.parse(id);
        //?} elif >=1.21 {
        /*return ResourceLocation.parse(id);*/
        //?} else {
        /*return new ResourceLocation(id);
        *///?}
    }

    /** The entity's position. */
    public static Vec3 entityPos(Entity entity) {
        return entity.position();
    }

    /** The entity's level. The {@code level()} accessor replaced the public {@code level} field at 1.20. */
    public static Level entityWorld(Entity entity) {
        //? if >=1.20 {
        return entity.level();
        //?} else {
        /*return entity.level;
        *///?}
    }

    /** Resolve a sound. 1.19.4+ exposes {@code SoundEvents} as registry entries (needs {@code .value()}); 1.19.2 exposes raw {@code SoundEvent}. */
    //? if >=1.19.4 {
    public static SoundEvent sound(Holder<SoundEvent> entry) {
        return entry.value();
    }
    //?} else {
    /*public static SoundEvent sound(SoundEvent event) {
        return event;
    }
    *///?}

    // ---- Item registry ----
    // 1.19.4+ uses net.minecraft.registry.BuiltInRegistries; 1.19.2 uses net.minecraft.util.registry.Registry.
    // The .ITEM.getId/containsId/get methods are identical across both.

    //? if >=1.21.11 {
    public static Identifier itemId(Item item) {
    //?} else {
    /*public static ResourceLocation itemId(Item item) {
    *///?}
        //? if >=1.19.4 {
        return BuiltInRegistries.ITEM.getKey(item);
        //?} else {
        /*return Registry.ITEM.getKey(item);
        *///?}
    }

    //? if >=1.21.11 {
    public static boolean itemExists(Identifier id) {
    //?} else {
    /*public static boolean itemExists(ResourceLocation id) {
    *///?}
        //? if >=1.19.4 {
        return BuiltInRegistries.ITEM.containsKey(id);
        //?} else {
        /*return Registry.ITEM.containsKey(id);
        *///?}
    }

    //? if >=1.21.11 {
    public static Item getItem(Identifier id) {
    //?} else {
    /*public static Item getItem(ResourceLocation id) {
    *///?}
        //? if >=1.21.3 {
        return BuiltInRegistries.ITEM.getValue(id);
        //?} elif >=1.19.4 {
        /*return BuiltInRegistries.ITEM.get(id);*/
        //?} else {
        /*return Registry.ITEM.get(id);
        *///?}
    }

    /** True if two stacks carry equal enchantments. Storage became the {@code getEnchantments()} component at 1.20.5; older versions expose the raw NBT list. */
    public static boolean enchantmentsMatch(ItemStack a, ItemStack b) {
        //? if >=1.20.5 {
        return a.getEnchantments().equals(b.getEnchantments());
        //?} else {
        /*return a.getEnchantmentTags().equals(b.getEnchantmentTags());
        *///?}
    }

    /** Send a message to the player's action-bar overlay. {@code displayClientMessage(text, true)} split into {@code sendOverlayMessage} at 26.1. */
    public static void sendActionBar(Player player, Component text) {
        //? if >=26.1 {
        /*player.sendOverlayMessage(text);
        *///?} else {
        player.displayClientMessage(text, true);
        //?}
    }

    /** Send a message to the player's chat. {@code displayClientMessage(text, false)} became {@code sendSystemMessage} at 26.1. */
    public static void sendChatMessage(Player player, Component text) {
        //? if >=26.1 {
        /*player.sendSystemMessage(text);
        *///?} else {
        player.displayClientMessage(text, false);
        //?}
    }

    /** Silently remove an entity without firing game events (sculk/Warden). {@code discard()} on recent versions. */
    public static void discard(Entity entity) {
        entity.discard();
    }

    /** Kill an entity through its world. {@code kill(ServerLevel)} on recent versions, {@code kill()} on older ones. */
    public static void kill(Entity entity, ServerLevel world) {
        //? if >=1.21.3 {
        entity.kill(world);
        //?} else {
        /*entity.kill();
        *///?}
    }

    /**
     * Spawn the invisible marker armor stand used to anchor a location ping.
     * The spawn API changed substantially at 1.21.5 (builder {@code spawn(...)} vs.
     * {@code create() + readCustomDataFromNbt()}), so the whole spawn lives here.
     */
    @Nullable
    public static ArmorStand spawnPingArmorStand(Level world, Vec3 pos, Component customName, ItemStack headItem) {
        //? if >=1.20.5 {
        return EntityType.ARMOR_STAND.spawn(
                (ServerLevel) world,
                armorStand -> configurePingArmorStand(armorStand, customName, headItem, pos),
                BlockPos.containing(pos),
                //? if >=1.21.3 {
                EntitySpawnReason.COMMAND,
                //?} else {
                /*MobSpawnType.COMMAND,
                *///?}
                false,  // alignPosition
                false   // invertY
        );
        //?} else {
        /*ArmorStand armorStand = EntityType.ARMOR_STAND.create(world);
        if (armorStand == null) return null;
        configurePingArmorStand(armorStand, customName, headItem, pos);
        ((ServerLevel) world).addFreshEntity(armorStand);
        return armorStand;
        *///?}
    }

    private static void configurePingArmorStand(ArmorStand armorStand, Component customName, ItemStack headItem, Vec3 pos) {
        ArmorStandEntityAccessor accessor = (ArmorStandEntityAccessor) armorStand;
        accessor.invokeSetMarker(true);
        accessor.invokeSetSmall(true);
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setInvulnerable(true);
        accessor.invokeSetHideBasePlate(true);
        accessor.invokeSetShowArms(false);
        armorStand.setCustomName(customName);
        armorStand.setItemSlot(EquipmentSlot.HEAD, headItem);
        armorStand.setPos(pos.x(), pos.y() - 0.8, pos.z());
    }

    // ---- Permissions & game rules ----
    // At 1.21.11 the permission API became permissions().hasPermission(Permission) and the
    // GameRules class moved to ...gamerules with get(GameRule) replacing getBoolean(Key); the
    // rule constants were also renamed (RULE_COMMANDBLOCKOUTPUT -> COMMAND_BLOCK_OUTPUT, etc.).

    /** True if the command source has admin-level permission. */
    public static boolean isAdmin(CommandSourceStack source) {
        //? if >=1.21.11 {
        return source.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
        //?} else {
        /*return source.hasPermission(4);
        *///?}
    }

    /** True if the player has admin-level (operator) permission. */
    public static boolean isAdmin(ServerPlayer player, MinecraftServer server) {
        //? if >=1.21.11 {
        return player.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
        //?} elif >=1.21.9 {
        /*return player.hasPermissions(server.operatorUserPermissionLevel());*/
        //?} else {
        /*return player.hasPermissions(server.getOperatorUserPermissionLevel());
        *///?}
    }

    public static boolean commandBlockOutput(CommandSourceStack source) {
        //? if >=1.21.11 {
        return source.getLevel().getGameRules().get(GameRules.COMMAND_BLOCK_OUTPUT);
        //?} else {
        /*return source.getLevel().getGameRules().getBoolean(GameRules.RULE_COMMANDBLOCKOUTPUT);
        *///?}
    }

    public static boolean sendCommandFeedback(CommandSourceStack source) {
        //? if >=1.21.11 {
        return source.getLevel().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK);
        //?} else {
        /*return source.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK);
        *///?}
    }

    public static boolean logAdminCommands(CommandSourceStack source) {
        //? if >=1.21.11 {
        return source.getLevel().getGameRules().get(GameRules.LOG_ADMIN_COMMANDS);
        //?} else {
        /*return source.getLevel().getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS);
        *///?}
    }
}
