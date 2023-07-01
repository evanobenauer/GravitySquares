package com.ejo.orbit;

import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.ElementUI;
import com.ejo.glowui.scene.elements.shape.IShape;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsControllableUI;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsObjectUI;
import com.ejo.glowui.util.Key;
import com.ejo.glowui.util.QuickDraw;
import org.lwjgl.glfw.GLFW;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.math.VectorMod;
import com.ejo.glowlib.misc.ColorE;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class SceneOrbit extends Scene {

    private final ArrayList<PhysicsObjectUI> physicsObjects = new ArrayList<>();

    private final PhysicsObjTest controllableObject = new PhysicsObjTest(
            new RectangleUI(this,new Vector(1000,500),new Vector(66,66),ColorE.GREEN),
            600f,
            Vector.NULL,
            Vector.NULL);

    public SceneOrbit() {
        super("Orbit Screen");
        addElements(controllableObject);
        Random random = new Random();
        Random randomM = new Random();
        for (int i = 0; i < 100; i++) {
            double massSize = randomM.nextDouble(1,3f);
            PhysicsObjTest shape = new PhysicsObjTest(
                    new RectangleUI(this,new Vector(10 + i * 10, i + 10),new Vector(massSize,massSize).getMultiplied(10),new ColorE(random.nextInt(0,255),random.nextInt(0,255),random.nextInt(0,255),255)),
                    massSize*massSize*massSize,
                    Vector.NULL,
                    Vector.NULL);
            addElements(shape);
        }
        addPhysicsObjects();
    }

    int moveX = 0;
    int moveY = 0;

    boolean controllable = false;

    @Override
    public void draw() {
        super.draw();
        QuickDraw.drawFPSTPS(this,new Vector(10,10),20,false);
    }

    @Override
    public void tick() {
        calculateGravityForces(physicsObjects);
        super.tick(); //Calculates the forces/accelerations. Reset's the added forces after acceleration calculation
        if (Key.KEY_RIGHT.isKeyDown()) moveX += 10;
        if (Key.KEY_LEFT.isKeyDown()) moveX -= 10;
        if (Key.KEY_UP.isKeyDown()) moveY -= 10;
        if (Key.KEY_DOWN.isKeyDown()) moveY += 10;
        if (controllable) {
            controllableObject.setVelocity(Vector.NULL);
            controllableObject.setPos(new Vector(1000 + moveX, 500 + moveY)); //Key planet 1 steady
        }
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        super.onKeyPress(key, scancode, action, mods);
        if (key == GLFW.GLFW_KEY_G && action == GLFW.GLFW_PRESS) {
            controllableObject.setDisabled(!controllableObject.isDisabled());
        }
        if (key == GLFW.GLFW_KEY_ENTER && action == GLFW.GLFW_PRESS) {
            controllable = !controllable;
        }
        if (key == Key.KEY_PLUS.getId() && action == Key.ACTION_PRESS) {
            getWindow().setMaxTPS(getWindow().getMaxTPS() + 5);
        }
        if (key == Key.KEY_MINUS.getId() && action == Key.ACTION_PRESS) {
            getWindow().setMaxTPS(getWindow().getMaxTPS() - 5);
        }
    }


    public void calculateGravityForces(ArrayList<PhysicsObjectUI> objects) {
        for (PhysicsObjectUI obj : objects) {
            if (!obj.isDisabled()) {
                ((PhysicsObjTest)obj).swapVelocity();
                VectorMod gravityForce = new VectorMod(Vector.NULL);

                //Calculate the force on obj from every other object in the list
                for (PhysicsObjectUI obj2 : objects) {
                    if (!obj.equals(obj2) && !obj2.isDisabled()) {
                        double G = 300;//9000;//6.67*Math.pow(10,-11);
                        Vector objectDistance = calculateVectorBetweenObjects(obj2, obj);
                        Vector objForce = objectDistance
                                .getUnitVector()
                                .getMultiplied(G * obj.getMass() * obj2.getMass() / Math.pow(objectDistance.getMagnitude(), 2));
                        if (!(String.valueOf(objForce.getMagnitude())).equals("NaN")) gravityForce.add(objForce);

                        if (areObjectsColliding(obj,obj2) && !obj2.equals(controllableObject)) {
                            //obj2.setDisabled(false);
                            //obj2.setRendered(false);
                            //obj.setMass(obj.getMass() + obj2.getMass());
                        }
                    }
                }

                obj.setNetForce(obj.getNetForce().getAdded(gravityForce));
            }
        }
    }

    public boolean areObjectsColliding(PhysicsObjectUI obj1, PhysicsObjectUI obj2) {
        return obj1.isColliding(obj2);
    }

    public Vector calculateVectorBetweenObjects(PhysicsObjectUI object1, PhysicsObjectUI object2) {
        return object1.getCenter().getAdded(object2.getCenter().getMultiplied(-1));
    }

    public void addPhysicsObjects() {
        for (ElementUI element : getElements()) {
            if (element instanceof PhysicsObjectUI physicsObject && !(element instanceof PhysicsControllableUI)) physicsObjects.add(physicsObject);
        }
    }

}