package net.fabricmc.vanillapings.features.ping;

import net.fabricmc.vanillapings.VanillaPings;
import net.fabricmc.vanillapings.translation.Translations;
import net.fabricmc.vanillapings.util.Triple;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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

public class PingManager {
    private static final List<PingedEntity> entities = new ArrayList<>();
    private static final Style noPingStyle = Text.empty().getStyle().withColor(Formatting.AQUA);
    private static final Text noPingText = Text.literal("_noping").setStyle(noPingStyle);
    private static final Map<UUID, Integer> playerCooldowns = new HashMap<>();
    private static final InputCooldown clearOldPingCooldown = new InputCooldown(20);

    public static void pingWithCooldown(ServerPlayerEntity player) {
        if(!playerCooldowns.containsKey(player.getUuid())) {
            playerCooldowns.put(player.getUuid(), VanillaPings.SETTINGS.getPingCooldown());
        } else
            return;

        pingInFrontOfEntity(player);
    }

    public static void pingInFrontOfEntity(ServerPlayerEntity player) {
        boolean isNotInWater = player.getBlockStateAtPos().getFluidState().isEmpty();
        @Nullable Entity targetEntity = null;

        @Nullable Vec3d pos = fastRaycast(player, VanillaPings.SETTINGS.getPingRange(), isNotInWater);
        if(pos != null && pos.distanceTo(player.getPos()) < 200) {
            @Nullable Vec3d specificPos = exactRaycast(player, isNotInWater, pos.distanceTo(player.getPos()) + 25);
            if(specificPos != null)
                pos = specificPos;
        }

        @Nullable RayResult result = fastEntityRaycast(player);

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

        pingAtPosition(pos, targetEntity, player, player.getServerWorld());
    }

    public static void pingAtPosition(Vec3d pos, @Nullable Entity pingEntity, ServerPlayerEntity player, ServerWorld world) {
        boolean animate = true;
        boolean kill = true;

        Entity targetEntity;
        if(pingEntity instanceof LivingEntity livingEntity) {
            animate = false;
            kill = false;
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 20 * 5, 0));
            targetEntity = livingEntity;
        } else {
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("Marker", true);
            nbt.putBoolean("Small", true);
            NbtList nbtList = new NbtList();
            NbtCompound compound = new NbtCompound();
            Items.AIR.getDefaultStack().writeNbt(compound);
            nbtList.add(compound);
            nbtList.add(compound);
            nbtList.add(compound);
            Items.BLUE_STAINED_GLASS.getDefaultStack().writeNbt(compound);
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
            entity.setGlowing(true);
            world.spawnEntity(entity);
            targetEntity = entity;
        }

        if(pingEntity != null)
            world.getPlayers().forEach(serverPlayerEntity -> serverPlayerEntity.sendMessage(Translations.PING_MESSAGE.constructMessage(new Triple<>(player.getName().getString(), getTextForEntity(pingEntity), new Vec3i((int) Math.round(pos.x), (int)Math.round(pos.y), (int)Math.round(pos.z))))));

        PingedEntity pingedEntity = new PingedEntity(targetEntity, 20 * 5, animate, kill);
        entities.add(pingedEntity);
        pingedEntity.tick();
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

    public static @Nullable Vec3d fastRaycast(Entity entity, double maxDistance, boolean includeFluids) {
        Vec3d startPos = entity.getCameraPosVec(1.0f);
        Vec3d lookVec = entity.getRotationVec(1.0f);

        double stepMultiplier = 0.5;

        double dx = lookVec.x * stepMultiplier;
        double dy = lookVec.y * stepMultiplier;
        double dz = lookVec.z * stepMultiplier;

        double x = startPos.x;
        double y = startPos.y;
        double z = startPos.z;

        World world = entity.getWorld();

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

    public static Vec3d exactRaycast(Entity sourceEntity, boolean includeFluids, double maxDistance) {
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

    public static @Nullable RayResult fastEntityRaycast(Entity sourceEntity) {
        Vec3d start = sourceEntity.getCameraPosVec(1.0F);
        Vec3d end = start.add(sourceEntity.getRotationVec(1.0F).multiply(VanillaPings.SETTINGS.getPingRange()));
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

    public static void tickCooldowns() {
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

    public static void removeOldPings(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            List<Entity> remove = new ArrayList<>();
            world.iterateEntities().forEach(entity -> {
                if(entity != null && entity.getCustomName() != null && entity.getCustomName().equals(noPingText) && entities.stream().noneMatch(pingedEntity -> pingedEntity.getEntity().equals(entity)))
                    remove.add(entity);
            });
            remove.forEach(Entity::kill);
        }
    }

    public static void tick(MinecraftServer server) {
        entities.forEach(PingedEntity::tick);
        entities.removeIf(PingedEntity::isDead);
        tickCooldowns();

        if(clearOldPingCooldown.isReady()) {
            removeOldPings(server);
            clearOldPingCooldown.triggerCooldown();
        }
        clearOldPingCooldown.tick();
    }

    record RayResult(Vec3d position, @Nullable Entity entity) {
    }
}
