package spireCafe.interactables.merchants.enchanter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.CardGroup.CardGroupType;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import spireCafe.interactables.merchants.enchanter.EnchanterMerchant.ModifierRarity;

public class Enchantments {
    public AbstractCardModifier cardModifier;
    public ModifierRarity rarity;
    public String name;
    public String description;
    private Class<?> chimeraAbstractAugment;
    private boolean isChimera;

    
    public Enchantments(AbstractCardModifier cardModifier, ModifierRarity rarity) {
        this.cardModifier = cardModifier;
        this.rarity = rarity;
        this.name = cardModifier.getClass().getSimpleName();
        this.description = "";
    }
    
    public Enchantments(AbstractCardModifier cardModifier, ModifierRarity rarity, String name, String description) {
        this(cardModifier, rarity, name, description, false);
    }

    public Enchantments(AbstractCardModifier cardModifier, ModifierRarity rarity, String name, String description, boolean isChimera) {
        this(cardModifier, rarity);
        this.name = name;
        this.description = description;
        this.isChimera = isChimera;
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

        if (isChimera) {
            return getValidChimeraCards(cards);
        } else {
            return cards;
        }
    }

    public CardGroup getValidChimeraCards(CardGroup cards) {
        CardGroup newCards = new CardGroup(CardGroupType.UNSPECIFIED);
        try {
            chimeraAbstractAugment = Class.forName("CardAugments.cardmods.AbstractAugment");
            Method m = chimeraAbstractAugment.getMethod("canApplyTo", AbstractCard.class);
            
            for (AbstractCard c : cards.group) {
                if ((boolean) m.invoke(cardModifier, c)) {
                    newCards.addToBottom(c);
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving card mods from Chimera Cards", e);
        }
        return newCards;
    }
    
}
