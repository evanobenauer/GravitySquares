package com.ejo.gravitysquares.objects;

import com.ejo.glowlib.math.VectorMod;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsDraggableUI;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsObjectUI;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowlib.math.Vector;
import com.ejo.gravitysquares.PhysicsUtil;

import java.util.ArrayList;

public class PhysicsRectangle extends PhysicsDraggableUI {

    private final RectangleUI rect;

    public PhysicsRectangle(RectangleUI shape, double mass, Vector velocity, Vector acceleration) {
        super(shape, mass, velocity, acceleration);
        this.rect = shape;
        setDeltaT(.1f);
    }

    public void calculateGravityForceAndCollide(Scene scene, ArrayList<PhysicsRectangle> physicsObjects, double G, boolean doWallBounce, boolean doCollisions) {
            if (isDisabled()) return;
            if (doWallBounce) doBounce(scene);

            VectorMod gravityForce = Vector.NULL.getMod();

            //Calculate the force on obj from every other object in the list
            for (PhysicsRectangle otherObject : physicsObjects) {
                if (!this.equals(otherObject) && !otherObject.isDisabled()) {

                    //Do Object Collisions
                    if (doCollisions && areObjectsColliding(this,otherObject)) {
                        doCollision(this,otherObject);
                        continue;
                    }

                    Vector objectDistance = PhysicsUtil.calculateVectorBetweenObjects(otherObject, this);
                    Vector gravityFromOtherObject = objectDistance.getUnitVector()
                            .getMultiplied(G * getMass() * otherObject.getMass() / Math.pow(objectDistance.getMagnitude(), 2));

                    if (!(String.valueOf(gravityFromOtherObject.getMagnitude())).equals("NaN")) gravityForce.add(gravityFromOtherObject);
                }
            }
            setNetForce(gravityForce);
    }

    private boolean areObjectsColliding(PhysicsObjectUI forceObject, PhysicsObjectUI otherObject) {
        double objectDistance = forceObject.getCenter().getAdded(otherObject.getCenter().getMultiplied(-1)).getMagnitude();
        return objectDistance <= ((PhysicsRectangle) forceObject).getRectangle().getSize().getX()/2 + ((PhysicsRectangle) otherObject).getRectangle().getSize().getX()/2;
    }

    private void doCollision(PhysicsRectangle object1, PhysicsRectangle object2) {
        object1.setMass(object1.getMass() + object2.getMass());

        double weight = object1.getMass() / (object2.getMass() + object1.getMass());
        object1.setCenter(object1.getCenter().getMultiplied(weight).getAdded(object2.getCenter().getMultiplied(1 - weight)));
        object1.setVelocity(object1.getVelocity().getMultiplied(weight).getAdded(object2.getVelocity().getMultiplied(1 - weight)));

        double radius = Math.pow(object1.getMass(), (double) 1 /3);
        object1.getRectangle().setSize(new Vector(radius,radius));

        object1.getRectangle().setColor(new ColorE((int)(object1.getColor().getRed() * weight + object2.getColor().getRed() * (1-weight)),(int)(object1.getColor().getGreen() * weight + object2.getColor().getGreen() * (1-weight)),(int)(object1.getColor().getBlue() * weight + object2.getColor().getBlue() * (1-weight))));

        object2.setDisabled(true);
        object2.setEnabled(false);
    }

    public void doBounce(Scene scene) {
        PhysicsObjectUI object = this;
        double mul = .1f;
        if (getPos().getX() + getRectangle().getSize().getX() > scene.getSize().getX()) {
            object.setVelocity(new Vector(-mul * object.getVelocity().getX(), object.getVelocity().getY()));
            setPos(new Vector(scene.getSize().getX() - getRectangle().getSize().getX(), getPos().getY()));
        }
        if (getPos().getX() < 0.0) {
            object.setVelocity(new Vector(-mul * object.getVelocity().getX(), object.getVelocity().getY()));
            setPos(new Vector(0.0, getPos().getY()));
        }
        if (getPos().getY() + getRectangle().getSize().getY() > scene.getSize().getY()) {
            object.setVelocity(new Vector(object.getVelocity().getX(), -mul * object.getVelocity().getY()));
            setPos(new Vector(getPos().getX(), scene.getSize().getY() - getRectangle().getSize().getY()));
        }
        if (getPos().getY() < 0.0) {
            object.setVelocity(new Vector(object.getVelocity().getX(), -mul * object.getVelocity().getY()));
            setPos(new Vector(getPos().getX(), 0.0));
        }
    }

    public RectangleUI getRectangle() {
        return rect;
    }

}
