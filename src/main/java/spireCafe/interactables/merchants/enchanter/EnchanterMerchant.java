package spireCafe.interactables.merchants.enchanter;

import static spireCafe.Anniv7Mod.makeID;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.IronWave;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.abstracts.AbstractCardModifier;
import basemod.patches.whatmod.WhatMod;
import spireCafe.Anniv7Mod;
import spireCafe.abstracts.AbstractArticle;
import spireCafe.abstracts.AbstractMerchant;
import spireCafe.cardmods.BlessedMod;
import spireCafe.cardmods.TransientMod;
import spireCafe.interactables.merchants.HelpArticle;
import spireCafe.interactables.merchants.secretshop.IdentifyArticle;
import spireCafe.util.TexLoader;
import spireCafe.util.cutsceneStrings.LocalizedCutsceneStrings;

public class EnchanterMerchant extends AbstractMerchant {

    private static final float SCALE = 0.75F;
    private static final float HB_Y = 160.0F / SCALE;
    private static final float HB_X = 160.0F / SCALE;

    public enum ModifierRarity {
        COMMON, UNCOMMON, RARE, SPECIAL
    }

    public enum EnchantSister {
        WHITE, BLUE, RED
    }
    private static final float TOP_ROW_Y = 750.0F * Settings.yScale;
    private static final float BOTTOM_ROW_Y = 300.0F * Settings.yScale;
    private static final float DRAW_START_X = 400.0F * Settings.xScale;
    private static final float DRAW_OFFSET_X = 300.0F * Settings.xScale;
    private static final float HELP_X = 1250f * Settings.xScale;
    private static final float HELP_Y = 800f * Settings.yScale;

    private static final float BLUE_BUBBLE_X = 0;
    private static final float BLUE_BUBBLE_Y = 0;
    private static final float RED_BUBBLE_X = 0;
    private static final float RED_BUBBLE_Y = 0;
    private static final float WHITE_BUBBLE_X = 0;
    private static final float WHITE_BUBBLE_Y = 0;
    
    private static final String ID = EnchanterMerchant.class.getSimpleName();
    private static final CharacterStrings characterStrings = CardCrawlGame.languagePack.getCharacterString(Anniv7Mod.makeID(ID));
    private static final String[] TEXT = characterStrings.TEXT;
    private static final Texture RUG_TEXTURE = TexLoader.getTexture(Anniv7Mod.makeMerchantPath("enchanter/rug.png"));
    private static final String MERCHANT_STR = Anniv7Mod.makeMerchantPath("enchanter/idle/");

    private static final float PITCH_VAR = 0.8F;

    private ArrayList<Enchantments> commonEnchantments = new ArrayList<>();
    private ArrayList<Enchantments> uncommonEnchantments = new ArrayList<>();
    private ArrayList<Enchantments> rareEnchantments = new ArrayList<>();
    private ArrayList<Enchantments> specialEnchantments = new ArrayList<>();
    private Class<?> chimera;
    private Class<?> chimeraAbstractAugment;

    private Class<?> manaSurgeZone;
    private Random miscRng = AbstractDungeon.miscRng;
    private AbstractCard tooltipBuddy = new IronWave();
    
    public static final Logger logger = LogManager.getLogger("EnchanterMerchant");
    
    public EnchanterMerchant(float animationX, float animationY) {
        super(animationX - (20f * Settings.xScale), animationY, HB_X, HB_Y);
        this.name = characterStrings.NAMES[0];
        this.authors = "Coda";
        this.background = new TextureRegion(RUG_TEXTURE);
        loadAnimation(MERCHANT_STR + "skeleton.atlas", MERCHANT_STR + "skeleton.json", SCALE);
        this.state.setAnimation(0, "Sprite", true);
    }

    // EnchantCardEffect will delete the article
    @Override
    public void onBuyArticle(AbstractArticle article) {}

    @Override
    protected void rollShop() {

        loadDefaultMods();

        if (Loader.isModLoaded("CardAugments")) {
            loadChimeraCardModifiers();
        }
        if (Loader.isModLoaded("anniv6")) {
            loadAnniv6Modifiers();
        }

        for (int i = 0; i < 6; i++) {
            ModifierRarity rarity;
            if (i < 3) {
                rarity = miscRng.randomBoolean(0.5F) ? ModifierRarity.COMMON : ModifierRarity.UNCOMMON;
            } else {
                rarity = miscRng.randomBoolean(0.75F) ? ModifierRarity.UNCOMMON : ModifierRarity.RARE;
            }
            Enchantments enchantment = getEnchantmentFromRarity(rarity);

            EnchanterArticle articleToAdd;

            articleToAdd = new EnchanterArticle(this, enchantment);

            if (i < 3) {
                articleToAdd.updateXY(DRAW_START_X + ((i % 3) * DRAW_OFFSET_X), TOP_ROW_Y);
            } else {
                articleToAdd.updateXY(DRAW_START_X + ((i % 3) * DRAW_OFFSET_X), BOTTOM_ROW_Y);
            }
            this.articles.add(articleToAdd);
        }

        this.articles.add(new HelpArticle(this, HELP_X, HELP_Y, characterStrings.OPTIONS[0], characterStrings.OPTIONS[1]));
    }

    private void addModToEnchantments(AbstractCardModifier mod, ModifierRarity rarity, String name, String description) {
        Enchantments enchant = new Enchantments(mod, rarity, name, description);
        switch (rarity) {
            case COMMON:
                commonEnchantments.add(enchant);
                break;
            case UNCOMMON:
                uncommonEnchantments.add(enchant);
                break;
            case RARE:
                rareEnchantments.add(enchant);
                break;
            default:
                specialEnchantments.add(enchant);
                break;
        }
    }

    private Enchantments getEnchantmentFromRarity(ModifierRarity rarity) {

        switch (rarity) {
            case COMMON:
                Collections.shuffle(commonEnchantments, new java.util.Random(miscRng.random.nextLong()));
                return commonEnchantments.get(0);
            case UNCOMMON:
                Collections.shuffle(uncommonEnchantments, new java.util.Random(miscRng.random.nextLong()));    
                return uncommonEnchantments.get(0);
            case RARE:
                Collections.shuffle(rareEnchantments, new java.util.Random(miscRng.random.nextLong()));    
                return rareEnchantments.get(0);
            default:
                Collections.shuffle(specialEnchantments, new java.util.Random(miscRng.random.nextLong()));
                return specialEnchantments.get(0);
        }
    }
    
    private void loadDefaultMods() {

        String modLabel = makeModLabel(this.getClass());

        addModToEnchantments(new TransientMod(), ModifierRarity.UNCOMMON,
             BaseMod.getKeywordTitle("anniv7:Blessed"), modLabel + BaseMod.getKeywordDescription("anniv7:Blessed"));

        String[] transientStr = LocalizedCutsceneStrings.getCutsceneStrings(makeID("TheTransientCutscene")).OPTIONS;
        addModToEnchantments(new TransientMod(), ModifierRarity.RARE, transientStr[2], modLabel + transientStr[3]);
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
                                commonEnchantments.add(new Enchantments(mod, ModifierRarity.COMMON, getChimeraName(mod), getChimeraDescription(mod), true));
                                break;
                            case "Uncommon":
                                uncommonEnchantments.add(new Enchantments(mod, ModifierRarity.UNCOMMON, getChimeraName(mod), getChimeraDescription(mod), true));
                                break;
                            case "Rare":
                                rareEnchantments.add(new Enchantments(mod, ModifierRarity.RARE, getChimeraName(mod), getChimeraDescription(mod), true));
                                break;
                            default:
                                specialEnchantments.add(new Enchantments(mod, ModifierRarity.SPECIAL, getChimeraName(mod), getChimeraDescription(mod), true));
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

    private String getChimeraDescription(AbstractCardModifier modifier) {
        String body = "";
        try {
            chimeraAbstractAugment = Class.forName("CardAugments.cardmods.AbstractAugment");
            Method m = chimeraAbstractAugment.getMethod("getAugmentDescription");
            body = makeModLabel(modifier.getClass()) + " NL " + m.invoke(modifier);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return body;
        }
        if (body.endsWith(" NL ")) {
            body = body.substring(0, body.length()-4);
        }
        return body;
    }

    private String getChimeraName(AbstractCardModifier modifier) {
        String header = "";
        try {
            chimeraAbstractAugment = Class.forName("CardAugments.cardmods.AbstractAugment");
            Method m = chimeraAbstractAugment.getMethod("modifyName", String.class, AbstractCard.class);
            header = ((String) m.invoke(modifier, "", tooltipBuddy)).replace("  ", " ").trim();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        if (header.isEmpty()) {
            header = modifier.getClass().getSimpleName();
        }
        return header;
    } 

    @SuppressWarnings("unchecked")
    private void loadAnniv6Modifiers() {
        try {
            manaSurgeZone = Class.forName("spireMapOverhaul.zones.manasurge.ManaSurgeZone");
            Method m = manaSurgeZone.getMethod("getPositiveCommonModifierList", boolean.class);
            Method m2 = manaSurgeZone.getMethod("getPositiveUncommonModifierList", boolean.class);
            ArrayList<AbstractCardModifier> commonMana = new ArrayList<>((ArrayList<AbstractCardModifier>)m.invoke(null,true));
            ArrayList<AbstractCardModifier> uncommonMana = new ArrayList<>((ArrayList<AbstractCardModifier>)m2.invoke(null,true));

            for (AbstractCardModifier mod : commonMana) {
                addModToEnchantments(mod, ModifierRarity.COMMON, mod.additionalTooltips(tooltipBuddy).get(0).title,
                    mod.additionalTooltips(tooltipBuddy).get(0).description);
            }
            for (AbstractCardModifier mod : uncommonMana) {
                addModToEnchantments(mod, ModifierRarity.UNCOMMON, mod.additionalTooltips(tooltipBuddy).get(0).title,
                    mod.additionalTooltips(tooltipBuddy).get(0).description);
            }

        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving card mods from anniv6", e);
        }
    }


    private String makeModLabel(Class<?> clz) {
        StringBuilder label = new StringBuilder();
        label.setLength(0);
        for (String w : WhatMod.findModName(clz).split(" ")){
            label.append("#p").append(w).append(" ");
        }
        return label.toString().trim() + " NL ";
    }

    public void enchanterSpeech(String message, EnchantSister which) {
        float msgX;
        float msgY;

        switch (which) {
            case BLUE:
                msgX = BLUE_BUBBLE_X;
                msgY = BLUE_BUBBLE_Y;
                break;
            case RED:
                msgX = RED_BUBBLE_X;
                msgY = RED_BUBBLE_Y;
                break;
            case WHITE:
                msgX = WHITE_BUBBLE_X;
                msgY = WHITE_BUBBLE_Y;
                break;
        }


    }
}
