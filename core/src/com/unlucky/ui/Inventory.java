package com.unlucky.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.unlucky.entity.Player;
import com.unlucky.event.EventState;
import com.unlucky.main.Unlucky;
import com.unlucky.map.TileMap;
import com.unlucky.resource.ResourceManager;
import com.unlucky.screen.GameScreen;

/**
 * Inventory UI that allows for management of items and equips
 * Also shows player stats
 *
 * @author Ming Li
 */
public class Inventory extends UI implements Disposable {

    // Scene2D
    public Stage stage;
    private Viewport viewport;

    private boolean ended = false;

    // UI
    // main background ui
    private MovingImageUI ui;
    // exit button
    private ImageButton exitButton;
    // headers
    private Label[] headers;
    private String[] headerStrs = { "STATUS", "EQUIPMENT", "INVENTORY" };
    // stats labels
    private Label hp;
    private Label damage;
    private Label accuracy;
    private Label exp;
    // health bar (no need for dynamic one)
    private int maxBarWidth = 124;
    private int hpBarWidth = 0;
    private int expBarWidth = 0;

    public Inventory(GameScreen gameScreen, TileMap tileMap, Player player, ResourceManager rm) {
        super(gameScreen, tileMap, player, rm);

        viewport = new ExtendViewport(Unlucky.V_WIDTH * 2, Unlucky.V_HEIGHT * 2, new OrthographicCamera());
        stage = new Stage(viewport, gameScreen.getBatch());

        ui = new MovingImageUI(rm.inventoryui372x212, new Vector2(400, 14), new Vector2(14, 14), 8, 372, 212);
        stage.addActor(ui);

        // create exit button
        ImageButton.ImageButtonStyle exitStyle = new ImageButton.ImageButtonStyle();
        exitStyle.imageUp = new TextureRegionDrawable(rm.exitbutton18x18[0][0]);
        exitStyle.imageDown = new TextureRegionDrawable(rm.exitbutton18x18[1][0]);
        exitButton = new ImageButton(exitStyle);
        exitButton.setSize(18, 18);
        exitButton.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               end();
           }
        });
        stage.addActor(exitButton);

        // Fonts and Colors
        BitmapFont font = rm.pixel10;
        Label.LabelStyle stdWhite = new Label.LabelStyle(font, new Color(1, 1, 1, 1));
        Label.LabelStyle blue = new Label.LabelStyle(font, new Color(0, 190 / 255.f, 1, 1));
        Label.LabelStyle yellow = new Label.LabelStyle(font, new Color(1, 212 / 255.f, 0, 1));
        Label.LabelStyle green = new Label.LabelStyle(font, new Color(0, 1, 60 / 255.f, 1));
        Label.LabelStyle red = new Label.LabelStyle(font, new Color(220 / 255.f, 30 / 255.f, 30 / 255.f, 1));

        // create headers
        headers = new Label[3];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = new Label(headerStrs[i], stdWhite);
            headers[i].setSize(124, 8);
            headers[i].setTouchable(Touchable.disabled);
            headers[i].setAlignment(Align.left);
            stage.addActor(headers[i]);
        }

        // create stats
        hp = new Label("", green);
        hp.setSize(124, 8);
        hp.setTouchable(Touchable.disabled);
        hp.setAlignment(Align.left);
        stage.addActor(hp);

        damage = new Label("", red);
        damage.setSize(124, 8);
        damage.setTouchable(Touchable.disabled);
        damage.setAlignment(Align.left);
        stage.addActor(damage);

        accuracy = new Label("", blue);
        accuracy.setSize(124, 8);
        accuracy.setTouchable(Touchable.disabled);
        accuracy.setAlignment(Align.left);
        stage.addActor(accuracy);

        exp = new Label("", yellow);
        exp.setSize(124, 8);
        exp.setTouchable(Touchable.disabled);
        exp.setAlignment(Align.left);
        stage.addActor(exp);
    }

    /**
     * Initializes the inventory screen
     */
    public void start() {
        // ui slides left to right
        ui.setOrigin(new Vector2(400, 14));
        ui.setTarget(new Vector2(14, 14));
        ui.start();

        exitButton.setDisabled(false);
        exitButton.setTouchable(Touchable.enabled);
    }

    /**
     * Resets all animation variables
     * Activated by the exit button
     */
    public void end() {
        exitButton.setDisabled(true);
        exitButton.setTouchable(Touchable.disabled);

        // ui slides off screen right to left
        ui.setTarget(new Vector2(400, 14));
        ui.setOrigin(new Vector2(14, 14));
        ui.start();

        ended = true;
    }

    /**
     * Switches back to the next state
     */
    public void next() {
        gameScreen.setCurrentEvent(EventState.MOVING);
        gameScreen.hud.toggle(true);
        ended = false;
    }

    public void update(float dt) {
        ui.update(dt);
        if (ended && ui.getX() == 400 && ui.getY() == 14) {
            next();
        }

        // update bars
        hpBarWidth = (int) (maxBarWidth / ((float) player.getMaxHp() / player.getHp()));
        expBarWidth = (int) (maxBarWidth / ((float) player.getMaxExp() / player.getExp()));

        // update all text
        hp.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
        damage.setText("DAMAGE: " + player.getMinDamage() + "-" + player.getMaxDamage());
        accuracy.setText("ACCURACY: " + player.getAccuracy() + "%");
        exp.setText("EXP: " + player.getExp() + "/" + player.getMaxExp());

        // update all positions
        exitButton.setPosition(ui.getX() + 363, ui.getY() + 202);
        headers[0].setPosition(ui.getX() + 16, ui.getY() + 194);
        headers[1].setPosition(ui.getX() + 16, ui.getY() + 112);
        headers[2].setPosition(ui.getX() + 168, ui.getY() + 194);
        hp.setPosition(ui.getX() + 16, ui.getY() + 184);
        damage.setPosition(ui.getX() + 16, ui.getY() + 150);
        accuracy.setPosition(ui.getX() + 16, ui.getY() + 138);
        exp.setPosition(ui.getX() + 16, ui.getY() + 168);
    }

    public void render(float dt) {
        stage.act(dt);
        stage.draw();

        // draw bars
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // health bar
        shapeRenderer.setColor(60 / 255.f, 60 / 255.f, 60 / 255.f, 1);
        shapeRenderer.rect(ui.getX() + 16, ui.getY() + 178, maxBarWidth, 4);
        shapeRenderer.setColor(0, 225 / 255.f, 0, 1);
        shapeRenderer.rect(ui.getX() + 16, ui.getY() + 180, hpBarWidth, 2);
        shapeRenderer.setColor(0, 175 / 255.f, 0, 1);
        shapeRenderer.rect(ui.getX() + 16, ui.getY() + 178, hpBarWidth, 2);
        // exp bar
        shapeRenderer.setColor(60 / 255.f, 60 / 255.f, 60 / 255.f, 1);
        shapeRenderer.rect(ui.getX() + 16, ui.getY() + 162, maxBarWidth, 4);
        shapeRenderer.setColor(1, 212 / 255.f, 0, 1);
        shapeRenderer.rect(ui.getX() + 16, ui.getY() + 164, expBarWidth, 2);
        shapeRenderer.setColor(200 / 255.f, 170 / 255.f, 0, 1);
        shapeRenderer.rect(ui.getX() + 16, ui.getY() + 162, expBarWidth, 2);

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

}
