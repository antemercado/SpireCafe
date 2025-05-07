package spireCafe.interactables.merchants.enchanter.modifiers;

import java.lang.reflect.InvocationTargetException;

import com.megacrit.cardcrawl.cards.AbstractCard;

import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;

public class RipMod extends AbstractCardModifier {

    private Class<?> rippableModifier;

    // I don't understand how isInherit works on CardMods and after trying for a bit, I realized I do not care.

    @Override
    public void onInitialApplication(AbstractCard card) {
        super.onInitialApplication(card);
        try {
            rippableModifier = Class.forName("thePackmaster.cardmodifiers.rippack.RippableModifier");
            CardModifierManager.addModifier(card, (AbstractCardModifier) rippableModifier.getConstructor(boolean.class).newInstance(false));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new RipMod();
    }
    
}
