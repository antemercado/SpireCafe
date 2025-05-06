package spireCafe.interactables.merchants.enchanter;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.CardGroup.CardGroupType;
import com.megacrit.cardcrawl.cards.red.IronWave;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import basemod.patches.whatmod.WhatMod;
import spireCafe.interactables.merchants.enchanter.EnchanterMerchant.ModifierRarity;

public abstract class AbstractEnchantment {
    public static AbstractCard tooltipBuddy = new IronWave();

    public AbstractCardModifier cardModifier;
    public ModifierRarity rarity;
    
    public AbstractEnchantment(AbstractCardModifier cardModifier, ModifierRarity rarity) {
        this.cardModifier = cardModifier;
        this.rarity = rarity;
    }
    
    public CardGroup getValidCards() {
        CardGroup cards = new CardGroup(CardGroupType.UNSPECIFIED);
        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if (CardModifierManager.modifiers(c).isEmpty()) {
                if (cardModifier.shouldApply(c)) {
                    cards.addToTop(c);
                }
            }
        }
        return cards;
    }

    public abstract String getName();

    public abstract String getDescription();

    // public CardGroup getValidChimeraCards(CardGroup cards) {
    //     CardGroup newCards = new CardGroup(CardGroupType.UNSPECIFIED);
    //     try {
    //         chimeraAbstractAugment = Class.forName("CardAugments.cardmods.AbstractAugment");
    //         Method m = chimeraAbstractAugment.getMethod("canApplyTo", AbstractCard.class);
            
    //         for (AbstractCard c : cards.group) {
    //             if ((boolean) m.invoke(cardModifier, c)) {
    //                 newCards.addToBottom(c);
    //             }
    //         }
    //     } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
    //         e.printStackTrace();
    //         throw new RuntimeException("Error retrieving card mods from Chimera Cards", e);
    //     }
    //     return newCards;
    // }

    public static String makeModLabel(Class<?> clz) {
        StringBuilder label = new StringBuilder();
        label.setLength(0);
        for (String w : WhatMod.findModName(clz).split(" ")){
            label.append("#p").append(w).append(" ");
        }
        return label.toString().trim() + " NL ";
    }
    
}
