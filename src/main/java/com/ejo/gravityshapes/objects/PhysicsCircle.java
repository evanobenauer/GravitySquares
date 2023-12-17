package com.ejo.gravityshapes.objects;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.shape.CircleUI;
import com.ejo.uiphysics.elements.PhysicsDraggableUI;
import com.ejo.uiphysics.elements.PhysicsObjectUI;

public class PhysicsCircle extends PhysicsDraggableUI {

    private final CircleUI circle;

    public PhysicsCircle(CircleUI shape, double mass, Vector velocity, Vector netForce) {
        super(shape, mass, velocity, netForce);
        this.circle = shape;
        setDeltaT(.05f);
        setTickNetReset(true);
    }

    public void doWallBounce(Scene scene, double elasticity) {
        PhysicsObjectUI object = this;
        if (getPos().getX() + getCircle().getRadius() > scene.getSize().getX()) {
            object.setVelocity(new Vector(-elasticity * object.getVelocity().getX(), object.getVelocity().getY()));
            setPos(new Vector(scene.getSize().getX() - getCircle().getRadius(), getPos().getY()));
        }
        if (getPos().getX() - getCircle().getRadius() < 0.0) {
            object.setVelocity(new Vector(-elasticity * object.getVelocity().getX(), object.getVelocity().getY()));
            setPos(new Vector(getCircle().getRadius(), getPos().getY()));
        }
        if (getPos().getY() + getCircle().getRadius() > scene.getSize().getY()) {
            object.setVelocity(new Vector(object.getVelocity().getX(), -elasticity * object.getVelocity().getY()));
            setPos(new Vector(getPos().getX(), scene.getSize().getY() - getCircle().getRadius()));
        }
        if (getPos().getY() - getCircle().getRadius() < 0.0) {
            object.setVelocity(new Vector(object.getVelocity().getX(), -elasticity * object.getVelocity().getY()));
            setPos(new Vector(getPos().getX(), getCircle().getRadius()));
        }
    }

    @Override
    public double getMomentOfInertia() {
        return (double) 2 /5 * getMass() * Math.pow(getCircle().getRadius(),2);
    }

    @Override
    public void resetMovement() {
        setNetForce(Vector.NULL);
        setVelocity(Vector.NULL);
    }

    public CircleUI getCircle() {
        return circle;
    }

}
