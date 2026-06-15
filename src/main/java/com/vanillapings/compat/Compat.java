package com.vanillapings.compat;

import com.vanillapings.mixin.ArmorStandEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
//? if >=1.19.4 {
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
//?} else {
/*import net.minecraft.util.registry.Registry;
*///?}
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
//? if >=1.21.11 {
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.world.rule.GameRules;
//?} else {
/*import net.minecraft.world.GameRules;*/
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

    /** {@code Identifier.of(ns, path)} on 1.21+, {@code new Identifier(ns, path)} on older versions. */
    public static Identifier id(String namespace, String path) {
        //? if >=1.21 {
        return Identifier.of(namespace, path);
        //?} else {
        /*return new Identifier(namespace, path);*/
        //?}
    }

    /** {@code Identifier.of("ns:path")} on 1.21+, {@code new Identifier("ns:path")} on older versions. */
    public static Identifier id(String id) {
        //? if >=1.21 {
        return Identifier.of(id);
        //?} else {
        /*return new Identifier(id);*/
        //?}
    }

    /** {@code getEntityPos()} on recent versions, {@code getPos()} on older ones. */
    public static Vec3d entityPos(Entity entity) {
        //? if >=1.21.9 {
        return entity.getEntityPos();
        //?} else {
        /*return entity.getPos();*/
        //?}
    }

    /** {@code getEntityWorld()} on recent versions, {@code getWorld()} on older ones. */
    public static World entityWorld(Entity entity) {
        //? if >=1.21.9 {
        return entity.getEntityWorld();
        //?} else {
        /*return entity.getWorld();*/
        //?}
    }

    /** Resolve a sound. 1.19.4+ exposes {@code SoundEvents} as registry entries (needs {@code .value()}); 1.19.2 exposes raw {@code SoundEvent}. */
    //? if >=1.19.4 {
    public static SoundEvent sound(RegistryEntry<SoundEvent> entry) {
        return entry.value();
    }
    //?} else {
    /*public static SoundEvent sound(SoundEvent event) {
        return event;
    }*/
    //?}

    // ---- Item registry ----
    // 1.19.4+ uses net.minecraft.registry.Registries; 1.19.2 uses net.minecraft.util.registry.Registry.
    // The .ITEM.getId/containsId/get methods are identical across both.

    public static Identifier itemId(Item item) {
        //? if >=1.19.4 {
        return Registries.ITEM.getId(item);
        //?} else {
        /*return Registry.ITEM.getId(item);*/
        //?}
    }

    public static boolean itemExists(Identifier id) {
        //? if >=1.19.4 {
        return Registries.ITEM.containsId(id);
        //?} else {
        /*return Registry.ITEM.containsId(id);*/
        //?}
    }

    public static Item getItem(Identifier id) {
        //? if >=1.19.4 {
        return Registries.ITEM.get(id);
        //?} else {
        /*return Registry.ITEM.get(id);*/
        //?}
    }

    /** Send a message to the player's action-bar overlay. The boolean meaning flipped at 1.21.3. */
    public static void sendActionBar(PlayerEntity player, Text text) {
        player.sendMessage(text, true);
    }

    /** Send a message to the player's chat. The boolean meaning flipped at 1.21.3. */
    public static void sendChatMessage(PlayerEntity player, Text text) {
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

    /** Kill an entity through its world. {@code kill(ServerWorld)} on recent versions, {@code kill()} on older ones. */
    public static void kill(Entity entity, ServerWorld world) {
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
    public static ArmorStandEntity spawnPingArmorStand(World world, Vec3d pos, Text customName, ItemStack headItem) {
        //? if >=1.20.5 {
        return EntityType.ARMOR_STAND.spawn(
                (ServerWorld) world,
                armorStand -> configurePingArmorStand(armorStand, customName, headItem, pos),
                BlockPos.ofFloored(pos),
                SpawnReason.COMMAND,
                false,  // alignPosition
                false   // invertY
        );
        //?} else {
        /*ArmorStandEntity armorStand = EntityType.ARMOR_STAND.create(world);
        if (armorStand == null) return null;
        configurePingArmorStand(armorStand, customName, headItem, pos);
        world.spawnEntity(armorStand);
        return armorStand;*/
        //?}
    }

    private static void configurePingArmorStand(ArmorStandEntity armorStand, Text customName, ItemStack headItem, Vec3d pos) {
        ArmorStandEntityAccessor accessor = (ArmorStandEntityAccessor) armorStand;
        accessor.invokeSetMarker(true);
        accessor.invokeSetSmall(true);
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setInvulnerable(true);
        accessor.invokeSetHideBasePlate(true);
        accessor.invokeSetShowArms(false);
        armorStand.setCustomName(customName);
        armorStand.equipStack(EquipmentSlot.HEAD, headItem);
        armorStand.setPos(pos.getX(), pos.getY() - 0.8, pos.getZ());
    }

    // ---- Permissions & game rules ----
    // The permission API (Permission/PermissionLevel + getPermissions()) and the GameRules
    // package/accessors (getValue vs getBoolean, .world.rule vs .world) changed at 1.21.11.

    /** True if the command source has admin-level permission. */
    public static boolean isAdmin(ServerCommandSource source) {
        //? if >=1.21.11 {
        return source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.ADMINS));
        //?} else {
        /*return source.hasPermissionLevel(4);*/
        //?}
    }

    /** True if the player has admin-level (operator) permission. */
    public static boolean isAdmin(ServerPlayerEntity player, MinecraftServer server) {
        //? if >=1.21.11 {
        return player.getPermissions().hasPermission(new Permission.Level(PermissionLevel.ADMINS));
        //?} else {
        /*return player.hasPermissionLevel(server.getOpPermissionLevel());*/
        //?}
    }

    public static boolean commandBlockOutput(ServerCommandSource source) {
        //? if >=1.21.11 {
        return source.getWorld().getGameRules().getValue(GameRules.COMMAND_BLOCK_OUTPUT);
        //?} else {
        /*return source.getServer().getGameRules().getBoolean(GameRules.COMMAND_BLOCK_OUTPUT);*/
        //?}
    }

    public static boolean sendCommandFeedback(ServerCommandSource source) {
        //? if >=1.21.11 {
        return source.getWorld().getGameRules().getValue(GameRules.SEND_COMMAND_FEEDBACK);
        //?} else {
        /*return source.getServer().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK);*/
        //?}
    }

    public static boolean logAdminCommands(ServerCommandSource source) {
        //? if >=1.21.11 {
        return source.getWorld().getGameRules().getValue(GameRules.LOG_ADMIN_COMMANDS);
        //?} else {
        /*return source.getServer().getGameRules().getBoolean(GameRules.LOG_ADMIN_COMMANDS);*/
        //?}
    }
}
