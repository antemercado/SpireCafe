package spireCafe.interactables.merchants.enchanter.enchantments.dynamic;

import basemod.abstracts.AbstractCardModifier;
import spireCafe.interactables.merchants.enchanter.AbstractEnchantment;
import spireCafe.interactables.merchants.enchanter.EnchanterMerchant.EnchantmentRarity;

public class ManaSurgeEnchantment extends AbstractEnchantment{

    public ManaSurgeEnchantment(AbstractCardModifier cardModifier, EnchantmentRarity rarity) {
        super(cardModifier, rarity);
    }

    @Override
    public String getName() {
        return cardModifier.additionalTooltips(tooltipBuddy).get(0).title;
    }

    @Override
    public String getDescription() {
        return cardModifier.additionalTooltips(tooltipBuddy).get(0).description;
    }
    
}
