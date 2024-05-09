package com.vanillapings.features.ping;

import com.vanillapings.translation.Translations;
import com.vanillapings.VanillaPings;
import com.vanillapings.util.InputCooldown;
import com.vanillapings.util.Triple;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataOutput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.visitor.StringNbtWriter;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class PingManager {
    @FunctionalInterface
    public interface CustomPingHandle {
        void ping(Vec3d pos, @Nullable Entity pingEntity, PlayerEntity player, World world);
    }

    private static final List<PingedEntity> entities = new ArrayList<>();
    private static final Style noPingStyle = Text.empty().getStyle().withColor(Formatting.AQUA);
    private static final Text noPingText = Text.literal("_noping").setStyle(noPingStyle);
    private static final Map<UUID, Integer> playerCooldowns = new HashMap<>();
    private static final InputCooldown clearOldPingCooldown = new InputCooldown(20*20);

    /**
     * Calls {@link PingManager#pingInFrontOfEntity(PlayerEntity, CustomPingHandle)} only if the specified player doesn't have a cooldown.
     * If the player doesn't have a cooldown, then one is added for that player.
     * @param player The player from which to cast the ping from and use the cooldown of.
     */
    public static void pingWithCooldown(PlayerEntity player) {
        if(!playerCooldowns.containsKey(player.getUuid())) {
            playerCooldowns.put(player.getUuid(), VanillaPings.SETTINGS.getPingCooldown());
        } else
            return;

        pingInFrontOfEntity(player, null);
    }

    /**
     * Casts a raycast in the direction the player is facing in and spawns a ping.
     * @param player The player from which to cast the ray from.
     * @param customHandle A custom handler which will be called when a ping can be made.
     */
    public static void pingInFrontOfEntity(PlayerEntity player, @Nullable CustomPingHandle customHandle) {
        boolean isNotInWater = player.getBlockStateAtPos().getFluidState().isEmpty();
        @Nullable Entity targetEntity = null;

        @Nullable Vec3d pos = fastRaycast(player, isNotInWater, VanillaPings.SETTINGS.getPingRange());
        if(pos != null && pos.distanceTo(player.getPos()) < 200) {
            @Nullable Vec3d specificPos = exactRaycast(player, isNotInWater, pos.distanceTo(player.getPos()) + 25);
            if(specificPos != null)
                pos = specificPos;
        }

        @Nullable RayResult result = fastEntityRaycast(player, VanillaPings.SETTINGS.getPingRange());

        if(pos == null && result != null) {
            targetEntity = result.entity;
            pos = result.position;
        } else if(pos != null && result != null) {
            if(result.position.distanceTo(player.getPos()) < pos.distanceTo(player.getPos())) {
                pos = result.position;
                targetEntity = result.entity;
            }
        }
        else if(pos == null)
            return;

        if(customHandle != null) {
            customHandle.ping(pos, targetEntity, player, player.getWorld());
            return;
        }
        pingAtPosition(pos, targetEntity, player, player.getWorld());
    }

    /**
     * Spawns a ping at the specified position or highlights a specified entity if it's not null.
     * @param pos The position where a ping will be spawned.
     * @param pingEntity The entity that will be pinged if it's not null.
     * @param player The player that owns the cast and whose name gets broadcast on an entity ping.
     * @param world The world in which to create the ping.
     */
    public static void pingAtPosition(Vec3d pos, @Nullable Entity pingEntity, PlayerEntity player, World world) {
        boolean animate = true;
        boolean kill = true;
        HighlightSettings highlight = new HighlightSettings(VanillaPings.SETTINGS.isPingGlowing(), VanillaPings.SETTINGS.isPingGlowingFlash(), 5, .65f);

        Entity targetEntity;
        if(pingEntity != null) {
            animate = false;
            kill = false;
            targetEntity = pingEntity;
        } else {
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("Marker", true);
            nbt.putBoolean("Small", true);
            NbtList nbtList = new NbtList();
            NbtCompound compound = new NbtCompound();
            nbtList.add(compound);
            nbtList.add(compound);
            nbtList.add(compound);
            compound.putString("id", Registries.ITEM.getId(VanillaPings.SETTINGS.getPingItem()).toString());
            nbtList.add(compound);
            nbt.put("ArmorItems", nbtList);

            ArmorStandEntity entity = Objects.requireNonNull(EntityType.ARMOR_STAND.create(world));
            entity.readCustomDataFromNbt(nbt);
            entity.setCustomName(noPingText);

            entity.setPos(pos.getX(), pos.getY() - .8, pos.getZ());
            entity.setInvulnerable(true);
            entity.setNoGravity(true);
            entity.setInvisible(true);
            entity.setHideBasePlate(true);
            entity.setShowArms(false);
            // entity.setGlowing(true); No longer needed since glowing is now handled by the highlight parameter of the PingEntity
            world.spawnEntity(entity);
            targetEntity = entity;
        }

        // Send ping message
        world.getPlayers().forEach(playerEntity -> {
            Vec3d playerPos = playerEntity.getPos();
            int distance = (int)Math.floor(new Vec3d(pos.x - playerPos.x, 0, pos.z - playerPos.z).length());
            if(distance < VanillaPings.SETTINGS.getPingDirectionMessageRange() || VanillaPings.SETTINGS.hasInfinitePingDirectionMessageRange()) {
                double degree = getDegreeDirectionToPing(pos, playerEntity.getPos());
                double relDegree = getRelativeDegree(degree, playerEntity.getYaw());
                var pingDirMessage = Translations.PING_DIRECTION_MESSAGE.constructMessage(new Triple<>(distance, getPingDirectionArrow(relDegree), getPingCardinalDirection(degree)));
                playerEntity.sendMessage(pingDirMessage, true);
            }

            if(pingEntity != null && (distance < VanillaPings.SETTINGS.getPingChatMessageRange() || VanillaPings.SETTINGS.hasInfinitePingChatMessageRange())) {
                var pingMessage = Translations.PING_MESSAGE.constructMessage(new Triple<>(player.getName().getString(), getTextForEntity(pingEntity), new Vec3i((int) Math.round(pos.x), (int)Math.round(pos.y), (int)Math.round(pos.z))));
                playerEntity.sendMessage(pingMessage);
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

    public static double getDegreeDirectionToPing(Vec3d pingLocation, Vec3d playerLocation) {
        double ak = pingLocation.x - playerLocation.x;
        double gk = pingLocation.z - playerLocation.z;
        double hy = new Vec3d(ak, 0, pingLocation.z - playerLocation.z).length();
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

    public static Text getTextForEntity(Entity entity) {
        if(entity instanceof ItemEntity itemEntity) {
            MutableText completeText = (MutableText) itemEntity.getStack().toHoverableText();

            if(VanillaPings.SETTINGS.isPingItemCount() && VanillaPings.SETTINGS.getPingItemCountRange() != 0) {
                int amount = countStackableItemsInRange(itemEntity.getWorld(), itemEntity.getPos(), VanillaPings.SETTINGS.getPingItemCountRange(), itemEntity.getStack());
                if(amount > 1) {
                    MutableText amountText = (MutableText) Text.of(String.format(" (%dx)", amount));
                    completeText.append(amountText);
                }
            }

            return completeText;
        }

        MutableText completeText = Text.empty().formatted(Formatting.BOLD);
        Style nameStyle = completeText.getStyle().withColor(Formatting.BLUE);
        if(entity instanceof HostileEntity)
            nameStyle = completeText.getStyle().withColor(Formatting.YELLOW);
        else if(entity instanceof LivingEntity)
            nameStyle = completeText.getStyle().withColor(Formatting.DARK_GREEN);

        MutableText nameText = Text.literal(entity.getName().getString());
        nameText.setStyle(nameStyle);
        completeText.append(nameText);

        if(entity instanceof LivingEntity livingEntity) {
            Style healthStyle = completeText.getStyle().withColor(Formatting.GREEN);
            int health = Math.round(livingEntity.getHealth());

            float perc = livingEntity.getHealth()/livingEntity.getMaxHealth();
            if(perc < 0.8)
                healthStyle = completeText.getStyle().withColor(Formatting.DARK_GREEN);
            if(perc < 0.6)
                healthStyle = completeText.getStyle().withColor(Formatting.YELLOW);
            if(perc < 0.4)
                healthStyle = completeText.getStyle().withColor(Formatting.RED);
            if(perc < 0.2)
                healthStyle = completeText.getStyle().withColor(Formatting.DARK_RED);

            MutableText healthText = (MutableText) Text.of(String.format(" (%d❤)", health));
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
    public static int countStackableItemsInRange(World world, Vec3d center, double range, ItemStack targetItem) {
        int totalCount = 0;

        Box boundingBox = new Box(
            center.getX() - range, center.getY() - range, center.getZ() - range,
            center.getX() + range, center.getY() + range, center.getZ() + range
        );

        for (Entity entity : world.getEntitiesByClass(Entity.class, boundingBox, entity -> entity instanceof ItemEntity)) {
            ItemEntity itemEntity = (ItemEntity) entity;

            ItemStack itemStack = itemEntity.getStack();
            // Make sure the item is of the same type has the same name and enchantments
            if (itemStack.getItem() == targetItem.getItem() && itemStack.getName().equals(targetItem.getName()) && itemStack.getEnchantments().equals(targetItem.getEnchantments())) {
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
     * @return The {@link Vec3d} of the raycast hit or null if nothing is hit.
     */
    public static @Nullable Vec3d fastRaycast(Entity sourceEntity, boolean includeFluids, double maxDistance) {
        Vec3d startPos = sourceEntity.getCameraPosVec(1.0f);
        Vec3d lookVec = sourceEntity.getRotationVec(1.0f);

        double stepMultiplier = 0.5;

        double dx = lookVec.x * stepMultiplier;
        double dy = lookVec.y * stepMultiplier;
        double dz = lookVec.z * stepMultiplier;

        double x = startPos.x;
        double y = startPos.y;
        double z = startPos.z;

        World world = sourceEntity.getWorld();

        double distance = 0.0;
        Vec3d prevPos = startPos;
        double yChange;

        while (distance < maxDistance) {
            yChange = y - prevPos.getY();
            BlockPos currentPos = new BlockPos((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
            Vec3d distancePos = new Vec3d(x, startPos.y, z);
            double distanceToStart = startPos.distanceTo(distancePos);

            if (distanceToStart > 256 || distanceToStart > maxDistance || currentPos.getY() > world.getHeight() && yChange > 0 || currentPos.getY() < world.getHeight() * -1 && yChange < 0)
                return null;

            BlockState state = world.getBlockState(currentPos);
            if (!state.isAir()) {
                Vec3d returnPos = new Vec3d(x, world.getBlockState(currentPos.add(0, 1, 0)).isAir() ? y + 0.25 : y, z);
                if(!includeFluids) {
                    if(state.getFluidState().isEmpty())
                        return returnPos;
                } else
                    return returnPos;
            }

            prevPos = new Vec3d(x, y, z);
            x += dx;
            y += dy;
            z += dz;
            distance += 1 * stepMultiplier;
        }

        return null;
    }

    /**
     * A raycast function using the builtin {@link World#raycast(RaycastContext)} function. This is so exact it can raycast between slabs.
     * This function is rather slow especially for long distances.
     * @param sourceEntity The entity from which to cast the ray from using the direction it's facing.
     * @param includeFluids Whether to pass through fluids.
     * @param maxDistance The maximum distance the ray can travel if it travels unobstructed.
     * @return The {@link Vec3d} of the raycast hit or null if nothing is hit.
     */
    public static @Nullable Vec3d exactRaycast(Entity sourceEntity, boolean includeFluids, double maxDistance) {
        World world = sourceEntity.getEntityWorld();

        Vec3d playerPos = sourceEntity.getCameraPosVec(1.0F);
        Vec3d raycastDir = sourceEntity.getRotationVec(1.0F);
        Vec3d raycastEnd = playerPos.add(raycastDir.multiply(maxDistance));

        BlockHitResult blockHitResult = world.raycast(new RaycastContext(playerPos, raycastEnd,
                RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, sourceEntity));

        if (blockHitResult.getType() == HitResult.Type.MISS)
            return null;

        return blockHitResult.getPos();
    }

    /**
     * A fast custom raycast implementation specific for entities.
     * @param sourceEntity The entity from which to cast the ray from using the direction it's facing.
     * @param maxDistance The maximum distance the ray can travel if it travels unobstructed.
     * @return The {@link RayResult} of the raycast, if nothing is hit null is returned.
     */
    public static @Nullable RayResult fastEntityRaycast(Entity sourceEntity, double maxDistance) {
        Vec3d start = sourceEntity.getCameraPosVec(1.0F);
        Vec3d end = start.add(sourceEntity.getRotationVec(1.0F).multiply(maxDistance));
        Box searchBox = sourceEntity.getBoundingBox().stretch(end.subtract(start)).expand(1.0D);

        double closestDistance = Double.POSITIVE_INFINITY;
        @Nullable Vec3d pos = null;
        @Nullable Entity hitEntity = null;

        for (Entity entity : sourceEntity.getWorld().getOtherEntities(sourceEntity, searchBox)) {
            Box entityBox = entity.getBoundingBox().expand(entity.getTargetingMargin());
            Optional<Vec3d> hitResult = entityBox.raycast(start, end);

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
        for (ServerWorld world : server.getWorlds()) {
            List<Entity> remove = new ArrayList<>();
            world.iterateEntities().forEach(entity -> {
                if(entity != null && entity.getCustomName() != null && entity.getCustomName().equals(noPingText) && entities.stream().noneMatch(pingedEntity -> pingedEntity.getEntity().equals(entity)))
                    remove.add(entity);
            });
            removed += remove.size();
            remove.forEach(Entity::kill);
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

    public record RayResult(Vec3d position, @Nullable Entity entity) {
    }
}
