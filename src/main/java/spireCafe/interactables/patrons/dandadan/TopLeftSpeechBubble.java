package spireCafe.interactables.patrons.dandadan;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.DialogWord.AppearEffect;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.SpeechTextEffect;
import spireCafe.Anniv7Mod;
import spireCafe.util.TexLoader;

public class TopLeftSpeechBubble extends AbstractGameEffect {
    private static final float SHADOW_OFFSET;
    private static final float WAVY_DISTANCE;
    private static final float ADJUST_X;
    private static final float ADJUST_Y;

    private static final Texture speech_bubble_img = TexLoader.getTexture(Anniv7Mod.makeCharacterPath("Dandadan/speechBubble.png"));

    private float shadow_offset;
    private float x;
    private float y;
    private float wavy_y;
    private float wavyHelper;
    private float scaleTimer;
    private boolean facingRight;
    private Color shadowColor;

    public TopLeftSpeechBubble(float x, float y, String msg, boolean flipX) {
        this(x, y, 2.0F, msg, flipX);
    }

    public TopLeftSpeechBubble(float x, float y, float duration, String msg, boolean flipX) {
        this.shadow_offset = 0.0F;
        this.scaleTimer = 0.3F;
        this.shadowColor = new Color(0.0F, 0.0F, 0.0F, 0.0F);
        float effect_x = -170.0F * Settings.scale;
        if (flipX) {
            effect_x = 170.0F * Settings.scale;
        }

        AbstractDungeon.topLevelEffectsQueue.add(
                new SpeechTextEffect(x + effect_x, y + 124.0F * Settings.scale, duration, msg, AppearEffect.BUMP_IN));
        if (flipX) {
            this.x = x + ADJUST_X;
        } else {
            this.x = x - ADJUST_X;
        }

        this.y = y + ADJUST_Y;
        this.color = new Color(0.8F, 0.9F, 0.9F, 0.0F);
        this.duration = duration;
        this.facingRight = !flipX;
    }

    public void update() {
        this.updateScale();
        this.wavyHelper += Gdx.graphics.getDeltaTime() * 4.0F;
        this.wavy_y = MathUtils.sin(this.wavyHelper) * WAVY_DISTANCE;
        this.duration -= Gdx.graphics.getDeltaTime();
        if (this.duration < 0.0F) {
            this.isDone = true;
        }

        if (this.duration > 0.3F) {
            this.color.a = MathUtils.lerp(this.color.a, 1.0F, Gdx.graphics.getDeltaTime() * 12.0F);
        } else {
            this.color.a = MathUtils.lerp(this.color.a, 0.0F, Gdx.graphics.getDeltaTime() * 12.0F);
        }

        this.shadow_offset = MathUtils.lerp(this.shadow_offset, SHADOW_OFFSET, Gdx.graphics.getDeltaTime() * 4.0F);
    }

    private void updateScale() {
        this.scaleTimer -= Gdx.graphics.getDeltaTime();
        if (this.scaleTimer < 0.0F) {
            this.scaleTimer = 0.0F;
        }

        if (Settings.isMobile) {
            this.scale = Interpolation.swingIn.apply(Settings.scale * 1.15F, Settings.scale / 2.0F,
                    this.scaleTimer / 0.3F);
        } else {
            this.scale = Interpolation.swingIn.apply(Settings.scale, Settings.scale / 2.0F, this.scaleTimer / 0.3F);
        }

    }

    public void render(SpriteBatch sb) {
        this.shadowColor.a = this.color.a / 4.0F;
        sb.setColor(this.shadowColor);
        sb.draw(speech_bubble_img, this.x - 256.0F + this.shadow_offset,
                this.y - 256.0F + this.wavy_y - this.shadow_offset, 256.0F, 256.0F, 512.0F, 512.0F, this.scale,
                this.scale, this.rotation, 0, 0, 512, 512, this.facingRight, false);
        sb.setColor(this.color);
        sb.draw(speech_bubble_img, this.x - 256.0F, this.y - 256.0F + this.wavy_y, 256.0F, 256.0F, 512.0F,
                512.0F, this.scale, this.scale, this.rotation, 0, 0, 512, 512, this.facingRight, false);
    }

    public void dispose() {
    }

    static {
        SHADOW_OFFSET = 16.0F * Settings.scale;
        WAVY_DISTANCE = 2.0F * Settings.scale;
        ADJUST_X = 170.0F * Settings.scale;
        ADJUST_Y = 116.0F * Settings.scale;
    }
}
