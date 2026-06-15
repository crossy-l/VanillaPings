package com.vanillapings.mixin;

import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorStandEntity.class)
public interface ArmorStandEntityAccessor {
    @Invoker("setMarker")
    void invokeSetMarker(boolean marker);
    @Invoker("setSmall")
    void invokeSetSmall(boolean small);
    // Public on 1.20+, private on 1.19.2 — the invoker works regardless of access level.
    @Invoker("setHideBasePlate")
    void invokeSetHideBasePlate(boolean hide);
    @Invoker("setShowArms")
    void invokeSetShowArms(boolean show);
}
