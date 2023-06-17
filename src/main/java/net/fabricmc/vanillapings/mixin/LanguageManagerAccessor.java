package net.fabricmc.vanillapings.mixin;

import net.minecraft.client.resource.language.LanguageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LanguageManager.class)
public interface LanguageManagerAccessor {
    @Accessor("currentLanguageCode")
    String getCurrentLanguageCode();
}
