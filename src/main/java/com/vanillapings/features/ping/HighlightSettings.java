package com.vanillapings.features.ping;

import net.minecraft.entity.Entity;

public class HighlightSettings {
    private final boolean highlight;
    private final boolean flash;
    private int flashRate;
    private float startFlashing;
    private int flashCycle = 0;
    private boolean isActive = true;
    private boolean speedUp = false;

    public HighlightSettings(boolean highlight, boolean flash, int flashRate, float startFlashing) {
        this.highlight = highlight;
        this.flash = flash;
        this.flashRate = flashRate;
        this.startFlashing = startFlashing;
    }

    public HighlightSettings(int flashRate, float startFlashing) {
        this.highlight = true;
        this.flash = true;
        this.flashRate = flashRate;
        this.startFlashing = startFlashing;
    }

    public HighlightSettings(boolean highlight) {
        this.highlight = highlight;
        this.flash = false;
    }

    public void animate(int age, int maxAge, Entity entity) {
        if(!flash) return;

        float flashFactor = ((float) age/maxAge);
        if(flashFactor < startFlashing)
            return;

        if(flashFactor > 0.90 && !speedUp) {
            speedUp = true;
            flashRate /= 2;
        }

        if(flashCycle >= flashRate) {
            flashCycle = 0;
            isActive = !isActive;
            entity.setGlowing(isActive);
        }
        ++flashCycle;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public boolean isFlash() {
        return flash;
    }

    public int getFlashRate() {
        return flashRate;
    }
}
