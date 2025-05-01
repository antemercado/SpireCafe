package spireCafe;

import basemod.AutoAdd;
import basemod.BaseMod;
import basemod.IUIElement;
import basemod.ModPanel;
import basemod.ModSlider;
import basemod.abstracts.CustomSavable;
import basemod.devcommands.ConsoleCommand;
import basemod.helpers.RelicType;
import basemod.interfaces.*;
import basemod.patches.com.megacrit.cardcrawl.helpers.TipHelper.HeaderlessTip;
import basemod.patches.com.megacrit.cardcrawl.screens.options.DropdownMenu.DropdownColoring;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.Keyword;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import imgui.ImGui;
import imgui.type.ImFloat;
import javassist.CtClass;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import spireCafe.abstracts.AbstractAttraction;
import spireCafe.abstracts.AbstractAttractionSettings;
import spireCafe.abstracts.AbstractBartender;
import spireCafe.abstracts.AbstractCafeInteractable;
import spireCafe.abstracts.AbstractCutscene;
import spireCafe.abstracts.AbstractSCRelic;
import spireCafe.cardvars.SecondDamage;
import spireCafe.cardvars.SecondMagicNumber;
import spireCafe.interactables.attractions.jukebox.JukeboxRelic;
import spireCafe.interactables.attractions.makeup.MakeupTableAttraction;
import spireCafe.interactables.attractions.punchingbag.PunchingBagAttraction;
import spireCafe.interactables.attractions.punchingbag.PunchingBagSettings;
import spireCafe.interactables.merchants.fleamerchant.FleaMerchant;
import spireCafe.interactables.patrons.dandadan.RightBallPotionSavable;
import spireCafe.interactables.patrons.dandadan.RightballPotion;
import spireCafe.interactables.patrons.dandadan.RightballPotionPatch;
import spireCafe.interactables.patrons.missingno.MissingnoUtil;
import spireCafe.interactables.patrons.powerelic.implementation.debug.DevcommandPowerelic;
import spireCafe.interactables.patrons.spiomesmanifestation.SpiomesManifestationPatron;
import spireCafe.patches.CafeEntryExitPatch;
import spireCafe.patches.PotencySaverPatch;
import spireCafe.screens.CafeMerchantScreen;
import spireCafe.screens.JukeboxScreen;
import spireCafe.screens.CafeMatchAndKeepScreen;
import spireCafe.ui.Dialog;
import spireCafe.ui.FixedModLabeledToggleButton.FixedModLabel;
import spireCafe.ui.FixedModLabeledToggleButton.FixedModLabeledToggleButton;
import spireCafe.util.TexLoader;
import spireCafe.util.cutsceneStrings.CutsceneStrings;
import spireCafe.util.cutsceneStrings.LocalizedCutsceneStrings;
import spireCafe.util.devcommands.Cafe;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static spireCafe.interactables.attractions.bookshelf.BookshelfAttraction.PAGE_CONFIG_KEY;
import static spireCafe.interactables.patrons.missingno.MissingnoPatches.*;
import static spireCafe.patches.CafeEntryExitPatch.CAFE_ENTRY_SOUND_KEY;
import static spireCafe.screens.JukeboxScreen.*;

@SuppressWarnings({"unused"})
@SpireInitializer
public class Anniv7Mod implements
        EditCardsSubscriber,
        EditRelicsSubscriber,
        EditStringsSubscriber,
        EditKeywordsSubscriber,
        PostInitializeSubscriber,
        AddAudioSubscriber,
        PostUpdateSubscriber,
        PostDungeonInitializeSubscriber,
        ImGuiSubscriber,
        StartGameSubscriber {

    public static final Logger logger = LogManager.getLogger("SpireCafe");

    public static Settings.GameLanguage[] SupportedLanguages = {
            Settings.GameLanguage.ENG
    };

    public static Anniv7Mod thismod;
    public static SpireConfig modConfig = null;
    public static HashSet<String> currentRunSeenInteractables = null;
    public static ArrayList<String> allTimeSeenInteractables = null;

    public static final String modID = "anniv7";

    private static final String ATTACK_S_ART = modID + "Resources/images/512/attack.png";
    private static final String SKILL_S_ART = modID + "Resources/images/512/skill.png";
    private static final String POWER_S_ART = modID + "Resources/images/512/power.png";
    private static final String CARD_ENERGY_S = modID + "Resources/images/512/energy.png";
    private static final String TEXT_ENERGY = modID + "Resources/images/512/text_energy.png";
    private static final String ATTACK_L_ART = modID + "Resources/images/1024/attack.png";
    private static final String SKILL_L_ART = modID + "Resources/images/1024/skill.png";
    private static final String POWER_L_ART = modID + "Resources/images/1024/power.png";

    public static boolean initializedStrings = false;

    public static final Map<String, Keyword> keywords = new HashMap<>();

    public static List<String> unfilteredAllInteractableIDs = new ArrayList<>();
    public static HashMap<String, Class<? extends AbstractCafeInteractable>> interactableClasses = new HashMap<>();


    public static String makeID(String idText) {
        return modID + ":" + idText;
    }


    public Anniv7Mod() {
        BaseMod.subscribe(this);
    }

    public static String makePath(String resourcePath) {
        return modID + "Resources/" + resourcePath;
    }

    public static String makeImagePath(String resourcePath) {
        return modID + "Resources/images/" + resourcePath;
    }

    public static String makeCharacterPath(String resourcePath) {
        return modID + "Resources/images/characters/" + resourcePath;
    }

    public static String makeMerchantPath(String resourcePath) {
        return modID + "Resources/images/merchants/" + resourcePath;
    }

    public static String makeBartenderPath(String resourcePath) {
        return modID + "Resources/images/bartenders/" + resourcePath;
    }

    public static String makeAttractionPath(String resourcePath) {
        return modID + "Resources/images/attractions/" + resourcePath;
    }

    public static String makeUIPath(String resourcePath) {
        return modID + "Resources/images/ui/" + resourcePath;
    }

    public static String makeRelicPath(String resourcePath) {
        return modID + "Resources/images/relics/" + resourcePath;
    }

    public static String makeMonsterPath(String resourcePath) {
        return modID + "Resources/images/monsters/" + resourcePath;
    }

    public static String makePowerPath(String resourcePath) {
        return modID + "Resources/images/powers/" + resourcePath;
    }

    public static String makeCardPath(String resourcePath) {
        return modID + "Resources/images/cards/" + resourcePath;
    }

    public static String makeShaderPath(String resourcePath) {
        return modID + "Resources/shaders/" + resourcePath;
    }

    public static String makeOrbPath(String resourcePath) {
        return modID + "Resources/images/orbs/" + resourcePath;
    }

    public static String makeEventPath(String resourcePath) {
        return modID + "Resources/images/events/" + resourcePath;
    }

    public static String makeBackgroundPath(String resourcePath) {
        return modID + "Resources/images/backgrounds/" + resourcePath;
    }

    public static void initialize() {
        thismod = new Anniv7Mod();

        try {
            Properties defaults = new Properties();
            defaults.put("cafeEntryCost", "TRUE");
            defaults.put("disableShaders", "FALSE");
            defaults.put("seenInteractables", "");
            defaults.put(PAGE_CONFIG_KEY, "");
            modConfig = new SpireConfig(modID, "anniv7Config", defaults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadInteractables() {
        AutoAdd autoAdd = new AutoAdd(modID)
                .packageFilter(Anniv7Mod.class);

        Class<?> type = AbstractCafeInteractable.class;
        Collection<CtClass> foundClasses = autoAdd.findClasses(type);

        for (CtClass ctClass : foundClasses) {
            boolean ignore = ctClass.hasAnnotation(AutoAdd.Ignore.class);
            if (!ignore) {
                String id = ctClass.getSimpleName();
                unfilteredAllInteractableIDs.add(id);
                try {
                    Class<? extends AbstractCafeInteractable> interactableClass = (Class<? extends AbstractCafeInteractable>) Loader.getClassPool().getClassLoader().loadClass(ctClass.getName());
                    interactableClasses.put(id, interactableClass);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        logger.info("Found interactable classes with AutoAdd: " + unfilteredAllInteractableIDs.size());
    }

    public static ArrayList<String> getSeenInteractables() {
        if (modConfig == null) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(modConfig.getString("seenInteractables").split(",")));
    }

    public static void saveSeenInteractables(ArrayList<String> input) throws IOException {
        if (modConfig == null) return;
        modConfig.setString("seenInteractables", String.join(",", input));
        modConfig.save();
    }

    @Override
    public void receiveEditRelics() {
        new AutoAdd(modID)
                .packageFilter(Anniv7Mod.class)
                .any(AbstractSCRelic.class, (info, relic) -> {
                    if (relic.color == null) {
                            BaseMod.addRelic(relic, RelicType.SHARED);
                    } else {
                        BaseMod.addRelicToCustomPool(relic, relic.color);
                    }
                    if (!info.seen) {
                        UnlockTracker.markRelicAsSeen(relic.relicId);
                    }
                });
    }

    @Override
    public void receiveEditCards() {
        new AutoAdd(modID)
                .packageFilter(Anniv7Mod.class)
                .setDefaultSeen(true)
                .cards();

        BaseMod.addDynamicVariable(new SecondMagicNumber());
        BaseMod.addDynamicVariable(new SecondDamage());
    }

    @Override
    public void receivePostInitialize() {
        initializedStrings = true;
        addPotions();
        addSaveFields();
        initializeConfig();
        initializeSavedData();
        BaseMod.addEvent(CafeRoom.ID, CafeRoom.class, "CafeDungeon");
        BaseMod.addCustomScreen(new CafeMerchantScreen());
        BaseMod.addCustomScreen(new JukeboxScreen());
        BaseMod.addCustomScreen(new CafeMatchAndKeepScreen());
        ConsoleCommand.addCommand("cafe", Cafe.class);
        ConsoleCommand.addCommand("powerelic", DevcommandPowerelic.class);
    }

    public static void addPotions() {
        if (Loader.isModLoaded("widepotions")) {
            Consumer<String> whitelist = getWidePotionsWhitelistMethod();

        }
        BaseMod.addPotion(RightballPotion.class, 
                new Color(254 / 255f, 193 / 255f, 27 / 255f, 1f), null, null, RightballPotion.Potion_ID);
    }

    public static final ImFloat shake_power = new ImFloat(0.007f);
    public static final ImFloat shake_rate = new ImFloat(0.1f);
    public static final ImFloat shake_speed = new ImFloat(2f);
    public static final ImFloat shake_block_size = new ImFloat(0.001f);
    public static final ImFloat shake_color_rate = new ImFloat(0.004f);

    @Override
    public void receiveImGui() {
        ImGui.sliderFloat("Glitch Power", shake_power.getData(), 0, 1f);
        ImGui.sliderFloat("Glitch Rate", shake_rate.getData(), 0, 1);
        ImGui.sliderFloat("Glitch Speed", shake_speed.getData(), 0, 10);
        ImGui.sliderFloat("Glitch Block Size", shake_block_size.getData(), 0, 1.0f);
        ImGui.sliderFloat("Glitch Color Rate", shake_color_rate.getData(), 0, 0.01f);
    }

    private static Consumer<String> getWidePotionsWhitelistMethod() {
        // To avoid the need for a dependency of any kind, we call Wide Potions through reflection
        try {
            Method whitelistMethod = Class.forName("com.evacipated.cardcrawl.mod.widepotions.WidePotionsMod").getMethod("whitelistSimplePotion", String.class);
            return s -> {
                try {
                    whitelistMethod.invoke(null, s);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error trying to whitelist wide potion for " + s, e);
                }
            };
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not find method WidePotionsMod.whitelistSimplePotion", e);
        }
    }

    @Deprecated
    private String getLangString() {
        for (Settings.GameLanguage lang : SupportedLanguages) {
            if (lang.equals(Settings.language)) {
                return Settings.language.name().toLowerCase(Locale.ROOT);
            }
        }
        return "eng";
    }

    @Override
    public void receiveEditStrings() {
        loadInteractables();

        loadStrings("eng");
        loadInteractableStrings(unfilteredAllInteractableIDs, "eng");
        if (Settings.language != Settings.GameLanguage.ENG)
        {
            loadStrings(Settings.language.toString().toLowerCase());
            loadInteractableStrings(unfilteredAllInteractableIDs, Settings.language.toString().toLowerCase());
        }
    }


    private void loadStrings(String langKey) {
        if (!Gdx.files.internal(modID + "Resources/localization/" + langKey + "/").exists()) return;
        loadStringsFile(langKey, CharacterStrings.class);
        loadStringsFile(langKey, CardStrings.class);
        loadStringsFile(langKey, RelicStrings.class);
        loadStringsFile(langKey, PowerStrings.class);
        loadStringsFile(langKey, UIStrings.class);
        loadStringsFile(langKey, StanceStrings.class);
        loadStringsFile(langKey, OrbStrings.class);
        loadStringsFile(langKey, PotionStrings.class);
        loadStringsFile(langKey, EventStrings.class);
        loadStringsFile(langKey, MonsterStrings.class);
        loadStringsFile(langKey, BlightStrings.class);
    }

    public void loadInteractableStrings(Collection<String> interactableIDs, String langKey) {
        for (String id : interactableIDs) {
            String languageAndInteractable = langKey + "/" + id;
            String filepath = modID + "Resources/localization/" + languageAndInteractable;
            if (!Gdx.files.internal(filepath).exists()) {
                continue;
            }
            logger.info("Loading strings for interactable " + id + " from \"resources/localization/" + languageAndInteractable + "\"");

            loadStringsFile(languageAndInteractable, CharacterStrings.class);
            loadStringsFile(languageAndInteractable, CardStrings.class);
            loadStringsFile(languageAndInteractable, RelicStrings.class);
            loadStringsFile(languageAndInteractable, PowerStrings.class);
            loadStringsFile(languageAndInteractable, UIStrings.class);
            loadStringsFile(languageAndInteractable, StanceStrings.class);
            loadStringsFile(languageAndInteractable, OrbStrings.class);
            loadStringsFile(languageAndInteractable, PotionStrings.class);
            loadCutsceneStringsFile(languageAndInteractable, CutsceneStrings.class);
            loadStringsFile(languageAndInteractable, MonsterStrings.class);
            loadStringsFile(languageAndInteractable, BlightStrings.class);
        }
    }

    private void loadStringsFile(String key, Class<?> stringType) {
        String filepath = modID + "Resources/localization/" + key + "/" + stringType.getSimpleName().replace("Strings", "strings") + ".json";
        if (Gdx.files.internal(filepath).exists()) {
            BaseMod.loadCustomStringsFile(stringType, filepath);
        }
    }

    private void loadCutsceneStringsFile(String key, Class<?> stringType) {
        String filepath = modID + "Resources/localization/" + key + "/" + stringType.getSimpleName().replace("Strings", "strings") + ".json";
        if (Gdx.files.internal(filepath).exists()) {
            LocalizedCutsceneStrings.loadCutsceneStringsFile(filepath);
        }
    }

    @Override
    public void receiveEditKeywords() {
        loadKeywords("eng");
        if (Settings.language != Settings.GameLanguage.ENG) {
            loadKeywords(Settings.language.toString().toLowerCase());
        }
    }

    private void loadKeywords(String langKey) {
        String filepath = modID + "Resources/localization/" + langKey + "/Keywordstrings.json";
        Gson gson = new Gson();
        List<Keyword> keywords = new ArrayList<>();
        if (Gdx.files.internal(filepath).exists()) {
            String json = Gdx.files.internal(filepath).readString(String.valueOf(StandardCharsets.UTF_8));
            keywords.addAll(Arrays.asList(gson.fromJson(json, Keyword[].class)));
        }

        for (Keyword keyword : keywords) {
            BaseMod.addKeyword(modID, keyword.PROPER_NAME, keyword.NAMES, keyword.DESCRIPTION);
            if (!keyword.ID.isEmpty())
            {
                Anniv7Mod.keywords.put(keyword.ID, keyword);
            }
        }
    }

    @Override
    public void receiveAddAudio() {
        BaseMod.addAudio(CAFE_ENTRY_SOUND_KEY, makePath("audio/cafe_entry_door_chime.mp3"));
        BaseMod.addAudio(POKE1, makePath("audio/poke1.mp3"));
        BaseMod.addAudio(POKE2, makePath("audio/poke2.mp3"));
        BaseMod.addAudio(POKE3, makePath("audio/poke3.mp3"));
        BaseMod.addAudio(POKE4, makePath("audio/poke4.mp3"));
        BaseMod.addAudio(POKE5, makePath("audio/poke5.mp3"));
        BaseMod.addAudio(POKE6, makePath("audio/poke6.mp3"));
        BaseMod.addAudio(POKE7, makePath("audio/poke7.mp3"));
        BaseMod.addAudio(POKE8, makePath("audio/poke8.mp3"));
        BaseMod.addAudio(POKE9, makePath("audio/poke9.mp3"));
    }

    public static float time = 0f;
    @Override
    public void receivePostUpdate() {
        time += Gdx.graphics.getRawDeltaTime();
        MissingnoUtil.doMissingnoStuff();
        //Jukebox Active Update Handling
        if (JukeboxScreen.FadingOut && nowPlayingSong != null) {
            JukeboxScreen.updateFadeOut();
            return;
        }
        // Reset to default music if not in a run and something is playing
        if (!CardCrawlGame.isInARun() && isPlaying) {
            JukeboxScreen.resetToDefaultMusic();
        }
        // Mute music when the game is backgrounded
        if (CardCrawlGame.MUTE_IF_BG && Settings.isBackgrounded) {
            if (nowPlayingSong != null) {
                nowPlayingSong.setVolume(0.0f);
            }
            return;
        }
        // Adjust volume dynamically while the game is in the foreground
        if (!JukeboxScreen.isPaused && nowPlayingSong != null && !Settings.isBackgrounded) {
            float adjustedVolume = Settings.MUSIC_VOLUME * Settings.MASTER_VOLUME; // Use the global volume slider
            nowPlayingSong.setVolume(adjustedVolume); // Update the music volume on the fly
        }
    }

    @Override
    public void receivePostDungeonInitialize() {
        if (!CardCrawlGame.isInARun()) {
            JukeboxScreen.resetToDefaultMusic();
        }
    }

    private ModPanel settingsPanel;

    
    private void loadAttractionSettings() {
        attractionSettings.put(PunchingBagAttraction.class.getSimpleName(), new PunchingBagSettings());

        for (Entry<String, AbstractAttractionSettings> setting : attractionSettings.entrySet()) {
            ArrayList<IUIElement> elements = setting.getValue().getElements();
            for (IUIElement e : elements) {
                settingsPanel.addUIElement(e);
            }
        }
    }

    private DropdownMenu filterDropdown;

    private String filterViewedInteractable;
    private AbstractAttractionSettings filterViewedAttractionSetting = null;

    private static final float ENTRYCOST_CHECKBOX_X = 400f;
    private static final float ENTRYCOST_CHECKBOX_Y = 685f;
    private static final float ENTRYCOST_SLIDER_X = 400f;
    private static final float ENTRYCOST_SLIDER_Y = 650f;
    private static final float ENTRYCOST_SLIDER_X2 = 425f;
    private static final float ENTRYCOST_SLIDER_Y2 = 615f;
    private static final float ENTRYCOST_SLIDER_MULTIPLIER = 100f;
    private static final float SHADERS_CHECKBOX_SERIES_X = 400f;
    private static final float SHADERS_CHECKBOX_Y = 560f;
    private static final float DROPDOWN_X = 400f;
    private static final float DROPDOWN_Y = 540f;
    private static final float FILTER_CHECKBOX_X = 400f;
    private static final float FILTER_CHECKBOX_Y = 450f;

    public static final float ATTRACTION_SETTING_X = 400f;
    public static final float ATTRACTION_SETTING_Y = 400f;

    private FixedModLabeledToggleButton filterCheckbox;

    private HashMap<String, AbstractAttractionSettings> attractionSettings = new HashMap<>();
    
    private void initializeConfig() {
        UIStrings configStrings = CardCrawlGame.languagePack.getUIString(makeID("ConfigMenuText"));
        
        Texture badge = TexLoader.getTexture(makeImagePath("ui/badge.png"));
        
        settingsPanel = new ModPanel();

        loadAttractionSettings();

        FixedModLabeledToggleButton cafeEntryCostToggle = new FixedModLabeledToggleButton(configStrings.TEXT[3], ENTRYCOST_CHECKBOX_X, ENTRYCOST_CHECKBOX_Y, Color.WHITE, FontHelper.tipBodyFont, getCafeEntryCostConfig(), null,
                (label) -> {},
                (button) -> setCafeEntryCostConfig(button.enabled));
        settingsPanel.addUIElement(cafeEntryCostToggle);

        FixedModLabeledToggleButton disableShaders = new FixedModLabeledToggleButton(configStrings.TEXT[4], SHADERS_CHECKBOX_SERIES_X, SHADERS_CHECKBOX_Y, Color.WHITE, FontHelper.tipBodyFont, getDisableShadersConfig(), null,
                (label) -> {},
                (button) -> setDisableShadersConfig(button.enabled));
        settingsPanel.addUIElement(disableShaders);

        FixedModLabel cafeEntryCostSliderLabel = new FixedModLabel(configStrings.TEXT[6], ENTRYCOST_SLIDER_X, ENTRYCOST_SLIDER_Y, Color.WHITE, FontHelper.tipBodyFont, null, (update) -> {});
        settingsPanel.addUIElement(cafeEntryCostSliderLabel);

        ModSlider cafeEntryCostSlider = new ModSlider("", ENTRYCOST_SLIDER_X2 * Settings.xScale, ENTRYCOST_SLIDER_Y2 * Settings.yScale, ENTRYCOST_SLIDER_MULTIPLIER, "%", null,
            (me) -> {
                int val = Math.round(me.value * me.multiplier);
                setCafeEntryPercentConfig(val);
            });
        settingsPanel.addUIElement(cafeEntryCostSlider);
        cafeEntryCostSlider.setValue((float)getCafeEntryPercentConfig() / ENTRYCOST_SLIDER_MULTIPLIER);
            
        ArrayList<String> filterOptions = new ArrayList<>();
        for (String i : unfilteredAllInteractableIDs) {
            try {
                Class<? extends AbstractCafeInteractable> clz = Anniv7Mod.interactableClasses.get(i);
                AbstractCafeInteractable interactable = clz.getConstructor(float.class, float.class).newInstance(0, 0);
                logger.info(String.format("interactable = %s", interactable));
                logger.info(String.format("interactable.name = %s", interactable.name));
                filterOptions.add(interactable.name);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                logger.error("Error creating interactable " + i + e);
                filterOptions.add(i);

            }
        }

        filterDropdown = new DropdownMenu((dropdownMenu, index, s) -> filterSetViewedInteractable(index),
            filterOptions, FontHelper.tipBodyFont, Settings.CREAM_COLOR);
            DropdownColoring.RowToColor.function.set(filterDropdown, (index) -> getFilterConfig(unfilteredAllInteractableIDs.get(index)) ? null : Settings.RED_TEXT_COLOR);
        IUIElement wrapperDropdown = new IUIElement() {
            public void render(SpriteBatch sb) {
                filterDropdown.render(sb, DROPDOWN_X * Settings.xScale,DROPDOWN_Y * Settings.yScale);
            }
            public void update() {
                filterDropdown.update();
            }
            public int renderLayer() {return 3;}
            public int updateOrder() {return 0;}
        };

        settingsPanel.addUIElement(wrapperDropdown);

        filterCheckbox = new FixedModLabeledToggleButton(configStrings.TEXT[5], FILTER_CHECKBOX_X, FILTER_CHECKBOX_Y, Color.WHITE,
            FontHelper.tipBodyFont, true, null, (label) -> {},
            (button) -> setFilterConfig(filterViewedInteractable, button.enabled));
        IUIElement wrapperFilterCheckbox = new IUIElement() {
            @Override
            public void render(SpriteBatch sb) {
                filterCheckbox.render(sb);
            }

            @Override
            public void update() {
                if (!filterDropdown.isOpen) {
                    filterCheckbox.update();
                }
            }

            @Override
            public int renderLayer() {
                return filterCheckbox.renderLayer();
            }

            @Override
            public int updateOrder() {
                return 1;
            }
        };

        settingsPanel.addUIElement(wrapperFilterCheckbox);

        BaseMod.registerModBadge(badge, configStrings.TEXT[0], configStrings.TEXT[1], configStrings.TEXT[2], settingsPanel);
    }

    private void filterSetViewedInteractable(int index) {
        filterViewedInteractable = unfilteredAllInteractableIDs.get(index);
        filterCheckbox.toggle.enabled = getFilterConfig(filterViewedInteractable);
        if (filterViewedAttractionSetting != null){
            filterViewedAttractionSetting.disable();
        }
        filterViewedAttractionSetting = attractionSettings.get(filterViewedInteractable);
        if (filterViewedAttractionSetting != null) {
            filterViewedAttractionSetting.enable();
        }
    }


    public static boolean getFilterConfig(String interactableID) {
        if (modConfig != null && modConfig.has(interactableID +"_ENABLED")) {
            return modConfig.getBool(interactableID +"_ENABLED");
        } else {
            return true;
        }
    }

    private static void setFilterConfig(String interactableID, boolean enable) {
        if (modConfig != null) {
            modConfig.setBool(interactableID +"_ENABLED", enable);
            try {
                modConfig.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean getCafeEntryCostConfig() {
        return modConfig != null && modConfig.getBool("cafeEntryCost");
    }

    public static void setCafeEntryCostConfig(boolean bool) {
        if (modConfig != null) {
            modConfig.setBool("cafeEntryCost", bool);
            try {
                modConfig.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getCafeEntryPercentConfig() {
        if (modConfig != null && modConfig.has("cafeEntryPercent")) {
            return modConfig.getInt("cafeEntryPercent");
        } else {
            return CafeEntryExitPatch.HP_COST_PERCENT;
        }
    }
    
    public static void setCafeEntryPercentConfig(int val) {
        if (modConfig != null) {
            modConfig.setInt("cafeEntryPercent", val);
            try {
                modConfig.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean getDisableShadersConfig() {
        return modConfig != null && modConfig.getBool("disableShaders");
    }

    public static void setDisableShadersConfig(boolean bool) {
        if (modConfig != null) {
            modConfig.setBool("disableShaders", bool);
            try {
                modConfig.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeSavedData() {
    }

    public static void addSaveFields() {
        BaseMod.addSaveField(SavableCurrentRunSeenInteractables.SaveKey, new SavableCurrentRunSeenInteractables());
        BaseMod.addSaveField(makeID("AppliedMakeup"), new CustomSavable<Boolean>() {
            @Override
            public Boolean onSave() {
                return MakeupTableAttraction.isAPrettySparklingPrincess;
            }

            @Override
            public void onLoad(Boolean state) {
                MakeupTableAttraction.isAPrettySparklingPrincess = state != null && state;
            }
        });
	BaseMod.addSaveField("Anniv7DepletedPotion", new CustomSavable<List<Integer>>() {
            @Override
            public List<Integer> onSave() {
                return AbstractDungeon.player.potions.stream().map(p -> PotencySaverPatch.PotionUseField.isDepleted.get(p)).collect(Collectors.toCollection(ArrayList::new));
            }

            @Override
            public void onLoad(List<Integer> l) {
                int c = 0;
                if (l != null && !l.isEmpty()) {
                    for (AbstractPotion p : AbstractDungeon.player.potions) {
                        int i = l.get(NumberUtils.min(c++, AbstractDungeon.player.potions.size() - 1));
                        if(i!=-1) {
                            PotencySaverPatch.PotionUseField.isDepleted.set(p, i);
                            p.name = CardCrawlGame.languagePack.getCharacterString(FleaMerchant.ID).TEXT[0].replace("{0}", p.name);
                        }
                        p.initializeData();
                    }
                }
            }
        });
        BaseMod.addSaveField(makeID("ballPotion"), new RightBallPotionSavable());
        BaseMod.addSaveField(makeID("queuedBiomeID"), new CustomSavable<String>() {
            @Override
            public String onSave() {
                return SpiomesManifestationPatron.queuedBiomeID;
            }

            @Override
            public void onLoad(String id) {
                if (Loader.isModLoaded("anniv6") && id != null) {
                    SpiomesManifestationPatron.queuedBiomeID=id;
                    Object queuedBiome = SpiomesManifestationPatron.getBiomeById(id);
                    if (queuedBiome != null) {
                        SpiomesManifestationPatron.addBiomeToNextMap(queuedBiome);
                    }
                }
            }
        });
    }

    @Override
    public void receiveStartGame() {
        //Reset static variables in case the player quit to main menu during a cutscene
        CafeRoom.isInteracting = false;
        AbstractCutscene.isInCutscene = false;
        Dialog.optionList.clear();
        // Reset JukeboxScreen.isCoinSlotClicked
        if (AbstractDungeon.player != null) {
            if (AbstractDungeon.player.hasRelic(JukeboxRelic.ID)) {
                // Player has the relic, mark the coin slot as clicked
                JukeboxScreen.isCoinSlotClicked = true;
            } else {
                // Player doesn't have the relic, clear the coin slot flag
                JukeboxScreen.isCoinSlotClicked = false;
            }
        }
        if (!CardCrawlGame.loadingSave) {
            RightballPotionPatch.receiveStartGame();
        }
    }

    public static class SavableCurrentRunSeenInteractables implements CustomSavable<HashSet<String>> {
        public final static String SaveKey = "CurrentRunSeenInteractables";

        @Override
        public HashSet<String> onSave() {
            return currentRunSeenInteractables;
        }

        @Override
        public void onLoad(HashSet<String> s) {
            currentRunSeenInteractables = s == null ? new HashSet<>() : s;
        }
    }

    public static void clearCurrentRunSeenInteractables(Class<? extends AbstractCafeInteractable> clz) {
        ArrayList<String> toRemove = getInteractableClassIDs(clz);
        for (String i : toRemove){
            currentRunSeenInteractables.remove(i);
        }
    }

    private static ArrayList<String> getInteractableClassIDs(Class<? extends AbstractCafeInteractable> clz) {
        List<Class<? extends AbstractCafeInteractable>> classes = getInteractableClassesByType(clz);
        ArrayList<String> ret = new ArrayList<>();
        for (Class<? extends AbstractCafeInteractable> i : classes) {
            ret.add(i.getSimpleName());
        }
        return ret;
    }

    private static List<Class<? extends AbstractCafeInteractable>> getInteractableClassesByType(Class<? extends AbstractCafeInteractable> clz) {
        return Anniv7Mod.interactableClasses.entrySet().stream()
                .filter(entry -> clz.isAssignableFrom(entry.getValue()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

}



