package com.ejo.gravitysquares.scenes;

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
import com.ejo.glowui.scene.elements.widget.*;
import com.ejo.gravitysquares.objects.PhysicsRectangle;

import java.awt.*;
import java.util.Random;

public class TitleScene extends Scene {

    private final Setting<Integer> squareCount = new Setting<>("squareCount", 50);
    private final Setting<Double> minSize = new Setting<>("minSize", 3d);
    private final Setting<Double> maxSize = new Setting<>("maxSize", 15d);
    private final Setting<Boolean> bigSquare = new Setting<>("bigSquare", false);
    private final Setting<Boolean> doWallBounce = new Setting<>("doBounce", true);
    private final Setting<Boolean> doCollisions = new Setting<>("doCollisions", false);

    private final SliderUI<Integer> sliderSquareCount = new SliderUI<>("Square Count", new Vector(10, 10), new Vector(300, 20), ColorE.BLUE, squareCount, 0, 500, 1, SliderUI.Type.INTEGER, true);
    private final SliderUI<Double> sliderMinSize = new SliderUI<>("Min Size", new Vector(10, 40), new Vector(300, 20), ColorE.BLUE, minSize, 0.1d, 50d, .1, SliderUI.Type.FLOAT, true);
    private final SliderUI<Double> sliderMaxSize = new SliderUI<>("Max Size", new Vector(10, 70), new Vector(300, 20), ColorE.BLUE, maxSize, 1.1d, 50d, .1, SliderUI.Type.FLOAT, true);

    private final ToggleUI toggleDoWallBounce = new ToggleUI("Do Wall Bounce", new Vector(10, 100), new Vector(300, 20), ColorE.BLUE, doWallBounce);
    private final ToggleUI toggleDoCollisions = new ToggleUI("Do Collisions", new Vector(10, 130), new Vector(300, 20), ColorE.BLUE, doCollisions);
    private final ToggleUI toggleBigSquare = new ToggleUI("Big Square", new Vector(10, 160), new Vector(300, 20), ColorE.BLUE, bigSquare);

    private final ButtonUI buttonStart = new ButtonUI("Start!", Vector.NULL, new Vector(200, 60), new ColorE(0, 125, 200, 200), ButtonUI.MouseButton.LEFT, () -> {
        getWindow().setScene(new SquareOrbitScene(squareCount.get(), minSize.get(), maxSize.get(), bigSquare.get(), doWallBounce.get(), doCollisions.get()));
        SettingManager.getDefaultManager().saveAll();
    });

    private final TextUI title = new TextUI("Gravity Squares", new Font("Arial Black", Font.BOLD, 50), Vector.NULL, ColorE.WHITE);

    private double titleAnimationStep = 0;
    private final StopWatch watchTwinkleStars = new StopWatch();

    public TitleScene() {
        super("Title");
        DoOnce.DEFAULT6.reset();
        SettingManager.getDefaultManager().loadAll();
    }

    @Override
    public void draw() {
        //Adds all elements and creates all stars
        initElements();

        //Updates Star Positions on window resize
        updateStarPositionsOnResize();

        //Draw Background
        drawBackground(new ColorE(25, 25, 25, 255));

        super.draw();
    }

    @Override
    public void tick() {
        super.tick();

        //Set size range caps
        if (minSize.get() > maxSize.get()) minSize.set(maxSize.get());

        //Twinkle Stars
        watchTwinkleStars.start();
        Random random = new Random();
        if (watchTwinkleStars.hasTimePassedS(.25)) {
            for (ElementUI element : getElements()) {
                if (element instanceof PhysicsRectangle phys)
                    phys.getRectangle().setColor(new ColorE(255, random.nextInt(125, 255), 100, 255));
            }
            watchTwinkleStars.restart();
        }

        //Set Title Pos
        double yOffset = -40;
        title.setPos(getSize().getMultiplied(.5d).getAdded(title.getSize().getMultiplied(-.5)).getAdded(0, yOffset));
        title.setPos(title.getPos().getAdded(new Vector(0, Math.sin(titleAnimationStep) * 8)));
        titleAnimationStep += 0.05;
        if (titleAnimationStep >= Math.PI * 2) titleAnimationStep = 0;

        //Set Button Pos
        buttonStart.setPos(getSize().getMultiplied(.5d).getAdded(buttonStart.getSize().getMultiplied(-.5)).getAdded(0, title.getFont().getSize() + 30).getAdded(0, yOffset));
    }

    private void initElements() {
        DoOnce.DEFAULT6.run(() -> {
            //Create Stars
            Random random = new Random();
            for (int i = 0; i < 100; i++) {
                ColorE color = new ColorE(255, random.nextInt(125, 255), 100, 255);
                PhysicsRectangle obj = new PhysicsRectangle(new RectangleUI(new Vector(random.nextDouble(0, getSize().getX()), random.nextDouble(0, getWindow().getSize().getY())), new Vector(1, 1), color), 1, Vector.NULL, Vector.NULL);
                obj.setDisabled(true);
                obj.setTicking(false);
                addElements(obj);
            }

            //Add Widgets
            addElements(buttonStart, title, sliderSquareCount, sliderMinSize, sliderMaxSize, toggleBigSquare, toggleDoWallBounce, toggleDoCollisions);
        });
    }

    private void updateStarPositionsOnResize() {
        getWindow().doOnResize.run(() -> {
            Random random = new Random();
            for (ElementUI el : getElements()) {
                if (el instanceof PhysicsRectangle rect) {
                    rect.setPos(new Vector(random.nextDouble(0, getSize().getX()), random.nextDouble(0, getWindow().getSize().getY())));
                }
            }
        });
    }
}
