package spireCafe.interactables.merchants.enchanter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import basemod.ReflectionHacks;
import basemod.abstracts.AbstractCardModifier;
import spireCafe.Anniv7Mod;
import spireCafe.abstracts.AbstractArticle;
import spireCafe.abstracts.AbstractMerchant;
import spireCafe.cardmods.BlessedMod;
import spireCafe.cardmods.TransientMod;
import spireCafe.interactables.merchants.enchanter.enchantermods.MagneticMod;
import spireCafe.interactables.merchants.secretshop.IdentifyArticle;
import spireCafe.util.TexLoader;

public class EnchanterMerchant extends AbstractMerchant {

    private static final float SCALE = 0.75F;
    private static final float HB_Y = 160.0F / SCALE;
    private static final float HB_X = 160.0F / SCALE;

    public enum ModifierRarity {
        COMMON, UNCOMMON, RARE, SPECIAL
    }

    private static final float TOP_ROW_Y = 800.0F * Settings.yScale;
    private static final float BOTTOM_ROW_Y = 330.0F * Settings.yScale;
    private static final float DRAW_START_X = 400.0F * Settings.xScale;
    private static final float DRAW_OFFSET_X = 300.0F * Settings.xScale;
    
    
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
    private Random miscRng = AbstractDungeon.miscRng;
    
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

        // loadDefaultMods();

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

        ;

        for (int i = 0; i < 6; i++) {
            ModifierRarity rarity;
            if (i < 3) {
                rarity = miscRng.randomBoolean(0.5F) ? ModifierRarity.COMMON : ModifierRarity.UNCOMMON;
            } else {
                rarity = miscRng.randomBoolean(0.75F) ? ModifierRarity.UNCOMMON : ModifierRarity.RARE;
            }
            AbstractCardModifier modifier = getModifierFromRarity(rarity);

            EnchanterArticle articleToAdd;

            //TODO: Remove            
            try {
                Class<?> testReflect = Class.forName("thePackmaster.cardmodifiers.rippack.RippableModifier");
                modifier = (AbstractCardModifier) testReflect.getConstructor().newInstance();
            } catch (IllegalArgumentException | SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }

            if (chimeraAbstractAugment != null && chimeraAbstractAugment.isAssignableFrom(modifier.getClass())) {
                articleToAdd = new ChimeraEnchanterArticle(this, modifier, rarity);
            } else {
                articleToAdd = new EnchanterArticle(this, modifier, rarity);
            }

            if (i < 3) {
                articleToAdd.updateXY(DRAW_START_X + ((i % 3) * DRAW_OFFSET_X), TOP_ROW_Y);
            } else {
                articleToAdd.updateXY(DRAW_START_X + ((i % 3) * DRAW_OFFSET_X), BOTTOM_ROW_Y);
            }
            this.articles.add(articleToAdd);
        }
    }

    private AbstractCardModifier getModifierFromRarity(ModifierRarity rarity) {
        switch (rarity) {
            case COMMON:
                Collections.shuffle(commonMods, new java.util.Random(miscRng.random.nextLong()));
                return commonMods.get(0);
            case UNCOMMON:
                Collections.shuffle(uncommonMods, new java.util.Random(miscRng.random.nextLong()));    
                return uncommonMods.get(0);
            case RARE:
                Collections.shuffle(rareMods, new java.util.Random(miscRng.random.nextLong()));    
                return rareMods.get(0);
            default:
                Collections.shuffle(specialMods, new java.util.Random(miscRng.random.nextLong()));
                return specialMods.get(0);
        }
    }

    // EnchantCardEffect will delete the article
    @Override
    public void onBuyArticle(AbstractArticle article) {
    }
    
    private void loadDefaultMods() {
        // commonMods.add(null);

        uncommonMods.add(new BlessedMod());
        uncommonMods.add(new BlessedMod());
        uncommonMods.add(new BlessedMod());

        rareMods.add(new TransientMod());
        rareMods.add(new TransientMod());
        rareMods.add(new TransientMod());
        // rareMods.add(new AutoplayMod());
    }
    private void loadChimeraCardModifiers() {
        try {
            chimera = Class.forName("CardAugments.CardAugmentsMod");
            chimeraAbstractAugment = Class.forName("CardAugments.cardmods.AbstractAugment");
            ArrayList<AbstractCardModifier> allChimera = new ArrayList<>();
            allChimera.addAll(ReflectionHacks.getPrivateStatic(chimera, "commonMods"));
            allChimera.addAll(ReflectionHacks.getPrivateStatic(chimera, "uncommonMods"));
            allChimera.addAll(ReflectionHacks.getPrivateStatic(chimera, "rareMods"));
            allChimera.addAll(ReflectionHacks.getPrivateStatic(chimera, "specialMods"));

            Method m = chimeraAbstractAugment.getMethod("canApplyTo", AbstractCard.class);
            for (AbstractCardModifier mod: allChimera) {
                for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                    if ((boolean) m.invoke(mod, c)) {
                        Method m2 = chimeraAbstractAugment.getMethod("getModRarity");
                        switch (m2.invoke(mod).toString()){
                            case "Common":
                                commonMods.add(mod);
                                break;
                            case "Uncommon":
                                uncommonMods.add(mod);
                                break;
                            case "Rare":
                                rareMods.add(mod);
                                break;
                            default:
                                specialMods.add(mod);
                        }
                        break;
                    }
                }
            }

        } catch (ClassNotFoundException | SecurityException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
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

        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving card mods from anniv6", e);
        }
    }

    private void loadAnniv5Modifiers() {
        try {
            Class<?> rippableModClass = Class.forName("thePackmaster.cardmodifiers.rippack.RippableModifier");

            rareMods.add((AbstractCardModifier) rippableModClass.getConstructor().newInstance());
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving card mods from anniv5", e);
        }
    }

    private void loadAnniv5ExpansionModifiers() {
        try {
            Class<?> magModClass = Class.forName("thePackmaster.cardmodifiers.magnetizepack.MagnetizedModifier");
            
            uncommonMods.add((AbstractCardModifier) magModClass.getConstructor(boolean.class).newInstance(true));
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving card mods from expansionPacks", e);
        }
    }
    
}
