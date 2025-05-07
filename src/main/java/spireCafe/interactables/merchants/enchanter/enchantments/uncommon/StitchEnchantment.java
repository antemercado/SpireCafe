package spireCafe.interactables.merchants.enchanter.enchantments.uncommon;

import basemod.BaseMod;
import spireCafe.interactables.merchants.enchanter.AbstractEnchantment;
import spireCafe.interactables.merchants.enchanter.EnchanterMerchant.ModifierRarity;
import spireCafe.interactables.merchants.enchanter.modifiers.StitchMod;

public class StitchEnchantment extends AbstractEnchantment {

    public StitchEnchantment() {
        super(new StitchMod(), ModifierRarity.UNCOMMON);
    }

    @Override
    public String getName() {
        return BaseMod.getKeywordTitle("anniv5:stitch");
    }

    @Override
    public String getDescription() {
        return makeModLabel(this.cardModifier.getClass()) + BaseMod.getKeywordDescription("anniv5:stitch");
    }
    
}
