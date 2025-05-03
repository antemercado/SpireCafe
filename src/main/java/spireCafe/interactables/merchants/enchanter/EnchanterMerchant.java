package spireCafe.interactables.merchants.enchanter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;

import basemod.ReflectionHacks;
import basemod.abstracts.AbstractCardModifier;
import basemod.animations.SpriterAnimation;
import basemod.helpers.CardModifierManager;
import spireCafe.Anniv7Mod;
import spireCafe.abstracts.AbstractArticle;
import spireCafe.abstracts.AbstractMerchant;
import spireCafe.cardmods.AutoplayMod;
import spireCafe.cardmods.BlessedMod;
import spireCafe.cardmods.TransientMod;
import spireCafe.interactables.merchants.secretshop.IdentifyArticle;
import spireCafe.interactables.merchants.secretshop.SecretShopMerchant;
import spireCafe.util.TexLoader;

public class EnchanterMerchant extends AbstractMerchant {

    private static final float SCALE = 0.75F;
    private static final float HB_Y = 160.0F / SCALE;
    private static final float HB_X = 160.0F / SCALE;

    public enum ModifierRarity {
        COMMON, UNCOMMON, RARE, SPECIAL
    }

    private static final float TOP_ROW_Y = 760.0F * Settings.yScale;
    private static final float BOTTOM_ROW_Y = 337.0F * Settings.yScale;
    private static final float DRAW_START_X = Settings.WIDTH * 0.16F;
    
    private static final String ID = EnchanterMerchant.class.getSimpleName();
    private static final CharacterStrings characterStrings = CardCrawlGame.languagePack.getCharacterString(Anniv7Mod.makeID(ID));
    private static final String[] TEXT = characterStrings.TEXT;
    private static final Texture RUG_TEXTURE = TexLoader.getTexture(Anniv7Mod.makeMerchantPath("enchanter/rug.png"));
    private static final String MERCHANT_STR = Anniv7Mod.makeMerchantPath("enchanter/idle/");

    private static final float PITCH_VAR = 0.8F;

    public ArrayList<AbstractCard> cards = new ArrayList<>();
    public ArrayList<AbstractRelic> relics = new ArrayList<>();
    public ArrayList<AbstractPotion> potions = new ArrayList<>();
    public boolean identifyMode;
    public IdentifyArticle idArticle;

    private ArrayList<AbstractCardModifier> commonMods = new ArrayList<>();
    private ArrayList<AbstractCardModifier> uncommonMods = new ArrayList<>();
    private ArrayList<AbstractCardModifier> rareMods = new ArrayList<>();
    private ArrayList<AbstractCardModifier> specialMods = new ArrayList<>();
    private Class<?> chimera;
    private Class<?> chimeraAbstractAugment;

    private Class<?> manaSurgeZone;
    
    public static final Logger logger = LogManager.getLogger("EnchanterMerchant");

    public EnchanterMerchant(float animationX, float animationY) {
        super(animationX - (20f * Settings.xScale), animationY, HB_X, HB_Y);
        this.name = characterStrings.NAMES[0];
        this.authors = "Coda";
        this.background = new TextureRegion(RUG_TEXTURE);
        loadAnimation(MERCHANT_STR + "skeleton.atlas", MERCHANT_STR + "skeleton.json", SCALE);
        this.state.setAnimation(0, "Sprite", true);
    }

    @Override
    protected void rollShop() {
        loadDefaultMods();
        if (Loader.isModLoaded("CardAugments")) {
            loadChimeraCardModifiers();
        }
        if (Loader.isModLoaded("anniv6")) {
            loadAnniv6Modifiers();
        }
        if (Loader.isModLoaded("anniv5")) {
            loadAnniv5Modifiers();
        }
        if (Loader.isModLoaded("expansionPacks")) {
            loadAnniv5ExpansionModifiers();
        }

        Collections.shuffle(commonMods);
        Collections.shuffle(uncommonMods);
        articles.add(new EnchanterArticle(this, commonMods.get(0), ModifierRarity.COMMON, "Test", 500, 500));
        articles.add(new EnchanterArticle(this, uncommonMods.get(0), ModifierRarity.COMMON, "Test", 200, 200));
    }

    // Enchanter Article will delete itself
    @Override
    public void onBuyArticle(AbstractArticle article) {
    }

    @Override
    public void onInteract() {
        super.onInteract();
        // for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
        //     CardModifierManager.addModifier(c, commonMods.get(0));
        //     showChangedCard(c);
        // }

        // if (chimeraAbstractAugment.isAssignableFrom(commonMods.get(0).getClass())) {
        //     try {
        //         Method m = commonMods.get(0).getClass().getMethod("canApplyTo", AbstractCard.class);
        //         for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
        //             boolean x = (boolean) m.invoke(commonMods.get(0), c);
        //             logger.info("(boolean) m.invoke(commonMods.get(0), c): " + x);
        //             if (x) {
        //                 CardModifierManager.addModifier(c, commonMods.get(0).makeCopy());
        //                 showChangedCard(c);
        //             }
        //         }
        //     } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
        //         e.printStackTrace();
        //     }
        // }

    }
    
    private void loadDefaultMods() {
        // commonMods.add(null);

        // uncommonMods.add(new BlessedMod());

        // rareMods.add(new TransientMod());
        // rareMods.add(new AutoplayMod());
    }
    private void loadChimeraCardModifiers() {
        try {
            chimera = Class.forName("CardAugments.CardAugmentsMod");
            chimeraAbstractAugment = Class.forName("CardAugments.cardmods.AbstractAugment");
            ArrayList<AbstractCardModifier> commonChimera = new ArrayList<>(ReflectionHacks.getPrivateStatic(chimera, "commonMods"));
            ArrayList<AbstractCardModifier> uncommonChimera = new ArrayList<>(ReflectionHacks.getPrivateStatic(chimera, "uncommonMods"));
            ArrayList<AbstractCardModifier> rareChimera = new ArrayList<>(ReflectionHacks.getPrivateStatic(chimera, "rareMods"));
            ArrayList<AbstractCardModifier> specialChimera = new ArrayList<>(ReflectionHacks.getPrivateStatic(chimera, "specialMods"));

            commonMods.addAll(commonChimera);
            uncommonMods.addAll(uncommonChimera);
            rareMods.addAll(rareChimera);
            specialMods.addAll(specialChimera);

        } catch (ClassNotFoundException | SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving card mods from Chimera Cards", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAnniv6Modifiers() {
        try {
            manaSurgeZone = Class.forName("spireMapOverhaul.zones.manasurge.ManaSurgeZone");
            Method m = manaSurgeZone.getMethod("getPositiveCommonModifierList", boolean.class);
            Method m2 = manaSurgeZone.getMethod("getPositiveUncommonModifierList", boolean.class);
            ArrayList<AbstractCardModifier> commonMana = new ArrayList<>((ArrayList<AbstractCardModifier>)m.invoke(null,true));
            ArrayList<AbstractCardModifier> uncommonMana = new ArrayList<>((ArrayList<AbstractCardModifier>)m2.invoke(null,true));

            commonMods.addAll(commonMana);
            uncommonMods.addAll(uncommonMana);

            logger.info(commonMods);
            logger.info(uncommonMods);

        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving card mods from anniv6", e);
        }
    }

    private void loadAnniv5Modifiers() {
    }

    private void loadAnniv5ExpansionModifiers() {
    }

    private void showChangedCard(AbstractCard c) {
        float x = Settings.WIDTH * 0.5F + MathUtils.random.nextFloat() * Settings.WIDTH * 0.75F - Settings.WIDTH * 0.375F;
        float y = Settings.HEIGHT * 0.5F + MathUtils.random.nextFloat() * Settings.HEIGHT * 0.35F - Settings.HEIGHT * 0.175F;
        AbstractDungeon.topLevelEffectsQueue.add(new ShowCardBrieflyEffect(c.makeStatEquivalentCopy(), x, y));
    }
    
}
