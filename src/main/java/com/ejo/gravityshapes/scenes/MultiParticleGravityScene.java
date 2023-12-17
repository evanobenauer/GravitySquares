package com.ejo.gravityshapes.scenes;

import com.ejo.glowlib.math.Angle;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.math.VectorMod;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.misc.DoOnce;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.ElementUI;
import com.ejo.glowui.scene.elements.shape.CircleUI;
import com.ejo.glowui.scene.elements.shape.LineUI;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowui.scene.elements.shape.RegularPolygonUI;
import com.ejo.glowui.scene.elements.widget.ButtonUI;
import com.ejo.glowui.util.Key;
import com.ejo.glowui.util.Mouse;
import com.ejo.glowui.util.render.QuickDraw;
import com.ejo.gravityshapes.Util;
import com.ejo.gravityshapes.objects.PhysicsCircle;
import com.ejo.uiphysics.elements.PhysicsObjectUI;
import com.ejo.uiphysics.util.GravityUtil;
import com.ejo.uiphysics.util.VectorUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

public class MultiParticleGravityScene extends Scene {

    private final ButtonUI buttonX = new ButtonUI(Vector.NULL,new Vector(15,15),new ColorE(200,0,0,255), ButtonUI.MouseButton.LEFT,() -> getWindow().setScene(new TitleScene()));

    private final boolean doWallBounce;
    private final boolean doCollisions;
    private final boolean drawFieldLines;

    private boolean shooting;
    private boolean shouldRenderShooting;

    private Vector shootPos;
    private Vector shootVelocity;
    private double shootSize;
    private double shootSpin;
    private int shootVertices;
    private final DoOnce shooter = new DoOnce();
    private final DoOnce shooterInitializer = new DoOnce();

    private final int vertexLow = 29, vertexHigh = 30;

    public MultiParticleGravityScene(int objectCount, double sizeMin, double sizeMax, boolean doWallBounce, boolean drawFieldLines) {
        super("Orbit Scene");
        DoOnce.DEFAULT1.reset();

        this.doWallBounce = doWallBounce;
        this.doCollisions = false;
        this.drawFieldLines = drawFieldLines;

        this.shooting = false;
        this.shouldRenderShooting = false;
        this.shootPos = Vector.NULL;
        this.shootVelocity = Vector.NULL;
        this.shootSize = sizeMin;
        this.shootSpin = 0;
        this.shootVertices = 0;
        this.shooter.run(() -> {});

        addStars();
        //addPhysicsObjects(objectCount,sizeMin,sizeMax);
        addPhysicsObjects(objectCount * 20,sizeMin,sizeMax);
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
        if (shooting && shouldRenderShooting) drawShootingObject();

        //Draw X for Exit Button
        QuickDraw.drawText("X",new Font("Arial",Font.PLAIN,14), buttonX.getPos().getAdded(3,-2),ColorE.WHITE);
    }

    @Override
    public void tick() {
        initObjectPositions();


        for (PhysicsCircle obj : getPhysicsObjects()) {
            if (obj.isPhysicsDisabled()) continue;

            //Debug Vectors
            obj.setDebugVectorForceScale((double) 1 /100);
            obj.setDebugVectorCap(100);


            //Set Gravity Force
            if (Mouse.BUTTON_LEFT.isButtonDown()) applyGravityTowardsCursor(obj,getWindow().getScaledMousePos(),1);
            if (Mouse.BUTTON_RIGHT.isButtonDown()) applyGravityTowardsCursor(obj,getWindow().getScaledMousePos(),-1);

            //Do Wall Bounce
            if (doWallBounce) obj.doWallBounce(this,.1);
        }

        //Run Shoot New Object Computations
        updateShootObject();

        //Calculate the forces/accelerations. Reset's the added forces after acceleration calculation
        super.tick();
    }

    private void applyGravityTowardsCursor(PhysicsCircle obj, Vector mousePos, double G) {
        int mouseMass = 1000000;
        Vector distanceVector = VectorUtil.calculateVectorBetweenObjectAndPoint(obj,mousePos).getMultiplied(-1);
        Vector force = distanceVector.getUnitVector().getMultiplied(G * mouseMass * obj.getMass() / (distanceVector.getMagnitude() * distanceVector.getMagnitude()));
        if (distanceVector.getMagnitude() > 10) obj.addForce(force);
    }

    private void doCollision(PhysicsCircle obj1, PhysicsCircle obj2, double mRestitution, double fCoefficient) {
        //Obj1 is reference frame. Obj2 is observed object
        Vector dirVec = VectorUtil.calculateVectorBetweenObjects(obj2,obj1);

        Vector uParallel = dirVec.getUnitVector(); //From Obj1 to Obj2
        Vector uPerpendicular = uParallel.getCross(Vector.K); //From Obj1 to Obj2

        //Calculate Relative Velocity
        Vector relativeVelocity = obj2.getVelocity().getSubtracted(obj1.getVelocity());
        Vector relativeVelocityPerpendicular = uPerpendicular.getMultiplied(relativeVelocity.getDot(uPerpendicular));

        //Calculate Relative Force
        Vector relativeForce = obj2.getNetForce().getSubtracted(obj1.getNetForce());
        Vector relativeForceParallel = uParallel.getMultiplied(relativeForce.getDot(uParallel));

        //Calculate Parallel Velocities
        Vector obj1VelParallel = uParallel.getMultiplied(obj1.getVelocity().getDot(uParallel));
        Vector obj2VelParallel = uParallel.getMultiplied(obj2.getVelocity().getDot(uParallel));

        //Calculate Parallel Momentum
        Vector obj1MomentumParallel = obj1VelParallel.getMultiplied(obj1.getMass());
        Vector obj2MomentumParallel = obj2VelParallel.getMultiplied(obj2.getMass());
        Vector totalMomentumParallel = obj1MomentumParallel.getAdded(obj2MomentumParallel);

        //Calculate Post-Collision Obj2 Velocity
        Vector nObj2VelParallel = totalMomentumParallel.getSubtracted(obj2VelParallel.getSubtracted(obj1VelParallel).getMultiplied(obj1.getMass() * mRestitution)).getMultiplied(1 / (obj2.getMass() + obj1.getMass()));

        //Calculate Object Parallel Changes in Velocity Post-Collision
        Vector nObj2VelocityDiffParallel = nObj2VelParallel.getSubtracted(obj2VelParallel);
        Vector nObj1VelocityDiffParallel = nObj2VelocityDiffParallel.getMultiplied(-obj2.getMass()/obj1.getMass());

        //Calculate perpendicular friction between objects
        Vector nObj2FrictionForce = relativeVelocityPerpendicular.getMagnitude() > 1 ? relativeVelocityPerpendicular.getUnitVector().getMultiplied(-1).getMultiplied(relativeForceParallel.getMagnitude() * fCoefficient) : Vector.NULL;
        Vector nObj1FrictionForce = relativeVelocityPerpendicular.getMagnitude() > 1 ? relativeVelocityPerpendicular.getUnitVector().getMultiplied(relativeForceParallel.getMagnitude() * fCoefficient) : Vector.NULL;

        //Set Collision boundary pushing
        double overlap = obj1.getCircle().getRadius() + obj2.getCircle().getRadius() - dirVec.getMagnitude();
        obj1.setPos(obj1.getPos().getAdded(uParallel.getMultiplied(-overlap/2)));
        obj2.setPos(obj2.getPos().getAdded(uParallel.getMultiplied(overlap/2)));

        //Apply Friction Force
        obj1.addForce(nObj1FrictionForce);
        obj2.addForce(nObj2FrictionForce);

        //Set both object velocities to their new variants.
        obj1.setVelocity(obj1.getVelocity().getAdded(nObj1VelocityDiffParallel));
        obj2.setVelocity(obj2.getVelocity().getAdded(nObj2VelocityDiffParallel));
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        super.onKeyPress(key, scancode, action, mods);
        if (action == Key.ACTION_RELEASE) return;
        if (key == Key.KEY_ESC.getId() && action == Key.ACTION_PRESS) {
            buttonX.getAction().run();
        }
        if (shooting) {
            if (key == Key.KEY_UP.getId()) shootSize += 1;
            if (key == Key.KEY_DOWN.getId()) shootSize -= 1;
        }
    }

    @Override
    public void onMouseClick(int button, int action, int mods, Vector mousePos) {
        super.onMouseClick(button, action, mods, mousePos);
        if (button == Mouse.BUTTON_LEFT.getId()) {
            if (action == Mouse.ACTION_CLICK) {
                boolean isMouseFree = true;
                for (PhysicsObjectUI obj : getPhysicsObjects()) {
                    if (obj.isMouseOver() && !obj.isPhysicsDisabled()) {
                        isMouseFree = false;
                        break;
                    }
                }
                if (isMouseFree) {
                    shootPos = mousePos;
                    //shooting = true;
                }
            }
            if (action == Mouse.ACTION_RELEASE) shooting = false;
        }
    }

    @Override
    public void onMouseScroll(int scroll, Vector mousePos) {
        super.onMouseScroll(scroll, mousePos);
        if (shooting) {
            shootSize += scroll;
            if (shootSize < .1) shootSize = .1;
        }
    }

    private void drawFieldLines(double lineDensity, ArrayList<PhysicsCircle> physicsCircles) {
        int inverseDensity = (int) (1/lineDensity);
        int windowWidth = (int)getWindow().getScaledSize().getX();
        int windowHeight = (int)getWindow().getScaledSize().getY();

        for (int x = 0; x < windowWidth / inverseDensity + 1; x++) {
            for (int y = 0; y < windowHeight / inverseDensity + 1; y++) {
                VectorMod gravityForce = Vector.NULL.getMod();
                for (PhysicsCircle otherObject : physicsCircles) {
                    if (!otherObject.isPhysicsDisabled()) {
                        Vector gravityFromOtherObject = GravityUtil.calculateGravitationalField(1,otherObject,new Vector(x,y).getMultiplied(inverseDensity));
                        if (!(String.valueOf(gravityFromOtherObject.getMagnitude())).equals("NaN")) gravityForce.add(gravityFromOtherObject);
                    }
                }
                LineUI lineUI = new LineUI(new Vector(x,y).getMultiplied(inverseDensity),gravityForce.getTheta(),Math.min(Math.max(gravityForce.getMagnitude(),.1),1)*10,ColorE.WHITE.alpha(100), LineUI.Type.PLAIN,.5);
                lineUI.draw();
            }
        }
    }

    private void drawShootingObject() {
        RegularPolygonUI polygonUI = new RegularPolygonUI(shootPos, com.ejo.glowui.util.Util.GLOW_BLUE,true,shootSize,shootVertices,new Angle(shootSpin,true));
        GL11.glLineWidth(3);
        polygonUI.draw();
        LineUI line = new LineUI(ColorE.WHITE, LineUI.Type.DOTTED,2,shootPos,shootPos.getAdded(shootPos.getAdded(getWindow().getScaledMousePos().getMultiplied(-1))));
        line.draw();
        shootSpin += 1;
        if (shootSpin > 360) shootSpin = 0;
    }

    private void updateShootObject() {
        if (shooting) {
            shooter.reset();
            shooterInitializer.run(() -> {
                Random random = new Random();
                shootVertices = random.nextInt(vertexLow, vertexHigh);
                shouldRenderShooting = true;
            });
        } else {
            shooterInitializer.reset();
            shooter.run(() -> {
                //Shoot the object
                shouldRenderShooting = false;
                Random random = new Random();
                shootVelocity = shootPos.getAdded(getWindow().getScaledMousePos().getMultiplied(-1)).getMultiplied(.75);
                ColorE randomColor = new ColorE(random.nextInt(25,255),random.nextInt(25,255),random.nextInt(25,255),255);
                PhysicsCircle poly = new PhysicsCircle(new CircleUI(shootPos, randomColor, shootSize, CircleUI.Type.MEDIUM),(double) 4 /3*Math.PI*Math.pow(shootSize,3),shootVelocity,Vector.NULL);
                queueAddElements(poly);
            });
        }
    }


    private void initObjectPositions() {
        DoOnce.DEFAULT1.run(this::setRandomObjectPositions);
    }

    private void setRandomObjectPositions() {
        Random random = new Random();
        for (PhysicsCircle obj : getPhysicsObjects())
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
            addElements(new PhysicsCircle(
                    new CircleUI(Vector.NULL, randomColor, trueSize, CircleUI.Type.MEDIUM), (double) 4 /3*Math.PI*Math.pow(trueSize,3),
                    new Vector(random.nextDouble(-startVelRange,startVelRange),random.nextDouble(-startVelRange,startVelRange)), Vector.NULL));
        }
    }

    private void addStars() {
        for (int i = 0; i < 100; i++) {
            ColorE color = new ColorE(255, 255, 255,255);
            RectangleUI obj = new RectangleUI(Vector.NULL,new Vector(1,1), color);
            obj.setTicking(false);
            if (!this.drawFieldLines) addElements(obj);
        }
    }


    public ArrayList<PhysicsCircle> getPhysicsObjects() {
        ArrayList<PhysicsCircle> rectangles = new ArrayList<>();
        for (ElementUI elementUI : getElements()) {
            if (elementUI instanceof PhysicsCircle polygon) rectangles.add(polygon);
        }
        return rectangles;
    }

}