package com.vanillapings.mixin;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorStand.class)
public interface ArmorStandEntityAccessor {
    @Invoker("setMarker")
    void invokeSetMarker(boolean marker);
    @Invoker("setSmall")
    void invokeSetSmall(boolean small);
    // Mojmap names it setNoBasePlate (true = no plate); access level varies by version,
    // but @Invoker works regardless.
    @Invoker("setNoBasePlate")
    void invokeSetHideBasePlate(boolean hide);
    @Invoker("setShowArms")
    void invokeSetShowArms(boolean show);
}
