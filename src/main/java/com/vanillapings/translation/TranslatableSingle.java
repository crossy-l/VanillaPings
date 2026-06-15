package com.vanillapings.translation;

import net.minecraft.network.chat.MutableComponent;


public class TranslatableSingle<U> extends Translatable {
    @FunctionalInterface
    public interface TranslatableSupplierSingle<U> {
        MutableComponent get(Translator translator, U extra);
    }

    private final TranslatableSupplierSingle<U> singleSupplier;
    private final TranslatableDefault<U> extraSupplier;

    public TranslatableSingle(TranslatableSingle<U> other, TranslatableDefault<U> extraSupplier) {
        this(other.singleSupplier, extraSupplier);
    }

    public TranslatableSingle(TranslatableSupplierSingle<U> singleSupplier) {
        this(singleSupplier, () -> { throw new TranslatableRequiresArgumentException(); });
    }

    public TranslatableSingle(TranslatableSupplierSingle<U> singleSupplier, TranslatableDefault<U> extraSupplier) {
        super(translator -> singleSupplier.get(translator, extraSupplier.get()));

        this.singleSupplier = singleSupplier;
        this.extraSupplier = extraSupplier;
    }

    public Translatable toTranslatableWithExtraSupplier(TranslatableDefault<U> extraSupplier) {
        return new TranslatableSingle<U>(this, extraSupplier);
    }

    public MutableComponent constructMessage(Translator translator, U extra) {
        return singleSupplier.get(translator, extra);
    }

    public MutableComponent constructMessage(U extra) {
        return constructMessage(Translator.getTranslator(), extra);
    }

    public MutableComponent constructMessage(Translator translator) {
        return constructMessage(translator, extraSupplier.get());
    }

    public MutableComponent constructMessage() {
        return constructMessage(extraSupplier.get());
    }
}
