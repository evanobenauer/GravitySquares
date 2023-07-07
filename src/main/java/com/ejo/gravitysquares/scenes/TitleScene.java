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
import com.ejo.glowui.util.QuickDraw;
import com.ejo.gravitysquares.objects.PhysicsRectangle;

import java.awt.*;
import java.util.ConcurrentModificationException;
import java.util.Random;

public class TitleScene extends Scene {

    private final Setting<Integer> squareCount = new Setting<>("squareCount",50);
    private final Setting<Double> minSize = new Setting<>("minSize",3d);
    private final Setting<Double> maxSize = new Setting<>("maxSize",15d);
    private final Setting<Boolean> bigSquare = new Setting<>("bigSquare",false);
    private final Setting<Boolean> wallBounce = new Setting<>("doBounce",true);

    private final SliderUI<Integer> squareCountSlider = new SliderUI<>(this,"Square Count",new Vector(10,10),new Vector(300,20),ColorE.BLUE,squareCount,0,500,1, SliderUI.Type.INTEGER,true);
    private final SliderUI<Double> minSizeSlider = new SliderUI<>(this,"Min Size",new Vector(10,40),new Vector(300,20),ColorE.BLUE,minSize,0.1d,50d,.1, SliderUI.Type.FLOAT,true);
    private final SliderUI<Double> maxSizeSlider = new SliderUI<>(this,"Max Size",new Vector(10,70),new Vector(300,20),ColorE.BLUE,maxSize,1.1d,50d,.1, SliderUI.Type.FLOAT,true);

    private final ToggleUI bigSquareToggle = new ToggleUI(this,"Big Square",new Vector(10,130),new Vector(300,20),ColorE.BLUE,bigSquare);
    private final ToggleUI wallBounceToggle = new ToggleUI(this,"Do Wall Bounce",new Vector(10,100),new Vector(300,20),ColorE.BLUE, wallBounce);

    private final ButtonUI button = new ButtonUI(this,"Start!",Vector.NULL,new Vector(200,60),new ColorE(0,125,200,200),() -> {
        getWindow().setScene(new SquareOrbitScene(squareCount.get(),minSize.get(),maxSize.get(),bigSquare.get(),wallBounce.get()));
        SettingManager.getDefaultManager().saveAll();
    });

    private final TextUI title = new TextUI(this,"Gravity Squares",new Font("Arial Black",Font.BOLD,50),Vector.NULL,ColorE.WHITE);

    public TitleScene() {
        super("Title");
        DoOnce.default6.reset();
        watch.start();
        SettingManager.getDefaultManager().loadAll();
    }

    @Override
    public void draw() {
        //Draw Background
        QuickDraw.drawRect(this,Vector.NULL,getWindow().getSize(),new ColorE(25,25,25,255));

        DoOnce.default6.run(() -> {
            //Create Stars
            Random random = new Random();
            for (int i = 0; i < 100; i++) {
                ColorE color = new ColorE(255, random.nextInt(125,255), 100,255);
                PhysicsRectangle obj = new PhysicsRectangle(new RectangleUI(this,new Vector(random.nextDouble(0,getWindow().getSize().getX()),random.nextDouble(0,getWindow().getSize().getY())),new Vector(1,1), color), 1,Vector.NULL,Vector.NULL);
                obj.disable(true);
                addElements(obj);
            }

            //Add Widgets
            addElements(button,title,squareCountSlider,minSizeSlider,maxSizeSlider,bigSquareToggle,wallBounceToggle);
        });

        super.draw();
    }

    private double step = 0;
    private final StopWatch watch = new StopWatch();

    @Override
    public void tick() {
        try {
            super.tick();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }

        //Set size range caps
        if (minSize.get() > maxSize.get()) minSize.set(maxSize.get());

        //Twinkle Stars
        Random random = new Random();
        if (watch.hasTimePassedS(.25)) {
            for (ElementUI element : getElements()) {
                if (element instanceof PhysicsRectangle phys) {
                    phys.getRectangle().setColor(new ColorE(255, random.nextInt(125, 255), 100, 255));
                }
            }
            watch.restart();
        }

        double yOffset = -40;

        //Set Title Pos
        title.setPos(getWindow().getSize().getMultiplied(.5d).getAdded(title.getSize().getMultiplied(-.5)).getAdded(0,yOffset));
        title.setPos(title.getPos().getAdded(new Vector(0,Math.sin(step) * 8)));
        step += 0.05;
        if (step >= Math.PI*2) step = 0;

        //Set Button Pos
        button.setPos(getWindow().getSize().getMultiplied(.5d).getAdded(button.getSize().getMultiplied(-.5)).getAdded(0,title.getFont().getSize() + 30).getAdded(0,yOffset));
    }
}
