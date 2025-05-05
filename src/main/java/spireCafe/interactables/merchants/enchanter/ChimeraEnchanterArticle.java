package spireCafe.interactables.merchants.enchanter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.CardGroup.CardGroupType;

import basemod.abstracts.AbstractCardModifier;
import basemod.patches.whatmod.WhatMod;
import spireCafe.abstracts.AbstractMerchant;
import spireCafe.interactables.merchants.enchanter.EnchanterMerchant.ModifierRarity;

public class ChimeraEnchanterArticle extends EnchanterArticle {

    private Class<?> chimeraAbstractAugment;

    public ChimeraEnchanterArticle(AbstractMerchant merchant, AbstractCardModifier modifier, ModifierRarity rarity) {
        this(merchant, modifier, rarity, 0, 0);
    }

    public ChimeraEnchanterArticle(AbstractMerchant merchant, AbstractCardModifier modifier, ModifierRarity rarity,
            float x, float y) {
        super(merchant, modifier, rarity, x, y);
    }

    protected CardGroup getValidEnchantableCards() {
        CardGroup cards = super.getValidEnchantableCards();
        CardGroup newCards = new CardGroup(CardGroupType.UNSPECIFIED);
        try {
            chimeraAbstractAugment = Class.forName("CardAugments.cardmods.AbstractAugment");
            Method m = chimeraAbstractAugment.getMethod("canApplyTo", AbstractCard.class);
            
            for (AbstractCard c : cards.group) {
                if ((boolean) m.invoke(modifier, c)) {
                    newCards.addToBottom(c);
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving card mods from Chimera Cards", e);
        }
        return newCards;
    }


    @Override
    public String getTipHeader() {
        String header = "";
        try {
            chimeraAbstractAugment = Class.forName("CardAugments.cardmods.AbstractAugment");
            Method m = chimeraAbstractAugment.getMethod("modifyName", String.class, AbstractCard.class);
            header = ((String) m.invoke(modifier, "", tooltipBuddy)).replace("  ", " ").trim();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        if (header.isEmpty()) {
            header = modifier.getClass().getSimpleName();
        }
        return header;
    }

    @Override
    public String getTipBody() {
        String body = "";
        try {
            Class<?> formatHelper = Class.forName("CardAugments.util.FormatHelper");
            chimeraAbstractAugment = Class.forName("CardAugments.cardmods.AbstractAugment");
            Method m = formatHelper.getMethod("prefixWords", String.class, String.class);
            Method m2 = chimeraAbstractAugment.getMethod("getAugmentDescription");
            body = m.invoke(null, WhatMod.findModName(modifier.getClass()), "#p") + " NL " + m2.invoke(modifier);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return body;
        }
        if (body.endsWith(" NL ")) {
            body = body.substring(0, body.length()-4);
        }
        return body;
    }
    
}
