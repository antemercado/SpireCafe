package spireCafe.interactables.merchants.enchanter;

import static spireCafe.patches.CafeEntryExitPatch.closeCurrentScreen;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.CardGroup.CardGroupType;
import com.megacrit.cardcrawl.cards.red.IronWave;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
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

    private static final String TEXTURE_PATH = Anniv7Mod.makeMerchantPath("enchanter/scrolls/%s.png");

    private static final String ID = Anniv7Mod.makeID(EnchanterArticle.class.getSimpleName());

    private static final float PRICE_OFFSET = 85f;
    private static final float LABEL_OFFSET = 25f;
    private static final float LABEL_OFFSET_X = 11f;

    protected static AbstractCard tooltipBuddy = new IronWave();
    
    public AbstractCardModifier modifier;
    private ModifierRarity rarity;
    private String name;

    private ArrayList<PowerTip> powerTips = new ArrayList<PowerTip>();

    private List<TooltipInfo> tooltipInfo;

    public boolean isPurchased = false;

    public EnchanterArticle(AbstractMerchant merchant, AbstractCardModifier modifier, ModifierRarity rarity) {
        this(merchant, modifier, rarity, 0, 0);
    }

    public EnchanterArticle(AbstractMerchant merchant, AbstractCardModifier modifier, ModifierRarity rarity, float x, float y) {
        super(ID, merchant, x, y, TexLoader.getTexture((String.format(TEXTURE_PATH, getScrollNumber(modifier)))));
        logger.info(String.format(TEXTURE_PATH, getScrollNumber(modifier)));
        this.modifier = modifier;
        this.rarity = rarity;
        this.tooltipInfo = modifier.additionalTooltips(EnchanterArticle.tooltipBuddy);
        this.name = getTipHeader();
    }

    private static int getScrollNumber(AbstractCardModifier modifier) {
        return Math.floorMod(modifier.getClass().getName().hashCode(), 12) + 1;
    }

    @Override
    public String getTipHeader() {
        if (this.tooltipInfo == null) {
            return modifier.getClass().getSimpleName();
        }
        return tooltipInfo.get(0).title;
    }
    
    @Override
    public String getTipBody() {
        if (this.tooltipInfo == null) {
            return "";
        }
        return tooltipInfo.get(0).description;
    }


    @Override
    public boolean canBuy() {
        return (AbstractDungeon.player.gold > getModifiedPrice() && getValidEnchantableCards().size() != 0);
    }

    @Override
    public void onBuy() {
        CardGroup cards = getValidEnchantableCards();
        AbstractDungeon.player.loseGold(getModifiedPrice());
        AbstractDungeon.topLevelEffectsQueue.add(new EnchantCardEffect(cards, this));
    }

    protected CardGroup getValidEnchantableCards() {
        CardGroup cards = new CardGroup(CardGroupType.UNSPECIFIED);
        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if (CardModifierManager.modifiers(c).isEmpty()) {
                if (modifier.shouldApply(c)) {
                    cards.addToTop(c);
                }
            }
        }
        return cards;
    }

    @Override
    public void update() {
        super.update();        
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        renderLabel(sb);
    }

    @Override
    public int getBasePrice() {
        return 10;
    }

    public void updateXY(float x, float y) {
        xPos = x - (this.itemTexture.getRegionWidth() / 2);
        yPos = y - (this.itemTexture.getRegionHeight() / 2);
    }

    public void renderLabel(SpriteBatch sb) {
        float priceX = xPos + hb.width/2f + (LABEL_OFFSET_X * scale);
        float priceY = yPos - LABEL_OFFSET * scale;
        FontHelper.renderFontCentered(sb, FontHelper.tipHeaderFont, String.valueOf(this.name), priceX, priceY, canBuy()? Color.WHITE : Color.SALMON);
        hb.render(sb);
    }

    @Override
    public void renderPrice(SpriteBatch sb) {
        int price = getModifiedPrice();
        float priceX = xPos + hb.width/2f + (LABEL_OFFSET_X * scale);
        float priceY = yPos - PRICE_OFFSET * scale;
        float textLength = FontHelper.getWidth(FontHelper.tipHeaderFont, String.valueOf(price), scale);
        if (getPriceIcon() != null) {
            float lineStart = priceX - (textLength + getPriceIcon().getWidth() * scale)/2f;
            sb.draw(getPriceIcon(), lineStart, priceY, getPriceIcon().getWidth() * scale, getPriceIcon().getHeight() * scale);
            FontHelper.renderFont(sb, FontHelper.tipHeaderFont, String.valueOf(price), lineStart + getPriceIcon().getWidth() * scale, priceY + getPriceIcon().getHeight()/2f, canBuy()? Color.WHITE : Color.SALMON);
        } else {
            FontHelper.renderFontCentered(sb, FontHelper.tipHeaderFont, String.valueOf(price), priceX, priceY, canBuy()? Color.WHITE : Color.SALMON);
        }
        hb.render(sb);
    }
}
