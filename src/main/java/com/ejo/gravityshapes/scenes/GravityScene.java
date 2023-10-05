package com.ejo.gravityshapes.scenes;

import com.ejo.glowlib.math.Angle;
import com.ejo.glowlib.math.VectorMod;
import com.ejo.glowlib.misc.DoOnce;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.ElementUI;
import com.ejo.glowui.scene.elements.shape.LineUI;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowui.scene.elements.shape.RegularPolygonUI;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsObjectUI;
import com.ejo.glowui.scene.elements.widget.ButtonUI;
import com.ejo.glowui.util.DrawUtil;
import com.ejo.glowui.util.Key;
import com.ejo.glowui.util.Mouse;
import com.ejo.glowui.util.QuickDraw;
import com.ejo.gravityshapes.PhysicsUtil;
import com.ejo.gravityshapes.objects.PhysicsPolygon;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GravityScene extends Scene {

    private final ButtonUI buttonX = new ButtonUI(Vector.NULL,new Vector(15,15),new ColorE(200,0,0,255), ButtonUI.MouseButton.LEFT,() -> getWindow().setScene(new TitleScene()));

    private PhysicsPolygon bigObject = null;

    private final double sizeMin;
    private final double sizeMax;

    private final boolean doWallBounce;
    private final boolean doCollisions;
    private final boolean drawFieldLines;

    private boolean shooting;
    private Vector shootPos;
    private Vector shootVelocity;
    private double shootSize;
    private double shootSpin;
    private int shootVertices;
    private final DoOnce shooter = new DoOnce();
    private final DoOnce shooterInitializer = new DoOnce();

    public GravityScene(int objectCount, double sizeMin, double sizeMax, boolean bigObject, boolean doWallBounce, boolean doCollisions, boolean drawFieldLines) {
        super("Orbit Scene");
        DoOnce.DEFAULT1.reset();

        this.sizeMin = sizeMin;
        this.sizeMax = sizeMax;

        this.doWallBounce = doWallBounce;
        this.doCollisions = doCollisions;
        this.drawFieldLines = drawFieldLines;

        this.shooting = false;
        this.shootPos = Vector.NULL;
        this.shootVelocity = Vector.NULL;
        this.shootSize = 0;
        this.shootSpin = 0;
        this.shootVertices = 0;
        shooter.run(() -> {});

        addStars();
        addPhysicsObjects(objectCount,sizeMin,sizeMax);
        if (bigObject) addBigObject(sizeMax);
        addElements(buttonX);
    }

    @Override
    public void draw() {
        //Set exit button to top right corner
        buttonX.setPos(new Vector(getSize().getX(),0).getAdded(-buttonX.getSize().getX(),0));

        updateStarPositionsOnResize();

        if (this.drawFieldLines) drawFieldLines(.05, getPhysicsObjects());

        //Draw all screen objects
        super.draw();

        //Draw Shooting Object Visual
        if (shooting) {
            RegularPolygonUI polygonUI = new RegularPolygonUI(shootPos, DrawUtil.GLOW_BLUE,true,shootSize,shootVertices,new Angle(shootSpin,true));
            polygonUI.draw();
            LineUI line = new LineUI(shootPos,shootPos.getAdded(shootPos.getAdded(getWindow().getScaledMousePos().getMultiplied(-1))),ColorE.WHITE, LineUI.Type.DOTTED,2);
            line.draw();
            shootSpin += 1;
        }

        //Draw X for Exit Button
        QuickDraw.drawText("X",new Font("Arial",Font.PLAIN,14), buttonX.getPos().getAdded(3,-2),ColorE.WHITE);
    }

    @Override
    public void tick() {
        initObjectPositions();

        for (PhysicsPolygon obj : getPhysicsObjects()) {
            if (obj.isDisabled()) continue;

            //Do Collisions
            if (doCollisions) {
                for (PhysicsPolygon otherObject : getPhysicsObjects()) {
                    if (obj.equals(otherObject) || otherObject.isDisabled()) continue;
                    if (PhysicsUtil.areObjectsColliding(obj, otherObject)) obj.doCollision(otherObject);
                }
            } else {
                for (PhysicsPolygon otherObject : getPhysicsObjects()) {
                    if (obj.equals(otherObject) || otherObject.isDisabled()) continue;
                    if (PhysicsUtil.areObjectsColliding(obj,otherObject)) obj.spinObjectFromCollision(otherObject,50);
                }
            }

            //Do Wall Bounce
            if (doWallBounce) obj.doBounce(this);

            //Set Gravity Force
            obj.setNetForce(PhysicsUtil.calculateGravityForce(obj, getPhysicsObjects(), 1));
        }

        //Run Shoot New Object Computations
        updateShootObject();

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

    @Override
    public void onMouseClick(int button, int action, int mods, Vector mousePos) {
        super.onMouseClick(button, action, mods, mousePos);
        if (button == Mouse.BUTTON_LEFT.getId()) {
            if (action == Mouse.ACTION_CLICK) {
                boolean isMouseFree = true;
                for (PhysicsObjectUI obj : getPhysicsObjects()) {
                    if (obj.isMouseOver()) {
                        isMouseFree = false;
                        break;
                    }
                }
                if (isMouseFree) {
                    shootPos = mousePos;
                    shooting = true;
                }
            }
            if (action == Mouse.ACTION_RELEASE) shooting = false;
        }
    }


    private void drawFieldLines(double lineDensity, ArrayList<PhysicsPolygon> physicsPolygons) {
        int inverseDensity = (int) (1/lineDensity);
        int windowWidth = (int)getWindow().getScaledSize().getX();
        int windowHeight = (int)getWindow().getScaledSize().getY();

        for (int x = 0; x < windowWidth / inverseDensity + 1; x++) {
            for (int y = 0; y < windowHeight / inverseDensity + 1; y++) {
                VectorMod gravityForce = Vector.NULL.getMod();
                for (PhysicsPolygon otherObject : physicsPolygons) {
                    if (!otherObject.isDisabled()) {
                        Vector gravityFromOtherObject = PhysicsUtil.calculateGravitationalField(1,otherObject,new Vector(x,y).getMultiplied(inverseDensity));
                        if (!(String.valueOf(gravityFromOtherObject.getMagnitude())).equals("NaN")) gravityForce.add(gravityFromOtherObject);
                    }
                }
                LineUI lineUI = new LineUI(new Vector(x,y).getMultiplied(inverseDensity),gravityForce.getUnitVector().getMultiplied(Math.min(Math.max(gravityForce.getMagnitude(),.1),1)*10).getAdded(new Vector(x,y).getMultiplied(inverseDensity)),ColorE.WHITE.alpha(100), LineUI.Type.PLAIN,.5);
                lineUI.draw();
            }
        }
    }

    private void updateShootObject() {
        if (shooting) {
            shooter.reset();
            shooterInitializer.run(() -> {
                Random random = new Random();
                shootSize = (sizeMin == sizeMax) ? sizeMax : random.nextDouble(sizeMin, sizeMax);
                shootVertices = random.nextInt(3, 8);
            });
        } else {
            shooterInitializer.reset();
            shooter.run(() -> {
                //Shoot the object
                Random random = new Random();
                shootVelocity = shootPos.getAdded(getWindow().getScaledMousePos().getMultiplied(-1)).getMultiplied(.75);
                ColorE randomColor = new ColorE(random.nextInt(25,255),random.nextInt(25,255),random.nextInt(25,255),255);
                PhysicsPolygon poly = new PhysicsPolygon(new RegularPolygonUI(shootPos,randomColor,false,shootSize,shootVertices,new Angle(shootSpin,true)),(double) 4 /3*Math.PI*Math.pow(shootSize,3),shootVelocity,Vector.NULL);
                queueAddElements(poly);
            });
        }
    }


    private void initObjectPositions() {
        DoOnce.DEFAULT1.run(() -> {
            setRandomObjectPositions();

            //Set Big Object start in the middle
            if (bigObject != null) bigObject.setPos(getSize().getMultiplied(.5));
        });
    }

    private void setRandomObjectPositions() {
        Random random = new Random();
        for (PhysicsPolygon obj : getPhysicsObjects())
            obj.setPos(new Vector(random.nextDouble(0,getSize().getX()),random.nextDouble(0,getSize().getY())));
        for (ElementUI el : getElements()) {
            if (el instanceof RectangleUI rect && rect.shouldRender() && !rect.shouldTick()) {
                rect.setPos(new Vector(random.nextDouble(0,getSize().getX()),random.nextDouble(0,getWindow().getSize().getY())));
            }
        }
    }

    private void updateStarPositionsOnResize() {
        getWindow().doOnResize.run(() -> {
            Random random = new Random();
            for (ElementUI el : getElements()) {
                if (el instanceof RectangleUI rect && rect.shouldRender() && !rect.shouldTick()) {
                    rect.setPos(new Vector(random.nextDouble(0,getSize().getX()),random.nextDouble(0,getWindow().getSize().getY())));
                }
            }
        });
    }


    private void addPhysicsObjects(int objectCount, double sizeMin, double sizeMax) {
        Random random = new Random();
        for (int i = 0; i < objectCount; i++) {
            double trueSize = (sizeMin == sizeMax) ? sizeMax : random.nextDouble(sizeMin, sizeMax);
            double startVelRange = 10;
            ColorE randomColor = new ColorE(random.nextInt(25,255),random.nextInt(25,255),random.nextInt(25,255),255);
            addElements(new PhysicsPolygon(
                    new RegularPolygonUI(Vector.NULL, randomColor, trueSize, random.nextInt(3,8),new Angle(random.nextDouble(0,2*Math.PI))), (double) 4 /3*Math.PI*Math.pow(trueSize,3),
                    new Vector(random.nextDouble(-startVelRange,startVelRange),random.nextDouble(-startVelRange,startVelRange)), Vector.NULL));
        }
    }

    private void addBigObject(double sizeMax) {
        int mul = 3;
        addElements(this.bigObject = new PhysicsPolygon(
                new RegularPolygonUI(Vector.NULL,ColorE.YELLOW, sizeMax * mul,30,new Angle(0)),
                sizeMax*sizeMax*sizeMax*mul*mul*mul,Vector.NULL,Vector.NULL));
    }

    private void addStars() {
        for (int i = 0; i < 100; i++) {
            ColorE color = new ColorE(255, 255, 255,255);
            RectangleUI obj = new RectangleUI(Vector.NULL,new Vector(1,1), color);
            obj.setTicking(false);
            if (!this.drawFieldLines) addElements(obj);
        }
    }


    public ArrayList<PhysicsPolygon> getPhysicsObjects() {
        ArrayList<PhysicsPolygon> rectangles = new ArrayList<>();
        for (ElementUI elementUI : getElements()) {
            if (elementUI instanceof PhysicsPolygon polygon) rectangles.add(polygon);
        }
        return rectangles;
    }

}