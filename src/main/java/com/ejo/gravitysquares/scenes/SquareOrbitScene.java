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
import java.util.ConcurrentModificationException;
import java.util.Random;

public class SquareOrbitScene extends Scene {

    private final ButtonUI buttonUI = new ButtonUI(Vector.NULL,new Vector(15,15),new ColorE(200,0,0,255),() -> getWindow().setScene(new TitleScene()));

    private final ArrayList<PhysicsRectangle> physicsSquares = new ArrayList<>();
    private PhysicsRectangle bigSquare = null;

    private final boolean wallBounce;

    public SquareOrbitScene(int squareCount, double sizeMin, double sizeMax, boolean bigSquare, boolean wallBounce) {
        super("Orbit Screen");
        DoOnce.default1.reset();

        this.wallBounce = wallBounce;

        //Create the little squares
        Random random = new Random();
        for (int i = 0; i < squareCount; i++) {
            double trueSize = sizeMin == sizeMax ? sizeMax : random.nextDouble(sizeMin, sizeMax);
            double startVelRange = 10;
            PhysicsRectangle shape = new PhysicsRectangle(
                    new RectangleUI(
                            Vector.NULL,
                            new Vector(trueSize,trueSize),
                            new ColorE(random.nextInt(0,255),random.nextInt(0,255),random.nextInt(0,255),255)),
                    trueSize*trueSize*trueSize,
                    new Vector(random.nextDouble(-startVelRange,startVelRange),random.nextDouble(-startVelRange,startVelRange)),
                    Vector.NULL);
            addElements(shape);
        }

        //Create the Big Square
        if (bigSquare) {
            int mul = 3;
            addElements(this.bigSquare = new PhysicsRectangle(
                    new RectangleUI(Vector.NULL,new Vector(sizeMax,sizeMax).getMultiplied(mul),ColorE.YELLOW),
                    sizeMax*sizeMax*sizeMax*mul*mul*mul,Vector.NULL,Vector.NULL));
        }
        addPhysicsObjects();

        //Add Button
        addElements(buttonUI);
    }

    @Override
    public void draw(Scene scene, Vector mousePos) {
    //Initialization
        DoOnce.default1.run(() -> {
            //Set random starting positions for Little Squares
            Random random = new Random();
            for (PhysicsRectangle obj : physicsSquares) {
                obj.setPos(new Vector(random.nextDouble(0,getWindow().getSize().getX()),random.nextDouble(0,getWindow().getSize().getY())));
            }

            //Set Big Square start in the middle
            if (bigSquare != null) bigSquare.setPos(getWindow().getSize().getMultiplied(.5).getAdded(bigSquare.getRectangle().getSize().getMultiplied(-.5)));

            //Create Stars
            for (int i = 0; i < 100; i++) {
                ColorE color = new ColorE(255, 255, 255,255);
                PhysicsRectangle obj = new PhysicsRectangle(new RectangleUI(new Vector(random.nextDouble(0,getWindow().getSize().getX()),random.nextDouble(0,getWindow().getSize().getY())),new Vector(1,1), color), 1,Vector.NULL,Vector.NULL);
                obj.disable(true);
                addElements(obj);
            }
        });

        //Set exit button to top right corner
        buttonUI.setPos(new Vector(getWindow().getSize().getX(),0).getAdded(-buttonUI.getSize().getX(),0));

        //Draw all screen objects
        super.draw(scene, mousePos);

        //Draw X for Exit Button
        QuickDraw.drawText("X",new Font("Arial",Font.PLAIN,14),buttonUI.getPos().getAdded(3,0),ColorE.WHITE);

        //Draw FPS/TPS
        QuickDraw.drawFPSTPS(this,new Vector(1,1),10,false);
    }

    @Override
    public void tick(Scene scene, Vector mousePos) {
        //Calculate and set the forces on each physics rectangle
        PhysicsUtil.calculateGravityForcesAndCollisions(this, physicsSquares, 1, wallBounce);

        //Calculate the forces/accelerations. Reset's the added forces after acceleration calculation
        super.tick(scene, mousePos);
    }

    @Override
    public void onKeyPress(Scene scene, int key, int scancode, int action, int mods) {
        super.onKeyPress(scene, key, scancode, action, mods);

        //Increase or Decrease the max tick rate for calculations using the + or - key; This is a debug feature
        if (key == Key.KEY_PLUS.getId() && action == Key.ACTION_PRESS) {
            getWindow().setMaxTPS(getWindow().getMaxTPS() + 5);
        }
        if (key == Key.KEY_MINUS.getId() && action == Key.ACTION_PRESS) {
            getWindow().setMaxTPS(getWindow().getMaxTPS() - 5);
        }
    }

    public void addPhysicsObjects() {
        for (ElementUI element : getElements()) {
            if (element instanceof PhysicsRectangle physicsObject)
                physicsSquares.add(physicsObject);
        }
    }

}