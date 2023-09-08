package com.vanillapings.translation;

import net.minecraft.text.MutableText;

public class Translatable {
    @FunctionalInterface
    public interface TranslatableDefault<U> {
        U get();
    }

    @FunctionalInterface
    public interface TranslatableSupplier {
        MutableText get(Translator translator);
    }


    private final TranslatableSupplier supplier;

    public Translatable(TranslatableSupplier supplier) {
        this.supplier = supplier;
    }

    public MutableText constructMessage(Translator translator) {
        return supplier.get(translator);
    }

    public MutableText constructMessage() {
        return constructMessage(Translator.getTranslator());
    }
}
