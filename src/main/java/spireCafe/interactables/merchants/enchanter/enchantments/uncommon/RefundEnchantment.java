package spireCafe.interactables.merchants.enchanter.enchantments.uncommon;

import basemod.BaseMod;
import spireCafe.cardmods.RefundMod;
import spireCafe.interactables.merchants.enchanter.AbstractEnchantment;
import spireCafe.interactables.merchants.enchanter.EnchanterMerchant.ModifierRarity;

public class RefundEnchantment extends AbstractEnchantment {

    public RefundEnchantment() {
        super(new RefundMod(1), ModifierRarity.UNCOMMON);
    }

    @Override
    public String getName() {
        return BaseMod.getKeywordTitle("refund");
    }

    @Override
    public String getDescription() {
        return makeModLabel(this.cardModifier.getClass()) + BaseMod.getKeywordDescription("refund");
    }
    
}
