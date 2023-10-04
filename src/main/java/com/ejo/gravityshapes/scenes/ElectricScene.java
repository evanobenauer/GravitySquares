package com.ejo.gravityshapes.scenes;

import com.ejo.glowlib.math.Angle;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.math.VectorMod;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.misc.DoOnce;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.ElementUI;
import com.ejo.glowui.scene.elements.shape.LineUI;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowui.scene.elements.shape.RegularPolygonUI;
import com.ejo.glowui.scene.elements.widget.ButtonUI;
import com.ejo.glowui.util.Key;
import com.ejo.glowui.util.QuickDraw;
import com.ejo.gravityshapes.PhysicsUtil;
import com.ejo.gravityshapes.objects.PhysicsCharge;
import com.ejo.gravityshapes.objects.PhysicsPolygon;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class ElectricScene extends Scene {

    private final ButtonUI buttonX = new ButtonUI(Vector.NULL,new Vector(15,15),new ColorE(200,0,0,255), ButtonUI.MouseButton.LEFT,() -> getWindow().setScene(new TitleScene()));

    private final boolean doWallBounce;
    private final boolean doCollisions;
    private final boolean drawFieldLines;

    private double radius;

    public ElectricScene(int protonCount, int electronCount, double radius, boolean doWallBounce, boolean doCollisions, boolean drawFieldLines) {
        super("Electric Scene");
        DoOnce.DEFAULT1.reset();

        this.doWallBounce = doWallBounce;
        this.doCollisions = doCollisions;
        this.drawFieldLines = drawFieldLines;

        this.radius = radius;

        addProtons(protonCount);
        addElectrons(electronCount);
        addElements(buttonX);
    }

    @Override
    public void draw() {
        //Set exit button to top right corner
        buttonX.setPos(new Vector(getSize().getX(),0).getAdded(-buttonX.getSize().getX(),0));

        if (this.drawFieldLines) drawFieldLines(.05, getPhysicsObjects());

        //Draw all screen objects
        super.draw();

        //Draw X for Exit Button
        QuickDraw.drawText("X",new Font("Arial",Font.PLAIN,14), buttonX.getPos().getAdded(3,-2),ColorE.WHITE);
    }

    @Override
    public void tick() {
        initObjectPositions();

        for (PhysicsCharge obj : getPhysicsObjects()) {
            if (obj.isDisabled()) continue;

            //Do Collisions

            if (doCollisions) {
                for (PhysicsCharge otherObject : getPhysicsObjects()) {
                    if (obj.equals(otherObject) || otherObject.isDisabled()) continue;
                    if (PhysicsUtil.areObjectsColliding(obj, otherObject)) obj.doCollision(otherObject);
                }
            }


            //Do Wall Bounce
            if (doWallBounce) obj.doBounce(this);

            //Set Charge Force
            //Mass = Charge
            //G = k
            obj.setNetForce(PhysicsUtil.calculateElectricForce(obj, getPhysicsObjects(), 10));
        }

        //Calculate the forces/accelerations. Reset's the added forces after acceleration calculation
        super.tick();
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        super.onKeyPress(key, scancode, action, mods);
        if (key == Key.KEY_ESC.getId() && action == Key.ACTION_PRESS) {
            buttonX.getAction().run();
        }
    }

    private void addProtons(int objectCount) {
        double radius = 4;//this.radius;
        int vertexCount = 14;
        double charge = 200;

        double density = 1;
        double vol = (double) 4 /3*Math.PI*Math.pow(radius,3) * density; //Volume is calculated as a sphere

        Random random = new Random();
        for (int i = 0; i < objectCount; i++) {
            double startVelRange = 10;
            ColorE color = ColorE.BLUE;
            addElements(new PhysicsCharge(
                    new RegularPolygonUI(Vector.NULL, color, radius, vertexCount,new Angle(random.nextDouble(0,2*Math.PI))), vol,
                    charge,new Vector(random.nextDouble(-startVelRange,startVelRange),random.nextDouble(-startVelRange,startVelRange)), Vector.NULL));
        }
    }

    private void addElectrons(int objectCount) {
        double radius = 4;//this.radius;
        int vertexCount = 14;
        double charge = -200;

        double density = 1;
        double vol = (double) 4 /3*Math.PI*Math.pow(radius,3) * density; //Volume is calculated as a sphere

        Random random = new Random();
        for (int i = 0; i < objectCount; i++) {
            double startVelRange = 10;
            ColorE color = ColorE.RED;
            addElements(new PhysicsCharge(
                    new RegularPolygonUI(Vector.NULL, color, radius, vertexCount,new Angle(random.nextDouble(0,2*Math.PI))), vol,
                    charge,new Vector(random.nextDouble(-startVelRange,startVelRange),random.nextDouble(-startVelRange,startVelRange)), Vector.NULL));
        }
    }


    private void initObjectPositions() {
        DoOnce.DEFAULT1.run(this::setRandomObjectPositions);
    }

    private void setRandomObjectPositions() {
        Random random = new Random();
        for (PhysicsCharge obj : getPhysicsObjects())
            obj.setPos(new Vector(random.nextDouble(0,getSize().getX()),random.nextDouble(0,getSize().getY())));
    }


    public ArrayList<PhysicsCharge> getPhysicsObjects() {
        ArrayList<PhysicsCharge> rectangles = new ArrayList<>();
        for (ElementUI elementUI : getElements()) {
            if (elementUI instanceof PhysicsCharge polygon) rectangles.add(polygon);
        }
        return rectangles;
    }

    private void drawFieldLines(double lineDensity, ArrayList<PhysicsCharge> physicsPolygons) {
        int inverseDensity = (int) (1/lineDensity);
        int windowWidth = (int)getWindow().getScaledSize().getX();
        int windowHeight = (int)getWindow().getScaledSize().getY();

        for (int x = 0; x < windowWidth / inverseDensity + 1; x++) {
            for (int y = 0; y < windowHeight / inverseDensity + 1; y++) {
                VectorMod gravityForce = Vector.NULL.getMod();
                for (PhysicsCharge otherObject : physicsPolygons) {
                    if (!otherObject.isDisabled()) {
                        Vector gravityFromOtherObject = PhysicsUtil.calculateElectricField(100,otherObject,new Vector(x,y).getMultiplied(inverseDensity));
                        if (!(String.valueOf(gravityFromOtherObject.getMagnitude())).equals("NaN")) gravityForce.add(gravityFromOtherObject);
                    }
                }
                LineUI lineUI = new LineUI(new Vector(x,y).getMultiplied(inverseDensity),gravityForce.getUnitVector().getMultiplied(Math.min(Math.max(gravityForce.getMagnitude(),.1),1)*10).getAdded(new Vector(x,y).getMultiplied(inverseDensity)),ColorE.WHITE.alpha(100), LineUI.Type.PLAIN,.5);
                lineUI.draw();
            }
        }
    }

}