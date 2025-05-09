package spireCafe.interactables.merchants.enchanter;

import static spireCafe.Anniv7Mod.makeID;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

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
import basemod.cardmods.InnateMod;
import basemod.helpers.CardModifierManager;
import basemod.patches.whatmod.WhatMod;
import spireCafe.Anniv7Mod;
import spireCafe.abstracts.AbstractArticle;
import spireCafe.abstracts.AbstractMerchant;
import spireCafe.cardmods.BlessedMod;
import spireCafe.cardmods.RetainMod;
import spireCafe.cardmods.TransientMod;
import spireCafe.interactables.merchants.HelpArticle;
import spireCafe.interactables.merchants.enchanter.enchantments.common.MagnetizeEnchantment;
import spireCafe.interactables.merchants.enchanter.enchantments.common.PersistentEnchantment;
import spireCafe.interactables.merchants.enchanter.enchantments.common.ReshuffleEnchantment;
import spireCafe.interactables.merchants.enchanter.enchantments.dynamic.ChimeraEnchantment;
import spireCafe.interactables.merchants.enchanter.enchantments.dynamic.KeywordEnchantment;
import spireCafe.interactables.merchants.enchanter.enchantments.dynamic.ManaSurgeEnchantment;
import spireCafe.interactables.merchants.enchanter.enchantments.rare.AutoplayEnchantment;
import spireCafe.interactables.merchants.enchanter.enchantments.rare.TransientEnchantment;
import spireCafe.interactables.merchants.enchanter.enchantments.uncommon.LootEnchantment;
import spireCafe.interactables.merchants.enchanter.enchantments.uncommon.NoxiousEnchantment;
import spireCafe.interactables.merchants.enchanter.enchantments.uncommon.RefundEnchantment;
import spireCafe.interactables.merchants.enchanter.modifiers.RipMod;
import spireCafe.interactables.merchants.enchanter.modifiers.StitchMod;
import spireCafe.interactables.merchants.secretshop.IdentifyArticle;
import spireCafe.util.TexLoader;
import spireCafe.util.cutsceneStrings.LocalizedCutsceneStrings;

public class EnchanterMerchant extends AbstractMerchant {

    private static final float SCALE = 0.75F;
    private static final float HB_Y = 160.0F / SCALE;
    private static final float HB_X = 160.0F / SCALE;

    public enum EnchantmentRarity {
        COMMON, UNCOMMON, RARE
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

    private ArrayList<AbstractEnchantment> commonEnchantments = new ArrayList<>();
    private ArrayList<AbstractEnchantment> uncommonEnchantments = new ArrayList<>();
    private ArrayList<AbstractEnchantment> rareEnchantments = new ArrayList<>();

    private Class<?> chimera;
    private Class<?> chimeraAbstractAugment;

    private Class<?> manaSurgeZone;
    private Random enchanterRng = new Random(AbstractDungeon.miscRng.randomLong());
    private ArrayList<AbstractCardModifier> allChimera = new ArrayList<>();
    private HashMap<String,AbstractCardModifier> chimeraMap = new HashMap<>();
    
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

    private void initializeEnchantments() {
        if (Loader.isModLoaded("CardAugments")) {
            loadChimeraCardModifiers();
        }
        if (Loader.isModLoaded("anniv6")) {
            loadAnniv6Modifiers();
        }
        if (Loader.isModLoaded("anniv5")) {
            loadAnniv5Modifiers();
            if (Loader.isModLoaded("expansionPacks")) {
                loadExpansionPackModifiers();
            }
        }
        loadDefaultModifiers();
        loadBackupModifiers();

        logger.info(commonEnchantments);
        logger.info(uncommonEnchantments);
        logger.info(rareEnchantments);

        // It's possible, but rare, that the enchantment lists can be empty because no enchantments
        // are valid for the cards in the player's deck. To prevent crashing we just load a basic
        // set of enchantments in this case. Maybe they'll aquire a valid card from other interactables!
        if (commonEnchantments.isEmpty()){
            commonEnchantments.add(new PersistentEnchantment());
            commonEnchantments.add(new ReshuffleEnchantment());
        }
        if (uncommonEnchantments.isEmpty()){
            uncommonEnchantments.add(new RefundEnchantment());
            uncommonEnchantments.add(new RefundEnchantment());
        }
        if (rareEnchantments.isEmpty()){
            rareEnchantments.add(new TransientEnchantment());
            rareEnchantments.add(new AutoplayEnchantment());
        }
    }

    @Override
    protected void rollShop() {
        
        initializeEnchantments();

        for (int i = 0; i < 6; i++) {
            EnchantmentRarity rarity;
            if (i == 0) {
                rarity = EnchantmentRarity.COMMON;
            } else if (0 < i  && i < 3) {
                rarity = enchanterRng.randomBoolean(0.5F) ? EnchantmentRarity.COMMON : EnchantmentRarity.UNCOMMON;
            } else if (3 < i && i < 5) {
                rarity = enchanterRng.randomBoolean(0.75F) ? EnchantmentRarity.UNCOMMON : EnchantmentRarity.RARE;
            } else {
                rarity = EnchantmentRarity.RARE;
            }
            AbstractEnchantment enchantment = getEnchantmentFromRarity(rarity);

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

    private void addEnchantmentToList(AbstractEnchantment enchantment) {
        if (enchantment.getValidCards().isEmpty()) {
            return;
        }
        switch (enchantment.rarity) {
            case COMMON:
                commonEnchantments.add(enchantment);
                break;
            case UNCOMMON:
                uncommonEnchantments.add(enchantment);
                break;
            case RARE:
                rareEnchantments.add(enchantment);
                break;
        }
    }

    private AbstractEnchantment getEnchantmentFromRarity(EnchantmentRarity rarity) {

        // return new MagnetizeEnchantment();

        switch (rarity) {
            default:
                Collections.shuffle(commonEnchantments, new java.util.Random(enchanterRng.random.nextLong()));
                return commonEnchantments.get(0);
            case UNCOMMON:
                Collections.shuffle(uncommonEnchantments, new java.util.Random(enchanterRng.random.nextLong()));
                return uncommonEnchantments.get(0);
            case RARE:
                Collections.shuffle(rareEnchantments, new java.util.Random(enchanterRng.random.nextLong()));
                return rareEnchantments.get(0);
        }
    }
    
    private void loadDefaultModifiers() {
        // Common
        addEnchantmentToList(new PersistentEnchantment());

        // Uncommon
        addEnchantmentToList(new KeywordEnchantment(new BlessedMod(), EnchantmentRarity.UNCOMMON, "anniv7:Blessed"));
        addEnchantmentToList(new RefundEnchantment());
        // Rare
        addEnchantmentToList(new TransientEnchantment());
    }

    private void loadBackupModifiers() {
        // These enchantments are duplicates of modifiers you'd find in Chimera Cards.
        // As such, we're only loading them if the mod and/or modifier isnt't enabled.
        // This makes sure we have the basic effects we would expect to see without duplicates.

        // Common
        if (!chimeraModEnabled("CardAugments.cardmods.common.BootMod")) {
            addEnchantmentToList(new KeywordEnchantment(new InnateMod(), EnchantmentRarity.COMMON, "innate")); // Boot
        }
        
        if (!chimeraModEnabled("CardAugments.cardmods.common.StickyMod")) {
            addEnchantmentToList(new KeywordEnchantment(new RetainMod(), EnchantmentRarity.COMMON, "retain")); // Sticky
        }

        if (!chimeraModEnabled("CardAugments.cardmods.common.ReshuffleMod")) {
            addEnchantmentToList(new ReshuffleEnchantment()); // Reshuffle
        }

        // Uncommon
        if (!chimeraModEnabled("CardAugments.cardmods.uncommon.PreparedMod")) {
            addEnchantmentToList(new LootEnchantment()); // Prepared
        }
        
        if (!chimeraModEnabled("CardAugments.cardmods.uncommon.NoxiousMod")) {
            addEnchantmentToList(new NoxiousEnchantment()); // Noxious
        }
        
        // // Rare
        if (!chimeraModEnabled("CardAugments.cardmods.event.AutoMod")) {
            addEnchantmentToList(new AutoplayEnchantment()); // Auto
        }
    }

    private boolean chimeraModEnabled(AbstractCardModifier mod) {
        if (!Loader.isModLoaded("CardAugments") || chimeraMap.isEmpty()) {
            return false;
        }
        return chimeraModEnabled(mod.getClass().getName());   
    }

    private boolean chimeraModEnabled(String string) {
        if (!Loader.isModLoaded("CardAugments") || chimeraMap.isEmpty()) {
            return false;
        }
        if (chimeraMap.keySet().contains(string)) {
            try {
                chimera = Class.forName("CardAugments.CardAugmentsMod");
                chimeraAbstractAugment = Class.forName("CardAugments.cardmods.AbstractAugment");
                Method m = chimera.getMethod("isAugmentEnabled", chimeraAbstractAugment);
                return (boolean) m.invoke(null, chimeraMap.get(string));
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private void loadChimeraCardModifiers() {
        try {
            chimera = Class.forName("CardAugments.CardAugmentsMod");
            chimeraAbstractAugment = Class.forName("CardAugments.cardmods.AbstractAugment");
            allChimera.addAll(ReflectionHacks.getPrivateStatic(chimera, "commonMods"));
            allChimera.addAll(ReflectionHacks.getPrivateStatic(chimera, "uncommonMods"));
            allChimera.addAll(ReflectionHacks.getPrivateStatic(chimera, "rareMods"));
            allChimera.addAll(ReflectionHacks.getPrivateStatic(chimera, "specialMods"));
            allChimera.forEach((mod) -> chimeraMap.put(mod.getClass().getName(), mod));

            Method m = chimeraAbstractAugment.getMethod("getModRarity");
            
            for (AbstractCardModifier mod: allChimera) {
                if (!chimeraModEnabled(mod)) {
                    continue;
                }
                EnchantmentRarity rarity;
                switch (m.invoke(mod).toString()){
                    case "Common":
                        rarity = EnchantmentRarity.COMMON; 
                        break;
                    case "Uncommon":
                        rarity = EnchantmentRarity.UNCOMMON; 
                        break;
                    case "Rare":
                        rarity = EnchantmentRarity.RARE; 
                        break;
                    default:
                        rarity = EnchantmentRarity.RARE; 
                }
                AbstractEnchantment enchant = new ChimeraEnchantment(mod, rarity);
                addEnchantmentToList(enchant);
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

            for (AbstractCardModifier mod : commonMana) {
                addEnchantmentToList(new ManaSurgeEnchantment(mod, EnchantmentRarity.COMMON));
            }
            for (AbstractCardModifier mod : uncommonMana) {
                addEnchantmentToList(new ManaSurgeEnchantment(mod, EnchantmentRarity.UNCOMMON));
            }

        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving card mods from anniv6", e);
        }
    }

    private void loadAnniv5Modifiers() {
        //Rare
        addEnchantmentToList(new KeywordEnchantment(new RipMod(), EnchantmentRarity.RARE, "anniv5:rippable"));
    }

    private void loadExpansionPackModifiers() {
        // Common
        addEnchantmentToList(new MagnetizeEnchantment());
        // Uncommon
        addEnchantmentToList(new KeywordEnchantment(new StitchMod(), EnchantmentRarity.UNCOMMON, "anniv5:stitch"));
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

    public static boolean canSpawn() {
        return (AbstractDungeon.player.masterDeck.group.stream().filter(c -> CardModifierManager.modifiers(c).isEmpty()).anyMatch(c -> c.cost != -2));
    }
}
