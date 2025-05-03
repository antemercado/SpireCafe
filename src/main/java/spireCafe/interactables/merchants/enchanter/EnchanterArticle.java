package spireCafe.interactables.merchants.enchanter;

import static spireCafe.patches.CafeEntryExitPatch.closeCurrentScreen;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.red.IronWave;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;

import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import basemod.helpers.TooltipInfo;
import spireCafe.Anniv7Mod;
import spireCafe.abstracts.AbstractArticle;
import spireCafe.abstracts.AbstractMerchant;
import spireCafe.cardmods.TransientMod;
import spireCafe.interactables.merchants.enchanter.EnchanterMerchant.ModifierRarity;
import spireCafe.util.TexLoader;


public class EnchanterArticle extends AbstractArticle {    
    
    public static final Logger logger = LogManager.getLogger("EnchanterArticle");

    private static final Texture TEXTURE = TexLoader.getTexture(Anniv7Mod.makeMerchantPath("enchanter/scroll.png"));

    private static final String ID = Anniv7Mod.makeID(EnchanterArticle.class.getSimpleName());

    private static AbstractCard tooltipBuddy = new IronWave();
    
    public AbstractCardModifier modifier;
    private ModifierRarity rarity;
    private String name;

    private ArrayList<PowerTip> powerTips = new ArrayList<PowerTip>();

    private List<TooltipInfo> tooltipInfo;

    public boolean isPurchased = false;

    public EnchanterArticle(AbstractMerchant merchant, AbstractCardModifier modifier, ModifierRarity rarity, String name, float x, float y) {
        super(ID, merchant, x, y, TEXTURE);
        this.modifier = modifier;
        this.rarity = rarity;
        this.name = name;
        this.tooltipInfo = modifier.additionalTooltips(EnchanterArticle.tooltipBuddy);
    }

    @Override
    public String getTipHeader() {
        logger.info(tooltipInfo.get(0).title);
        return tooltipInfo.get(0).title;
    }
    
    @Override
    public String getTipBody() {
        logger.info(tooltipInfo.get(0).description);
        return tooltipInfo.get(0).description;
    }


    @Override
    public boolean canBuy() {
        return AbstractDungeon.player.gold > getModifiedPrice();
    }

    @Override
    public void onBuy() {
        CardGroup cards = getValidEnchantableCards();
        AbstractDungeon.player.loseGold(getModifiedPrice());
        AbstractDungeon.topLevelEffectsQueue.add(new EnchantCardEffect(cards, this));
    }

    private CardGroup getValidEnchantableCards() {
        CardGroup cards = AbstractDungeon.player.masterDeck;
        return cards;
    }

    @Override
    public void update() {
        super.update();        
    }

    @Override
    public int getBasePrice() {
        return 10;
    }
    
    private void showChangedCard(AbstractCard c) {
        float x = Settings.WIDTH * 0.5F + MathUtils.random.nextFloat() * Settings.WIDTH * 0.75F - Settings.WIDTH * 0.375F;
        float y = Settings.HEIGHT * 0.5F + MathUtils.random.nextFloat() * Settings.HEIGHT * 0.35F - Settings.HEIGHT * 0.175F;
        AbstractDungeon.topLevelEffectsQueue.add(new ShowCardBrieflyEffect(c.makeStatEquivalentCopy(), x, y));
    }
}
