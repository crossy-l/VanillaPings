package net.fabricmc.vanillapings.ping;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class PingedEntity {
    private boolean dead = false;
    private final int maxAge;
    private int age;
    private final Entity entity;
    private final double startY;
    private final double amplitude = .1;
    private final double frequency = 0.1;
    private final float rotationSpeed = 100.0f;
    private int soundAge = 0;
    private float takeOffY = 0;

    public PingedEntity(Entity entity, int maxAge) {
        this.entity = entity;
        this.maxAge = maxAge;
        startY = entity.getY();
    }

    public void tick() {
        if(dead)
            return;

        animate();

        if(soundAge == 0)
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL.value(), SoundCategory.PLAYERS, 10f, 1);
        if(soundAge == 5)
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 10f, 1);
        if(soundAge == 7)
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 10f, 1.5f);

        soundAge++;

        if(age >= maxAge) {
            ((ServerWorld)entity.getWorld()).spawnParticles(ParticleTypes.FIREWORK, entity.getX(), entity.getY() + 1.25, entity.getZ(), 10, 0, 0, 0, 0d);
            ((ServerWorld)entity.getWorld()).spawnParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 1.15, entity.getZ(), 10, 0, 0, 0, 0d);
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_FALL, SoundCategory.PLAYERS, 10f, 1.25f);
            entity.kill();
            dead = true;
        }
        age++;
    }

    private void animate() {
        double y = startY + MathHelper.sin((float)(age * frequency)) * amplitude + amplitude;

        if(maxAge - age < 20) {
            y += takeOffY;
            takeOffY += 0.1;
        }
        entity.setPos(entity.getX(), y, entity.getZ());

        float rotation = age * rotationSpeed;
        rotation = MathHelper.wrapDegrees(rotation);
        entity.setYaw(rotation);
    }

    public boolean isDead() {
        return dead;
    }
}
