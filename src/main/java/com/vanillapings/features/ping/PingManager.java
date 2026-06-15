package com.vanillapings.features.ping;

import com.vanillapings.VanillaPings;
import com.vanillapings.compat.Compat;
import com.vanillapings.translation.Translations;
import com.vanillapings.util.InputCooldown;
import com.vanillapings.util.Triple;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PingManager {
    @FunctionalInterface
    public interface CustomPingHandle {
        void ping(Vec3 pos, @Nullable Entity pingEntity, Player player, Level world);
    }

    private static final List<PingedEntity> entities = new ArrayList<>();
    private static final Style noPingStyle = Component.empty().getStyle().withColor(ChatFormatting.AQUA);
    private static final Component noPingText = Component.literal("_noping").setStyle(noPingStyle);
    private static final Map<UUID, Integer> playerCooldowns = new HashMap<>();
    private static final InputCooldown clearOldPingCooldown = new InputCooldown(20*20);

    /**
     * Calls {@link PingManager#pingInFrontOfEntity(Player, CustomPingHandle)} only if the specified player doesn't have a cooldown.
     * If the player doesn't have a cooldown, then one is added for that player.
     * @param player The player from which to cast the ping from and use the cooldown of.
     */
    public static void pingWithCooldown(Player player) {
        if(!playerCooldowns.containsKey(player.getUUID())) {
            playerCooldowns.put(player.getUUID(), VanillaPings.SETTINGS.getPingCooldown());
        } else
            return;

        pingInFrontOfEntity(player, null);
    }

    /**
     * Casts a raycast in the direction the player is facing in and spawns a ping.
     * @param player The player from which to cast the ray from.
     * @param customHandle A custom handler which will be called when a ping can be made.
     */
    public static void pingInFrontOfEntity(Player player, @Nullable CustomPingHandle customHandle) {
        boolean isNotInWater = Compat.entityWorld(player).getBlockState(player.blockPosition()).getFluidState().isEmpty();
        @Nullable Entity targetEntity = null;

        @Nullable Vec3 pos = fastRaycast(player, isNotInWater, VanillaPings.SETTINGS.getPingRange());
        if(pos != null && pos.distanceTo(Compat.entityPos(player)) < 200) {
            @Nullable Vec3 specificPos = exactRaycast(player, isNotInWater, pos.distanceTo(Compat.entityPos(player)) + 25);
            if(specificPos != null)
                pos = specificPos;
        }

        @Nullable RayResult result = fastEntityRaycast(player, VanillaPings.SETTINGS.getPingRange());

        if(pos == null && result != null) {
            targetEntity = result.entity;
            pos = result.position;
        } else if(pos != null && result != null) {
            if(result.position.distanceTo(Compat.entityPos(player)) < pos.distanceTo(Compat.entityPos(player))) {
                pos = result.position;
                targetEntity = result.entity;
            }
        }
        else if(pos == null)
            return;

        if(customHandle != null) {
            customHandle.ping(pos, targetEntity, player, Compat.entityWorld(player));
            return;
        }
        pingAtPosition(pos, targetEntity, player, Compat.entityWorld(player));
    }

    /**
     * Spawns a ping at the specified position or highlights a specified entity if it's not null.
     * @param pos The position where a ping will be spawned.
     * @param pingEntity The entity that will be pinged if it's not null.
     * @param player The player that owns the cast and whose name gets broadcast on an entity ping.
     * @param world The world in which to create the ping.
     */
    public static void pingAtPosition(Vec3 pos, @Nullable Entity pingEntity, Player player, Level world) {
        boolean animate = true;
        boolean kill = true;
        HighlightSettings highlight = new HighlightSettings(VanillaPings.SETTINGS.isPingGlowing(), VanillaPings.SETTINGS.isPingGlowingFlash(), 5, .65f);

        Entity targetEntity;
        if(pingEntity != null) {
            animate = false;
            kill = false;
            targetEntity = pingEntity;
        } else {
            ArmorStand entity = Compat.spawnPingArmorStand(world, pos, noPingText, new ItemStack(VanillaPings.SETTINGS.getPingItem()));

            if(entity == null) {
                VanillaPings.LOGGER.error("Couldn't spawn armor stand for ping. This is not intended behaviour.");
                return;
            }

            targetEntity = entity;
        }

        // Send ping message
        world.players().forEach(playerEntity -> {
            Vec3 playerPos = Compat.entityPos(playerEntity);
            int distance = (int)Math.floor(new Vec3(pos.x - playerPos.x, 0, pos.z - playerPos.z).length());
            if(distance < VanillaPings.SETTINGS.getPingDirectionMessageRange() || VanillaPings.SETTINGS.hasInfinitePingDirectionMessageRange()) {
                double degree = getDegreeDirectionToPing(pos, Compat.entityPos(playerEntity));
                double relDegree = getRelativeDegree(degree, playerEntity.getYRot());
                var pingDirMessage = Translations.PING_DIRECTION_MESSAGE.constructMessage(new Triple<>(distance, getPingDirectionArrow(relDegree), getPingCardinalDirection(degree)));
                Compat.sendActionBar(playerEntity, pingDirMessage);
            }

            if(pingEntity != null && (distance < VanillaPings.SETTINGS.getPingChatMessageRange() || VanillaPings.SETTINGS.hasInfinitePingChatMessageRange())) {
                var pingMessage = Translations.PING_MESSAGE.constructMessage(new Triple<>(player.getName().getString(), getTextForEntity(pingEntity), new Vec3i((int) Math.round(pos.x), (int)Math.round(pos.y), (int)Math.round(pos.z))));
                Compat.sendChatMessage(playerEntity, pingMessage);
            }
        });

        PingedEntity pingedEntity = new PingedEntity(targetEntity, 20 * 5, animate, kill, VanillaPings.SETTINGS.isPlaySound(), highlight);
        entities.add(pingedEntity);
        pingedEntity.tick();
    }

    public static double getRelativeDegree(double degree, double playerYaw) {
        if(playerYaw < 0)
            playerYaw += 360;
        playerYaw = 270 - playerYaw;
        double relDegree = degree + playerYaw;
        if(relDegree > 360)
            relDegree -= 360;
        return relDegree;
    }

    public static double getDegreeDirectionToPing(Vec3 pingLocation, Vec3 playerLocation) {
        double ak = pingLocation.x - playerLocation.x;
        double gk = pingLocation.z - playerLocation.z;
        double hy = new Vec3(ak, 0, pingLocation.z - playerLocation.z).length();
        double degree = Math.acos(ak/hy) * (180d/Math.PI);

        if(gk < 0) {
            degree = 360 - degree;
        }
        return degree;
    }

    public static String getPingDirectionArrow(double degree) {
        if(degree >= 22.5 && degree <= 67.5) {
            return "\uD83E\uDC7D";
        } else if(degree >= 67.5 && degree <= 112.5) {
            return "\uD83E\uDC7A";
        } else if (degree >= 112.5 && degree <= 157.5) {
            return "\uD83E\uDC7E";
        } else if(degree >= 157.5 && degree <= 202.5) {
            return "\uD83E\uDC7B";
        } else if(degree >= 202.5 && degree <= 247.5) {
            return "\uD83E\uDC7F";
        } else if(degree >= 247.5 && degree <= 292.5) {
            return "\uD83E\uDC78";
        } else if(degree >= 292.5 && degree <= 337.5) {
            return "\uD83E\uDC7C";
        }
        return "\uD83E\uDC79";
    }

    public static String getPingCardinalDirection(double degree) {
        if(degree >= 22.5 && degree <= 67.5) {
            return "SE";
        } else if(degree >= 67.5 && degree <= 112.5) {
            return "S";
        } else if (degree >= 112.5 && degree <= 157.5) {
            return "SW";
        } else if(degree >= 157.5 && degree <= 202.5) {
            return "W";
        } else if(degree >= 202.5 && degree <= 247.5) {
            return "NW";
        } else if(degree >= 247.5 && degree <= 292.5) {
            return "N";
        } else if(degree >= 292.5 && degree <= 337.5) {
            return "NE";
        }
        return "E";
    }

    public static Component getTextForEntity(Entity entity) {
        if(entity instanceof ItemEntity itemEntity) {
            MutableComponent completeText = (MutableComponent) itemEntity.getItem().getDisplayName();

            if(VanillaPings.SETTINGS.isPingItemCount() && VanillaPings.SETTINGS.getPingItemCountRange() != 0) {
                int amount = countStackableItemsInRange(Compat.entityWorld(itemEntity), Compat.entityPos(itemEntity), VanillaPings.SETTINGS.getPingItemCountRange(), itemEntity.getItem());
                if(amount > 1) {
                    MutableComponent amountText = Component.literal(String.format(" (%dx)", amount));
                    completeText.append(amountText);
                }
            }

            return completeText;
        }

        MutableComponent completeText = Component.empty().withStyle(ChatFormatting.BOLD);
        Style nameStyle = completeText.getStyle().withColor(ChatFormatting.BLUE);
        if(entity instanceof Monster)
            nameStyle = completeText.getStyle().withColor(ChatFormatting.YELLOW);
        else if(entity instanceof LivingEntity)
            nameStyle = completeText.getStyle().withColor(ChatFormatting.DARK_GREEN);

        MutableComponent nameText = Component.literal(entity.getName().getString());
        nameText.setStyle(nameStyle);
        completeText.append(nameText);

        if(entity instanceof LivingEntity livingEntity) {
            Style healthStyle = completeText.getStyle().withColor(ChatFormatting.GREEN);
            int health = Math.round(livingEntity.getHealth());

            float perc = livingEntity.getHealth()/livingEntity.getMaxHealth();
            if(perc < 0.8)
                healthStyle = completeText.getStyle().withColor(ChatFormatting.DARK_GREEN);
            if(perc < 0.6)
                healthStyle = completeText.getStyle().withColor(ChatFormatting.YELLOW);
            if(perc < 0.4)
                healthStyle = completeText.getStyle().withColor(ChatFormatting.RED);
            if(perc < 0.2)
                healthStyle = completeText.getStyle().withColor(ChatFormatting.DARK_RED);

            MutableComponent healthText = Component.literal(String.format(" (%d❤)", health));
            healthText.setStyle(healthStyle);
            completeText.append(healthText);
        }

        return completeText;
    }

    /**
     * Counts all items near a specific position.
     * The items have to be lying on the ground and don't have to be in the same stack.
     * @param world The world in which to count the items.
     * @param center The position where to search.
     * @param range The range items are counted near the center point.
     * @param targetItem The item to use as a filter. Only items with the same type, name and enchantments are counted.
     * @return The amount of items that are like the {@code targetItem} nearby.
     */
    public static int countStackableItemsInRange(Level world, Vec3 center, double range, ItemStack targetItem) {
        int totalCount = 0;

        AABB boundingBox = new AABB(
            center.x() - range, center.y() - range, center.z() - range,
            center.x() + range, center.y() + range, center.z() + range
        );

        for (Entity entity : world.getEntitiesOfClass(Entity.class, boundingBox, entity -> entity instanceof ItemEntity)) {
            ItemEntity itemEntity = (ItemEntity) entity;

            ItemStack itemStack = itemEntity.getItem();
            // Make sure the item is of the same type has the same name and enchantments
            if (itemStack.getItem() == targetItem.getItem() && itemStack.getHoverName().equals(targetItem.getHoverName()) && Compat.enchantmentsMatch(itemStack, targetItem)) {
                totalCount += itemStack.getCount();
            }
        }

        return totalCount;
    }

    /**
     * A fast custom raycast implementation that sacrifices precision for speed.
     * The ray can't travel between slabs as it stops as soon as a block is on the same coordinate.
     * It also stops if it reaches higher than build limit or below build limit, however it's still possible to cast into the build zone from below or above the build limit.
     * @param sourceEntity The entity from which to cast the ray from using the direction it's facing.
     * @param includeFluids Whether to pass through fluids.
     * @param maxDistance The maximum distance the ray can travel if it travels unobstructed. The ray won't travel further than 256 as that is the maximum possible render distance.
     * @return The {@link Vec3} of the raycast hit or null if nothing is hit.
     */
    public static @Nullable Vec3 fastRaycast(Entity sourceEntity, boolean includeFluids, double maxDistance) {
        Vec3 startPos = sourceEntity.getEyePosition(1.0f);
        Vec3 lookVec = sourceEntity.getViewVector(1.0f);

        double stepMultiplier = 0.5;

        double dx = lookVec.x * stepMultiplier;
        double dy = lookVec.y * stepMultiplier;
        double dz = lookVec.z * stepMultiplier;

        double x = startPos.x;
        double y = startPos.y;
        double z = startPos.z;

        Level world = Compat.entityWorld(sourceEntity);

        double distance = 0.0;
        Vec3 prevPos = startPos;
        double yChange;

        while (distance < maxDistance) {
            yChange = y - prevPos.y();
            BlockPos currentPos = new BlockPos((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
            Vec3 distancePos = new Vec3(x, startPos.y, z);
            double distanceToStart = startPos.distanceTo(distancePos);

            if (distanceToStart > 256 || distanceToStart > maxDistance || currentPos.getY() > world.getHeight() && yChange > 0 || currentPos.getY() < world.getHeight() * -1 && yChange < 0)
                return null;

            BlockState state = world.getBlockState(currentPos);
            if (!state.isAir()) {
                Vec3 returnPos = new Vec3(x, world.getBlockState(currentPos.offset(0, 1, 0)).isAir() ? y + 0.25 : y, z);
                if(!includeFluids) {
                    if(state.getFluidState().isEmpty())
                        return returnPos;
                } else
                    return returnPos;
            }

            prevPos = new Vec3(x, y, z);
            x += dx;
            y += dy;
            z += dz;
            distance += 1 * stepMultiplier;
        }

        return null;
    }

    /**
     * A raycast function using the builtin {@link Level#raycast(ClipContext)} function. This is so exact it can raycast between slabs.
     * This function is rather slow especially for long distances.
     * @param sourceEntity The entity from which to cast the ray from using the direction it's facing.
     * @param includeFluids Whether to pass through fluids.
     * @param maxDistance The maximum distance the ray can travel if it travels unobstructed.
     * @return The {@link Vec3} of the raycast hit or null if nothing is hit.
     */
    public static @Nullable Vec3 exactRaycast(Entity sourceEntity, boolean includeFluids, double maxDistance) {
        Level world = Compat.entityWorld(sourceEntity);

        Vec3 playerPos = sourceEntity.getEyePosition(1.0F);
        Vec3 raycastDir = sourceEntity.getViewVector(1.0F);
        Vec3 raycastEnd = playerPos.add(raycastDir.scale(maxDistance));

        BlockHitResult blockHitResult = world.clip(new ClipContext(playerPos, raycastEnd,
                ClipContext.Block.OUTLINE, includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, sourceEntity));

        if (blockHitResult.getType() == HitResult.Type.MISS)
            return null;

        return blockHitResult.getLocation();
    }

    /**
     * A fast custom raycast implementation specific for entities.
     * @param sourceEntity The entity from which to cast the ray from using the direction it's facing.
     * @param maxDistance The maximum distance the ray can travel if it travels unobstructed.
     * @return The {@link RayResult} of the raycast, if nothing is hit null is returned.
     */
    public static @Nullable RayResult fastEntityRaycast(Entity sourceEntity, double maxDistance) {
        Vec3 start = sourceEntity.getEyePosition(1.0F);
        Vec3 end = start.add(sourceEntity.getViewVector(1.0F).scale(maxDistance));
        AABB searchBox = sourceEntity.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D);

        double closestDistance = Double.POSITIVE_INFINITY;
        @Nullable Vec3 pos = null;
        @Nullable Entity hitEntity = null;

        for (Entity entity : Compat.entityWorld(sourceEntity).getEntities(sourceEntity, searchBox)) {
            AABB entityBox = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> hitResult = entityBox.clip(start, end);

            if (entityBox.contains(start)) {
                if (closestDistance >= 0.0D) {
                    closestDistance = 0.0D;
                    pos = start;
                }
            } else if (hitResult.isPresent()) {
                double distance = start.distanceTo(hitResult.get());
                if(entity.getCustomName() != null && entity.getCustomName().equals(noPingText))
                    continue;
                if (distance < closestDistance || closestDistance == 0.0D) {
                    closestDistance = distance;
                    pos = hitResult.get();
                    hitEntity = entity;
                }
            }
        }

        if(pos == null || hitEntity == null)
            return null;

        return new RayResult(pos, hitEntity);
    }

    /**
     * Loops over all loaded entities and removes them if they are a leftover ping armor stand.
     * @param server The server to use.
     * @return The amount of old pings removed.
     */
    public static int removeOldPings(MinecraftServer server) {
        int removed = 0;
        for (ServerLevel world : server.getAllLevels()) {
            List<Entity> remove = new ArrayList<>();
            world.getAllEntities().forEach(entity -> {
                if(entity != null && entity.getCustomName() != null && entity.getCustomName().equals(noPingText) && entities.stream().noneMatch(pingedEntity -> pingedEntity.getEntity().equals(entity)))
                    remove.add(entity);
            });
            removed += remove.size();
            remove.forEach(e -> Compat.kill(e, world));
        }
        return removed;
    }

    /**
     * Ticks all ping input cooldowns by one, this is done automatically in the {@link PingManager#tick(MinecraftServer)} function.
     */
    public static void tickInputCooldowns() {
        var iterator = playerCooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int updatedValue = entry.getValue() - 1;
            if (updatedValue < 0) {
                iterator.remove(); // Remove the entry if value is negative
            } else {
                entry.setValue(updatedValue); // Update the value
            }
        }
    }

    private static void tickOldPingCooldown(MinecraftServer server) {
        if(!VanillaPings.SETTINGS.isPingRemoveOld())
            return;

        if(clearOldPingCooldown.isReady()) {
            removeOldPings(server);
            clearOldPingCooldown.triggerCooldown();
        }
        clearOldPingCooldown.tick();
    }

    /**
     * Gets called once per server tick and updates all time related ping systems.
     */
    public static void tick(MinecraftServer server) {
        entities.forEach(PingedEntity::tick);
        entities.removeIf(PingedEntity::isDead);
        tickInputCooldowns();
        tickOldPingCooldown(server);
    }

    public record RayResult(Vec3 position, @Nullable Entity entity) {
    }
}
