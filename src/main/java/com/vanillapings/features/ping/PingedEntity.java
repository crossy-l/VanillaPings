package com.vanillapings.features.ping;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class PingedEntity {
    private boolean dead = false;
    private final int maxAge;
    private int age = 0;
    private final Entity entity;
    private final double startY;
    private final double amplitude = .1;
    private final double frequency = 0.1;
    private final float rotationSpeed = 100.0f;
    private int soundAge = 0;
    private float takeOffY = 0;
    private boolean animate;
    private boolean kill;
    private boolean playSound;
    private HighlightSettings highlightSettings;

    public PingedEntity(Entity entity, int maxAge, boolean animate, boolean kill, boolean playSound, HighlightSettings highlightSettings) {
        this.entity = entity;
        this.maxAge = maxAge;
        this.animate = animate;
        this.highlightSettings = highlightSettings;
        this.playSound = playSound;
        this.kill = kill;
        startY = entity.getY();
    }

    public void tick() {
        if(dead)
            return;

        if(age == 0)
            start();

        if(animate)
            animate();

        // Highlight animation
        if(highlightSettings.isHighlight() && shouldInterfereWithGlowing()) {
            highlightSettings.animate(age, maxAge, entity);
        }

        if(playSound)
            audibilize();

        if(age >= maxAge) {
            dead = true;
            end();
        }

        age++;
    }

    private void start() {
        if(highlightSettings.isHighlight() && shouldInterfereWithGlowing()) {
            entity.setGlowing(true);
        }
    }

    private void end() {
        if(shouldInterfereWithGlowing() && highlightSettings.isHighlight()) {
            entity.setGlowing(false);
        }

        if(kill)
            entity.kill();
    }

    private boolean shouldInterfereWithGlowing() {
        if(highlightSettings.isHighlight()) {
            if(entity instanceof LivingEntity livingEntity) {
                return !livingEntity.getStatusEffects().stream().anyMatch(statusEffectInstance -> statusEffectInstance.getEffectType().equals(StatusEffects.GLOWING));
            }
            return true;
        }
        return false;
    }

    private void audibilize() {

        if(soundAge == 0)
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL, SoundCategory.PLAYERS, 10f, 1);
        if(soundAge == 5)
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 10f, 1);
        if(soundAge == 7)
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.PLAYERS, 10f, 1.5f);

        if(age >= maxAge) {
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_FALL, SoundCategory.PLAYERS, 10f, 1.25f);
        }

        soundAge++;
    }

    private void animate() {
        // Particles and floating up and down
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
