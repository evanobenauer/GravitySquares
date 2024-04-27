package com.ejo.gravityshapes.scenes;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.misc.DoOnce;
import com.ejo.glowlib.setting.Setting;
import com.ejo.glowlib.setting.SettingManager;
import com.ejo.glowlib.time.StopWatch;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.ElementUI;
import com.ejo.glowui.scene.elements.TextUI;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowui.scene.elements.widget.ButtonUI;
import com.ejo.glowui.scene.elements.widget.ModeCycleUI;
import com.ejo.glowui.scene.elements.widget.SliderUI;
import com.ejo.glowui.scene.elements.widget.ToggleUI;
import com.ejo.glowui.util.input.Key;
import com.ejo.gravityshapes.test.scenes.CollisionTestScene;

import java.awt.*;
import java.util.Random;

public class TitleScene extends Scene {

    private final Setting<Integer> objectCount = new Setting<>("objectCount", 50);
    private final Setting<Double> minSize = new Setting<>("minSize", 3d);
    private final Setting<Double> maxSize = new Setting<>("maxSize", 15d);
    private final Setting<Boolean> doWallBounce = new Setting<>("doBounce", true);
    private final Setting<String> collisionType = new Setting<>("collisionType", "NONE");

    private final SliderUI<Integer> sliderObjectCount = new SliderUI<>("Object Count", new Vector(10, 10), new Vector(300, 20), ColorE.BLUE, objectCount, 0, 500, 1, SliderUI.Type.INTEGER, true);
    private final SliderUI<Double> sliderMinSize = new SliderUI<>("Min Size", new Vector(10, 40), new Vector(300, 20), ColorE.BLUE, minSize, 0.1d, 50d, .1, SliderUI.Type.FLOAT, true);
    private final SliderUI<Double> sliderMaxSize = new SliderUI<>("Max Size", new Vector(10, 70), new Vector(300, 20), ColorE.BLUE, maxSize, 1.1d, 50d, .1, SliderUI.Type.FLOAT, true);

    private final ToggleUI toggleDoWallBounce = new ToggleUI("Do Wall Bounce", new Vector(10, 100), new Vector(300, 20), ColorE.BLUE, doWallBounce);
    private final ModeCycleUI<String> modeCollisionType = new ModeCycleUI<>("Type", new Vector(10, 130), new Vector(300, 20), ColorE.BLUE, collisionType,"NONE","MERGE","PUSH");

    private final ButtonUI buttonStart = new ButtonUI("Start!", Vector.NULL, new Vector(200, 60), new ColorE(0, 125, 200, 200), ButtonUI.MouseButton.LEFT, () -> {
        getWindow().setScene(new GravityScene(objectCount.get(), minSize.get(), maxSize.get(), doWallBounce.get(), collisionType.get()));
        //getWindow().setScene(new CollisionTestScene(objectCount.get(), maxSize.get(), doWallBounce.get()));
        //getWindow().setScene(new OptimizedGravityScene(objectCount.get(), maxSize.get(), doWallBounce.get()));
        //getWindow().setScene(new OptimizedCollisionGravityScene((Key.isShiftDown() ? 10 : 1) * objectCount.get(), maxSize.get(), doWallBounce.get()));
        //getWindow().setScene(new ElectricScene(objectCount.get(), objectCount.get(), maxSize.get(), doWallBounce.get(), doCollisions.get(), drawFieldLines.get()));
        //getWindow().setScene(new CollisionGravityScene(objectCount.get(), minSize.get(), maxSize.get(), doWallBounce.get(), drawFieldLines.get()));
        //getWindow().setScene(new MouseGravityScene(objectCount.get(), minSize.get(), maxSize.get(), doWallBounce.get(), drawFieldLines.get()));
        SettingManager.getDefaultManager().saveAll();
    });

    private final TextUI title = new TextUI("Gravity Shapes", new Font("Arial Black", Font.BOLD, 50), Vector.NULL, ColorE.WHITE);

    private double titleAnimationStep = 0;
    private final StopWatch watchTwinkleStars = new StopWatch();

    public TitleScene() {
        super("Title");
        DoOnce.DEFAULT6.reset();
        SettingManager.getDefaultManager().loadAll();
    }


    @Override
    public void draw() {
        initElements();
        updateStarPositionsOnResize();

        drawBackground(new ColorE(25, 25, 25, 255));

        //Setup title and start button
        double yOffset = -40;
        updateTitle(yOffset);
        buttonStart.setPos(getSize().getMultiplied(.5d).getAdded(buttonStart.getSize().getMultiplied(-.5)).getAdded(0, title.getFont().getSize() + 30).getAdded(0, yOffset));

        super.draw();
    }

    @Override
    public void tick() {
        super.tick();

        //Set size range caps
        if (minSize.get() > maxSize.get()) minSize.set(maxSize.get());

        twinkleStars();
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        super.onKeyPress(key, scancode, action, mods);
        if (key == Key.KEY_ENTER.getId() && action == Key.ACTION_PRESS) {
            buttonStart.getAction().run();
        }
    }

    private void initElements() {
        DoOnce.DEFAULT6.run(() -> {
            addStars();
            addElements(buttonStart, title, sliderObjectCount, sliderMinSize, sliderMaxSize, toggleDoWallBounce, modeCollisionType);
        });
    }

    private void updateTitle(double yOffset) {
        title.setPos(getSize().getMultiplied(.5d).getAdded(title.getSize().getMultiplied(-.5)).getAdded(0, yOffset));
        title.setPos(title.getPos().getAdded(new Vector(0, Math.sin(titleAnimationStep) * 8)));
        titleAnimationStep += 0.05;
        if (titleAnimationStep >= Math.PI * 2) titleAnimationStep = 0;
    }

    private void addStars() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            ColorE color = new ColorE(255, random.nextInt(125, 255), 100, 255);
            RectangleUI obj = new RectangleUI(new Vector(random.nextDouble(0, getSize().getX()), random.nextDouble(0, getWindow().getSize().getY())), new Vector(1, 1), color);
            obj.setTicking(false);
            addElements(obj);
        }
    }

    private void twinkleStars() {
        watchTwinkleStars.start();
        Random random = new Random();
        if (watchTwinkleStars.hasTimePassedS(.25)) {
            for (ElementUI element : getElements()) {
                if (element instanceof RectangleUI rect)
                    rect.setColor(new ColorE(255, random.nextInt(125, 255), 100, 255));
            }
            watchTwinkleStars.restart();
        }
    }

    private void updateStarPositionsOnResize() {
        getWindow().doOnResize.run(() -> {
            Random random = new Random();
            for (ElementUI el : getElements()) {
                if (el instanceof RectangleUI rect) {
                    rect.setPos(new Vector(random.nextDouble(0, getSize().getX()), random.nextDouble(0, getWindow().getSize().getY())));
                }
            }
        });
    }
}
