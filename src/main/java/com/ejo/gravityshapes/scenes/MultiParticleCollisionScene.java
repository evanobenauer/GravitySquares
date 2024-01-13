package com.ejo.gravityshapes.scenes;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.misc.DoOnce;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.ElementUI;
import com.ejo.glowui.scene.elements.shape.CircleUI;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowui.scene.elements.widget.ButtonUI;
import com.ejo.glowui.util.input.Key;
import com.ejo.glowui.util.input.Mouse;
import com.ejo.glowui.util.render.QuickDraw;
import com.ejo.gravityshapes.objects.PhysicsCircle;
import com.ejo.uiphysics.util.VectorUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class MultiParticleCollisionScene extends Scene {

    private final ButtonUI buttonX = new ButtonUI(Vector.NULL,new Vector(15,15),new ColorE(200,0,0,255), ButtonUI.MouseButton.LEFT,() -> getWindow().setScene(new TitleScene()));

    private final boolean doWallBounce;
    private final boolean doCollisions;

    private final int vertexLow = 29, vertexHigh = 30;

    private final double diameter;

    private ArrayList<Integer>[][] particleGrid;

    public MultiParticleCollisionScene(int objectCount, double sizeMin, double sizeMax, boolean doWallBounce, boolean drawFieldLines) {
        super("Orbit Scene");
        DoOnce.DEFAULT1.reset();

        this.doWallBounce = doWallBounce;
        this.doCollisions = false;

        this.diameter = sizeMax * 2;

        addPhysicsObjects(objectCount * 20,sizeMin,sizeMax);
        addElements(buttonX);
    }

    @Override
    public void draw() {
        //Set exit button to top right corner
        buttonX.setPos(new Vector(getSize().getX(),0).getAdded(-buttonX.getSize().getX(),0));

        initGrid();
        for (int Y = 0; Y < particleGrid.length; Y++) {
            for (int X = 0; X < particleGrid[Y].length; X++) {
                QuickDraw.drawRect(new Vector(X,Y).getMultiplied(diameter),new Vector(1,1),ColorE.WHITE);
            }
        }

        //Draw all screen objects
        super.draw();

        //Draw X for Exit Button
        QuickDraw.drawText("X",new Font("Arial",Font.PLAIN,14), buttonX.getPos().getAdded(3,-2),ColorE.WHITE);
    }

    @Override
    public void tick() {
        initGrid();
        initObjectPositions();


        /*
        for (int objI = 0; objI < getPhysicsObjects().size(); objI++) {
            PhysicsCircle obj = getPhysicsObjects().get(objI);
            for (int Y = 0; Y < particleGrid.length; Y++) {
                for (int X = 0; X < particleGrid[Y].length; X++) {
                    Vector gridPos = new Vector(X, Y).getMultiplied(diameter);
                    Vector gridSize = new Vector(diameter, diameter);
                    boolean inX = obj.getPos().getX() > gridPos.getX() && obj.getPos().getX() < gridPos.getX() + gridSize.getX();
                    boolean inY = obj.getPos().getY() > gridPos.getY() && obj.getPos().getY() < gridPos.getY() + gridSize.getY();
                    //if (inX && inY) particleGrid[Y][X].add(objI);
                }
            }
        }
        
         */


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
    }

    private void initObjectPositions() {
        DoOnce.DEFAULT1.run(this::setRandomObjectPositions);
    }

    private void initGrid() {
        DoOnce.DEFAULT2.run(() -> {
            //particleGrid = new int[(int)(getSize().getY() / diameter)][(int)(getSize().getX() / diameter)];
            particleGrid = new ArrayList[(int)(getSize().getY() / diameter)][(int)(getSize().getX() / diameter)];
        });
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

    public ArrayList<PhysicsCircle> getPhysicsObjects() {
        ArrayList<PhysicsCircle> rectangles = new ArrayList<>();
        for (ElementUI elementUI : getElements()) {
            if (elementUI instanceof PhysicsCircle polygon) rectangles.add(polygon);
        }
        return rectangles;
    }

}