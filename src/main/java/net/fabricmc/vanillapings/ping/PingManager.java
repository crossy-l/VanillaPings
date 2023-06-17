package net.fabricmc.vanillapings.ping;

import com.mojang.brigadier.Command;
import net.fabricmc.vanillapings.translation.Translations;
import net.fabricmc.vanillapings.util.Triple;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.server.command.CommandManager.*;

import java.util.*;

public class PingManager {
    private static final int MAX_RAYCAST_DISTANCE = 500;
    private static final List<PingedEntity> entities = new ArrayList<>();

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("ping")
                        .requires(serverCommandSource -> serverCommandSource.getEntity() != null)
                        .executes(ctx -> pingInFrontOfEntity((ServerPlayerEntity) Objects.requireNonNull(ctx.getSource().getEntity())))
        ));
    }

    public static int pingInFrontOfEntity(ServerPlayerEntity player) {
        @Nullable RayResult result = getTargetPos(player, true);
        if(result == null)
            return Command.SINGLE_SUCCESS;

        Vec3d pos = result.position;

        ServerWorld world = (ServerWorld) player.getWorld();
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

        entity.setPos(pos.getX(), pos.getY() - .8, pos.getZ());
        entity.setInvulnerable(true);
        entity.setNoGravity(true);
        entity.setInvisible(true);
        entity.setHideBasePlate(true);
        entity.setShowArms(false);
        entity.setGlowing(true);
        world.spawnEntity(entity);

        if(result.entity != null)
            world.getPlayers().forEach(serverPlayerEntity -> serverPlayerEntity.sendMessage(Translations.PING_MESSAGE.constructMessage(new Triple<>(player.getName().getString(), getTextForEntity(result.entity), new Vec3i((int) Math.round(pos.x), (int)Math.round(pos.y), (int)Math.round(pos.z))))));

        PingedEntity pingedEntity = new PingedEntity(entity, 20 * 5);
        entities.add(pingedEntity);
        pingedEntity.tick();

        return Command.SINGLE_SUCCESS;
    }

    public static Text getTextForEntity(Entity entity) {
        if(entity instanceof ItemEntity itemEntity) {
            return itemEntity.getStack().toHoverableText();
        }

        MutableText completeText = Text.empty().formatted(Formatting.BOLD);
        Style nameStyle = completeText.getStyle().withColor(Formatting.BLUE);
        if(entity instanceof HostileEntity)
            nameStyle = completeText.getStyle().withColor(Formatting.YELLOW);
        else if(entity instanceof LivingEntity)
            nameStyle = completeText.getStyle().withColor(Formatting.DARK_GREEN);

        MutableText nameText = (MutableText) Text.literal(entity.getName().getString());
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

    public static RayResult getTargetPos(Entity entity, boolean includeFluids) {
        @Nullable RayResult closestBlock = getTargetBlockPos(entity, includeFluids);
        @Nullable RayResult closestEntity = getTargetEntityPos(entity);

        if(closestBlock == null)
            return closestEntity;
        else if(closestEntity == null)
            return closestBlock;

        Vec3d start = entity.getCameraPosVec(1.0F);
        if(start.distanceTo(closestBlock.position) < start.distanceTo(closestEntity.position))
            return closestBlock;
        return closestEntity;
    }

    public static @Nullable RayResult getTargetBlockPos(Entity sourceEntity, boolean includeFluids) {
        World world = sourceEntity.getEntityWorld();

        Vec3d playerPos = sourceEntity.getCameraPosVec(1.0F);
        Vec3d raycastDir = sourceEntity.getRotationVec(1.0F);
        Vec3d raycastEnd = playerPos.add(raycastDir.multiply(MAX_RAYCAST_DISTANCE));

        BlockHitResult blockHitResult = world.raycast(new RaycastContext(playerPos, raycastEnd,
                RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, sourceEntity));

        if (blockHitResult.getType() == HitResult.Type.MISS)
            return null;

        return new RayResult(blockHitResult.getPos(), null);
    }

    public static @Nullable RayResult getTargetEntityPos(Entity sourceEntity) {
        Vec3d start = sourceEntity.getCameraPosVec(1.0F);
        Vec3d end = start.add(sourceEntity.getRotationVec(1.0F).multiply(MAX_RAYCAST_DISTANCE));
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

    public static void tick() {
        entities.forEach(PingedEntity::tick);
        entities.removeIf(PingedEntity::isDead);
    }

    record RayResult(Vec3d position, @Nullable Entity entity) {

    }
}
