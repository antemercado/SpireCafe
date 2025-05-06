package spireCafe.interactables.merchants.enchanter.enchantments.uncommon;

import basemod.BaseMod;
import spireCafe.cardmods.BlessedMod;
import spireCafe.interactables.merchants.enchanter.AbstractEnchantment;
import spireCafe.interactables.merchants.enchanter.EnchanterMerchant.ModifierRarity;

public class BlessedEnchantment extends AbstractEnchantment{

    public BlessedEnchantment() {
        super(new BlessedMod(), ModifierRarity.UNCOMMON);
    }

    @Override
    public String getName() {
        return BaseMod.getKeywordTitle("anniv7:Blessed");
    }

    @Override
    public String getDescription() {
        return makeModLabel(this.cardModifier.getClass()) + BaseMod.getKeywordDescription("anniv7:Blessed");
    }
    
}
