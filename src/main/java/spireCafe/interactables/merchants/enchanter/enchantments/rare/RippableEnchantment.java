package spireCafe.interactables.merchants.enchanter.enchantments.rare;

import java.lang.reflect.InvocationTargetException;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import spireCafe.interactables.merchants.enchanter.AbstractEnchantment;
import spireCafe.interactables.merchants.enchanter.EnchanterMerchant.ModifierRarity;
import spireCafe.interactables.merchants.enchanter.modifiers.RipMod;

public class RippableEnchantment extends AbstractEnchantment {

    public RippableEnchantment() {
        super(null, ModifierRarity.RARE);
            cardModifier = new RipMod();
    }

    @Override
    public String getName() {
        return BaseMod.getKeywordTitle("anniv5:rippable");
    }

    @Override
    public String getDescription() {
        return makeModLabel(this.cardModifier.getClass()) + BaseMod.getKeywordDescription("anniv5:rippable");
    }
    
}
