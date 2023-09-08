package com.vanillapings.features.ping;

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
    private boolean animate = true;
    private boolean kill = true;

    public PingedEntity(Entity entity, int maxAge, boolean animate, boolean kill) {
        this.entity = entity;
        this.maxAge = maxAge;
        this.animate = animate;
        this.kill = kill;
        startY = entity.getY();
    }

    public void tick() {
        if(dead)
            return;

        if(animate)
            animate();

        audibilize();

        if(age >= maxAge) {
            dead = true;

            if(kill)
                entity.kill();
        }

        age++;
    }

    private void audibilize() {

        if(soundAge == 0)
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL.value(), SoundCategory.PLAYERS, 10f, 1);
        if(soundAge == 5)
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 10f, 1);
        if(soundAge == 7)
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 10f, 1.5f);

        if(age >= maxAge) {
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_FALL, SoundCategory.PLAYERS, 10f, 1.25f);
        }

        soundAge++;
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

        if(age >= maxAge) {
            ((ServerWorld)entity.getWorld()).spawnParticles(ParticleTypes.FIREWORK, entity.getX(), entity.getY() + 1.2, entity.getZ(), 10, 0, 0, 0, 0d);
            ((ServerWorld)entity.getWorld()).spawnParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 1.10, entity.getZ(), 10, 0, 0, 0, 0d);
        }
    }

    public boolean isDead() {
        return dead;
    }

    public Entity getEntity() {
        return entity;
    }
}
