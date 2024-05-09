package com.vanillapings.mixin;

import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorStandEntity.class)
public interface ArmorStandEntityMixin {
    @Invoker("setHideBasePlate")
    public void invokeSetHideBasePlate(boolean value);
    @Invoker("setShowArms")
    public void invokeSetShowArms(boolean value);
}
