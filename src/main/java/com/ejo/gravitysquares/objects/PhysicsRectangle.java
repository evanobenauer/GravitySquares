package com.ejo.gravitysquares.objects;

import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsDraggableUI;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsObjectUI;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowlib.math.Vector;

public class PhysicsRectangle extends PhysicsDraggableUI {

    private final RectangleUI rect;

    public PhysicsRectangle(RectangleUI shape, double mass, Vector velocity, Vector acceleration) {
        super(shape, mass, velocity, acceleration);
        this.rect = shape;
        setDeltaT(.1f);
    }

    public void doBounce(Scene scene) {
        PhysicsObjectUI object = this;
        double mul = .1f;
            if (getPos().getX() + getRectangle().getSize().getX() > scene.getSize().getX()) {
                object.setVelocity(new Vector(-mul*object.getVelocity().getX(), object.getVelocity().getY()));
                setPos(new Vector(scene.getSize().getX() - getRectangle().getSize().getX(), getPos().getY()));
            }
            if (getPos().getX() < 0.0) {
                object.setVelocity(new Vector(-mul*object.getVelocity().getX(), object.getVelocity().getY()));
                setPos(new Vector(0.0, getPos().getY()));
            }
            if (getPos().getY() + getRectangle().getSize().getY() > scene.getSize().getY()) {
                object.setVelocity(new Vector(object.getVelocity().getX(), -mul*object.getVelocity().getY()));
                setPos(new Vector(getPos().getX(), scene.getSize().getY() - getRectangle().getSize().getY()));
            }
            if (getPos().getY() < 0.0) {
                object.setVelocity(new Vector(object.getVelocity().getX(), -mul*object.getVelocity().getY()));
                setPos(new Vector(getPos().getX(), 0.0));
            }
        }

    public RectangleUI getRectangle() {
        return rect;
    }

}
