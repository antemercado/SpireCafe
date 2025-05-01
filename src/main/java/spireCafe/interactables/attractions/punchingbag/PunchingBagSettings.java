package spireCafe.interactables.attractions.punchingbag;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.UIStrings;

import basemod.IUIElement;
import spireCafe.Anniv7Mod;
import spireCafe.abstracts.AbstractAttractionSettings;
import spireCafe.ui.FixedModLabeledToggleButton.FixedModLabeledButton;

public class PunchingBagSettings extends AbstractAttractionSettings{

    private FixedModLabeledButton resetButton;
    public static boolean isEnabled = false;

    private static final String ID = Anniv7Mod.makeID(PunchingBagSettings.class.getSimpleName());
    private static final UIStrings uiString = CardCrawlGame.languagePack.getUIString(ID);

    public PunchingBagSettings() {
        super();
        this.resetButton = new FixedModLabeledButton(uiString.TEXT[0], Anniv7Mod.ATTRACTION_SETTING_X, Anniv7Mod.ATTRACTION_SETTING_Y - 38f, Color.WHITE, Color.RED, null,
            (click) -> {resetHighScore();});

        IUIElement wrapper = new IUIElement() {
            @Override
            public void render(SpriteBatch sb) {
                if (PunchingBagSettings.isEnabled) {
                    resetButton.render(sb);
                }
            }

            @Override
            public void update() {
                if (PunchingBagSettings.isEnabled) {
                    resetButton.update();
                }
            }

            @Override
            public int renderLayer() {
                return 3;
            }

            @Override
            public int updateOrder() {
                return 1;
            }
        };

        this.elements.add(wrapper);
    }
    
    private void resetHighScore() {
        PunchingBagAttraction.saveHiScore(0);
    }

    public void enable() {
        isEnabled = true;
    }

    @Override
    public void disable() {
        isEnabled = false;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }    
}
