package net.fabricmc.vanillapings.util;

public class InputCooldown {
    private final int maxCooldown;
    private int resetTicks;

    public InputCooldown(int maxCooldown) {
        this.maxCooldown = maxCooldown;
    }

    public boolean isReady() {
        return resetTicks >= maxCooldown;
    }

    public void triggerCooldown() {
        resetTicks = 0;
    }

    public void tick() {
        resetTicks++;
    }
}
