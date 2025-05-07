package spireCafe.interactables.merchants.enchanter.modifiers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;

public class StitchMod extends AbstractCardModifier{

    private Class<?> stitchAction;

    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        super.onUse(card, target, action);
        try {
            stitchAction = Class.forName("thePackmaster.actions.needlework.StitchAction");
            Constructor<?> constructor = stitchAction.getConstructor(AbstractCard.class);
            
            addToBot((AbstractGameAction) constructor.newInstance(card));
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return "anniv5:" + BaseMod.getKeywordProper("anniv5:stitch") + " NL " + rawDescription;
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new StitchMod();
    }
    
}
