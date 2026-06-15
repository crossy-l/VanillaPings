package com.vanillapings.compat;

import com.vanillapings.mixin.ArmorStandEntityAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
//? if >=1.21.11 {
import net.minecraft.commands.Permission;
import net.minecraft.commands.PermissionLevel;
import net.minecraft.world.level.GameRules;
//?} else {
/*import net.minecraft.world.level.GameRules;*/
//?}
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

    /** {@code ResourceLocation.of(ns, path)} on 1.21+, {@code new ResourceLocation(ns, path)} on older versions. */
    public static ResourceLocation id(String namespace, String path) {
        //? if >=1.21 {
        return ResourceLocation.of(namespace, path);
        //?} else {
        /*return new ResourceLocation(namespace, path);*/
        //?}
    }

    /** {@code ResourceLocation.of("ns:path")} on 1.21+, {@code new ResourceLocation("ns:path")} on older versions. */
    public static ResourceLocation id(String id) {
        //? if >=1.21 {
        return ResourceLocation.of(id);
        //?} else {
        /*return new ResourceLocation(id);*/
        //?}
    }

    /** {@code getEntityPos()} on recent versions, {@code getPos()} on older ones. */
    public static Vec3 entityPos(Entity entity) {
        //? if >=1.21.9 {
        return entity.position();
        //?} else {
        /*return entity.getPos();*/
        //?}
    }

    /** {@code getEntityWorld()} on recent versions, {@code getWorld()} on older ones. */
    public static Level entityWorld(Entity entity) {
        //? if >=1.21.9 {
        return entity.level();
        //?} else {
        /*return entity.getWorld();*/
        //?}
    }

    /** Resolve a sound. 1.19.4+ exposes {@code SoundEvents} as registry entries (needs {@code .value()}); 1.19.2 exposes raw {@code SoundEvent}. */
    //? if >=1.19.4 {
    public static SoundEvent sound(Holder<SoundEvent> entry) {
        return entry.value();
    }
    //?} else {
    /*public static SoundEvent sound(SoundEvent event) {
        return event;
    }*/
    //?}

    // ---- Item registry ----
    // 1.19.4+ uses net.minecraft.registry.BuiltInRegistries; 1.19.2 uses net.minecraft.util.registry.Registry.
    // The .ITEM.getId/containsId/get methods are identical across both.

    public static ResourceLocation itemId(Item item) {
        //? if >=1.19.4 {
        return BuiltInRegistries.ITEM.getId(item);
        //?} else {
        /*return Registry.ITEM.getId(item);*/
        //?}
    }

    public static boolean itemExists(ResourceLocation id) {
        //? if >=1.19.4 {
        return BuiltInRegistries.ITEM.containsId(id);
        //?} else {
        /*return Registry.ITEM.containsId(id);*/
        //?}
    }

    public static Item getItem(ResourceLocation id) {
        //? if >=1.19.4 {
        return BuiltInRegistries.ITEM.get(id);
        //?} else {
        /*return Registry.ITEM.get(id);*/
        //?}
    }

    /** Send a message to the player's action-bar overlay. The boolean meaning flipped at 1.21.3. */
    public static void sendActionBar(Player player, Component text) {
        player.sendMessage(text, true);
    }

    /** Send a message to the player's chat. The boolean meaning flipped at 1.21.3. */
    public static void sendChatMessage(Player player, Component text) {
        //? if >=1.21.3 {
        player.sendMessage(text, false);
        //?} else {
        /*player.sendMessage(text);*/
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
        /*entity.kill();*/
        //?}
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
                EntitySpawnReason.COMMAND,
                false,  // alignPosition
                false   // invertY
        );
        //?} else {
        /*ArmorStand armorStand = EntityType.ARMOR_STAND.create(world);
        if (armorStand == null) return null;
        configurePingArmorStand(armorStand, customName, headItem, pos);
        world.spawnEntity(armorStand);
        return armorStand;*/
        //?}
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
        armorStand.setPos(pos.getX(), pos.getY() - 0.8, pos.getZ());
    }

    // ---- Permissions & game rules ----
    // The permission API (Permission/PermissionLevel + getPermissions()) and the GameRules
    // package/accessors (getValue vs getBoolean, .world.rule vs .world) changed at 1.21.11.

    /** True if the command source has admin-level permission. */
    public static boolean isAdmin(CommandSourceStack source) {
        //? if >=1.21.11 {
        return source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.ADMINS));
        //?} else {
        /*return source.hasPermissionLevel(4);*/
        //?}
    }

    /** True if the player has admin-level (operator) permission. */
    public static boolean isAdmin(ServerPlayer player, MinecraftServer server) {
        //? if >=1.21.11 {
        return player.getPermissions().hasPermission(new Permission.Level(PermissionLevel.ADMINS));
        //?} else {
        /*return player.hasPermissionLevel(server.getOpPermissionLevel());*/
        //?}
    }

    public static boolean commandBlockOutput(CommandSourceStack source) {
        //? if >=1.21.11 {
        return source.getWorld().getGameRules().getValue(GameRules.COMMAND_BLOCK_OUTPUT);
        //?} else {
        /*return source.getServer().getGameRules().getBoolean(GameRules.COMMAND_BLOCK_OUTPUT);*/
        //?}
    }

    public static boolean sendCommandFeedback(CommandSourceStack source) {
        //? if >=1.21.11 {
        return source.getWorld().getGameRules().getValue(GameRules.SEND_COMMAND_FEEDBACK);
        //?} else {
        /*return source.getServer().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK);*/
        //?}
    }

    public static boolean logAdminCommands(CommandSourceStack source) {
        //? if >=1.21.11 {
        return source.getWorld().getGameRules().getValue(GameRules.LOG_ADMIN_COMMANDS);
        //?} else {
        /*return source.getServer().getGameRules().getBoolean(GameRules.LOG_ADMIN_COMMANDS);*/
        //?}
    }
}
