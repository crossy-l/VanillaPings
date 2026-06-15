package com.vanillapings.features.ping;

import com.vanillapings.compat.Compat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

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
            entity.setGlowingTag(true);
        }
    }

    private void end() {
        if(shouldInterfereWithGlowing() && highlightSettings.isHighlight()) {
            entity.setGlowingTag(false);
        }

        if(kill)
            Compat.discard(entity);
    }

    private boolean shouldInterfereWithGlowing() {
        if(highlightSettings.isHighlight()) {
            if(entity instanceof LivingEntity livingEntity) {
                return !livingEntity.getActiveEffects().stream().anyMatch(statusEffectInstance -> statusEffectInstance.getEffect().equals(MobEffects.GLOWING));
            }
            return true;
        }
        return false;
    }

    private void audibilize() {

        if(soundAge == 0)
            Compat.entityWorld(entity).playSound(null, entity.blockPosition(), Compat.sound(SoundEvents.NOTE_BLOCK_COW_BELL), SoundSource.PLAYERS, 10f, 1);
        if(soundAge == 5)
            Compat.entityWorld(entity).playSound(null, entity.blockPosition(), Compat.sound(SoundEvents.NOTE_BLOCK_BELL), SoundSource.PLAYERS, 10f, 1);
        if(soundAge == 7)
            Compat.entityWorld(entity).playSound(null, entity.blockPosition(), Compat.sound(SoundEvents.NOTE_BLOCK_CHIME), SoundSource.PLAYERS, 10f, 1.5f);

        if(age >= maxAge) {
            Compat.entityWorld(entity).playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_FALL, SoundSource.PLAYERS, 10f, 1.25f);
        }

        soundAge++;
    }

    private void animate() {
        // Particles and floating up and down
        double y = startY + Mth.sin((float)(age * frequency)) * amplitude + amplitude;

        if(maxAge - age < 20) {
            y += takeOffY;
            takeOffY += 0.1;
        }
        entity.setPos(entity.getX(), y, entity.getZ());

        float rotation = age * rotationSpeed;
        rotation = Mth.wrapDegrees(rotation);
        entity.setYRot(rotation);

        if(age >= maxAge) {
            ((ServerLevel)Compat.entityWorld(entity)).sendParticles(ParticleTypes.FIREWORK, entity.getX(), entity.getY() + 1.2, entity.getZ(), 10, 0, 0, 0, 0d);
            ((ServerLevel)Compat.entityWorld(entity)).sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 1.10, entity.getZ(), 10, 0, 0, 0, 0d);
        }
    }

    public boolean isDead() {
        return dead;
    }

    public Entity getEntity() {
        return entity;
    }
}
