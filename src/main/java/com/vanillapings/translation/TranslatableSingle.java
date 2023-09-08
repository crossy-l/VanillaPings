package com.vanillapings.translation;

import net.minecraft.text.MutableText;


public class TranslatableSingle<U> extends Translatable {
    @FunctionalInterface
    public interface TranslatableSupplierSingle<U> {
        MutableText get(Translator translator, U extra);
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

    public MutableText constructMessage(Translator translator, U extra) {
        return singleSupplier.get(translator, extra);
    }

    public MutableText constructMessage(U extra) {
        return constructMessage(Translator.getTranslator(), extra);
    }

    public MutableText constructMessage(Translator translator) {
        return constructMessage(translator, extraSupplier.get());
    }

    public MutableText constructMessage() {
        return constructMessage(extraSupplier.get());
    }
}
