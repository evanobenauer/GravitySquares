package com.ejo.gravitysquares.scenes;

import com.ejo.glowlib.misc.DoOnce;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.ElementUI;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowui.scene.elements.widget.ButtonUI;
import com.ejo.glowui.util.Key;
import com.ejo.glowui.util.QuickDraw;
import com.ejo.gravitysquares.PhysicsUtil;
import com.ejo.gravitysquares.objects.PhysicsRectangle;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class SquareOrbitScene extends Scene {

    private final ButtonUI buttonX = new ButtonUI(Vector.NULL,new Vector(15,15),new ColorE(200,0,0,255), ButtonUI.MouseButton.LEFT,() -> getWindow().setScene(new TitleScene()));

    private PhysicsRectangle bigSquare = null;

    private final boolean doWallBounce;
    private final boolean doCollisions;

    public SquareOrbitScene(int squareCount, double sizeMin, double sizeMax, boolean bigSquare, boolean doWallBounce, boolean doCollisions) {
        super("Orbit Screen");
        DoOnce.DEFAULT1.reset();

        this.doWallBounce = doWallBounce;
        this.doCollisions = doCollisions;

        addStars();
        addPhysicsSquares(squareCount,sizeMin,sizeMax);
        if (bigSquare) addBigSquare(sizeMax);
        addElements(buttonX);
    }

    @Override
    public void draw() {
        //Set exit button to top right corner
        buttonX.setPos(new Vector(getSize().getX(),0).getAdded(-buttonX.getSize().getX(),0));

        updateStarPositionsOnResize();

        //Draw all screen objects
        super.draw();

        //Draw X for Exit Button
        QuickDraw.drawText("X",new Font("Arial",Font.PLAIN,14), buttonX.getPos().getAdded(3,-2),ColorE.WHITE);
        QuickDraw.drawFPSTPS(this,new Vector(1,1),10,false);
    }

    @Override
    public void tick() {
        initObjectPositions();

        for (PhysicsRectangle rect : getPhysicsSquares()) {
            if (rect.isDisabled()) continue;
            rect.setNetForce(PhysicsUtil.calculateGravityForceAndCollide(this, rect,getPhysicsSquares(), 1, doWallBounce, doCollisions));
        }

        //Calculate the forces/accelerations. Reset's the added forces after acceleration calculation
        super.tick();
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        super.onKeyPress(key, scancode, action, mods);

        //Increase or Decrease the max tick rate for calculations using the + or - key; This is a debug feature
        if (key == Key.KEY_EQUALS.getId() && action == Key.ACTION_PRESS) {
            getWindow().setMaxTPS(getWindow().getMaxTPS() + 5);
        }
        if (key == Key.KEY_MINUS.getId() && action == Key.ACTION_PRESS) {
            getWindow().setMaxTPS(getWindow().getMaxTPS() - 5);
        }
    }


    private void addPhysicsSquares(int squareCount, double sizeMin, double sizeMax) {
        Random random = new Random();
        for (int i = 0; i < squareCount; i++) {
            double trueSize = (sizeMin == sizeMax) ? sizeMax : random.nextDouble(sizeMin, sizeMax);
            double startVelRange = 10;
            ColorE randomColor = new ColorE(random.nextInt(25,255),random.nextInt(25,255),random.nextInt(25,255),255);
            addElements(new PhysicsRectangle(
                    new RectangleUI(Vector.NULL, new Vector(trueSize,trueSize), randomColor), trueSize*trueSize*trueSize,
                    new Vector(random.nextDouble(-startVelRange,startVelRange),random.nextDouble(-startVelRange,startVelRange)), Vector.NULL));
        }
    }

    private void addBigSquare(double sizeMax) {
        int mul = 3;
        addElements(this.bigSquare = new PhysicsRectangle(
                new RectangleUI(Vector.NULL,new Vector(sizeMax,sizeMax).getMultiplied(mul),ColorE.YELLOW),
                sizeMax*sizeMax*sizeMax*mul*mul*mul,Vector.NULL,Vector.NULL));
    }

    private void addStars() {
        for (int i = 0; i < 100; i++) {
            ColorE color = new ColorE(255, 255, 255,255);
            PhysicsRectangle obj = new PhysicsRectangle(new RectangleUI(Vector.NULL,new Vector(1,1), color), 1,Vector.NULL,Vector.NULL);
            obj.setDisabled(true);
            obj.setTicking(false);
            addElements(obj);
        }
    }


    private void initObjectPositions() {
        DoOnce.DEFAULT1.run(() -> {
            //Sets the random positions of the stars AND the physics squares
            setRandomSquarePositions();

            //Set Big Square start in the middle
            if (bigSquare != null) bigSquare.setPos(getSize().getMultiplied(.5).getAdded(bigSquare.getRectangle().getSize().getMultiplied(-.5)));

        });
    }

    private void setRandomSquarePositions() {
        Random random = new Random();
        for (PhysicsRectangle obj : getPhysicsSquares())
            obj.setPos(new Vector(random.nextDouble(0,getSize().getX()),random.nextDouble(0,getSize().getY())));
    }

    private void updateStarPositionsOnResize() {
        getWindow().doOnResize.run(() -> {
            Random random = new Random();
            for (ElementUI el : getElements()) {
                if (el instanceof PhysicsRectangle rect && rect.isDisabled() && rect.shouldRender()) {
                    rect.setPos(new Vector(random.nextDouble(0,getSize().getX()),random.nextDouble(0,getWindow().getSize().getY())));
                }
            }
        });
    }


    private ArrayList<PhysicsRectangle> getPhysicsSquares() {
        ArrayList<PhysicsRectangle> rectangles = new ArrayList<>();
        for (ElementUI elementUI : getElements()) if (elementUI instanceof PhysicsRectangle rectangle) {
            rectangles.add(rectangle);
        }
        return rectangles;
    }

}