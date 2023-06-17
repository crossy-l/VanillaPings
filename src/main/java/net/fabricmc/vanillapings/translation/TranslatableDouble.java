package net.fabricmc.vanillapings.translation;

import net.minecraft.text.MutableText;


public class TranslatableDouble<U, D> extends Translatable {
    @FunctionalInterface
    public interface TranslatableSupplierDouble<U, D> {
        MutableText get(Translator translator, U extra, D extra2);
    }

    private final TranslatableSupplierDouble<U, D> doubleSupplier;
    private final TranslatableDefault<U> extraSupplier;
    private final TranslatableDefault<D> extraSupplier2;

    public TranslatableDouble(TranslatableDouble<U, D> other, TranslatableDefault<U> extraSupplier, TranslatableDefault<D> extraSupplier2) {
        this(other.doubleSupplier, extraSupplier, extraSupplier2);
    }

    public TranslatableDouble(TranslatableSupplierDouble<U, D> doubleSupplier) {
        this(doubleSupplier, () -> { throw new TranslatableRequiresArgumentException(); }, () -> { throw new TranslatableRequiresArgumentException(); });
    }

    public TranslatableDouble(TranslatableSupplierDouble<U, D> doubleSupplier, TranslatableDefault<U> extraSupplier, TranslatableDefault<D> extraSupplier2) {
        super(translator -> doubleSupplier.get(translator, extraSupplier.get(), extraSupplier2.get()));

        this.doubleSupplier = doubleSupplier;
        this.extraSupplier = extraSupplier;
        this.extraSupplier2 = extraSupplier2;
    }

    public Translatable toTranslatableWithExtraSupplier(TranslatableDefault<U> extraSupplier, TranslatableDefault<D> extraSupplier2) {
        return new TranslatableDouble<U, D>(this, extraSupplier, extraSupplier2);
    }

    public MutableText constructMessage(Translator translator, U extra, D extra2) {
        return doubleSupplier.get(translator, extra, extra2);
    }

    public MutableText constructMessage(U extra, D extra2) {
        return constructMessage(Translator.getTranslator(), extra, extra2);
    }

    public MutableText constructMessage(Translator translator) {
        return constructMessage(translator, extraSupplier.get(), extraSupplier2.get());
    }

    public MutableText constructMessage() {
        return constructMessage(extraSupplier.get(), extraSupplier2.get());
    }
}
