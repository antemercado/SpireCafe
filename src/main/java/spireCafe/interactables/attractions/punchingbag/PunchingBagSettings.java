package spireCafe.interactables.attractions.punchingbag;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import basemod.IUIElement;
import spireCafe.abstracts.AbstractAttractionSettings;
import spireCafe.ui.FixedModLabeledToggleButton.FixedModLabeledButton;

public class PunchingBagSettings extends AbstractAttractionSettings{

    private FixedModLabeledButton resetButton;
    public static boolean isEnabled = false;

    public PunchingBagSettings() {
        super(PunchingBagAttraction.class);

        this.resetButton = new FixedModLabeledButton("Reset High Score", 400f, 450f, Color.WHITE, Color.RED, null,
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
