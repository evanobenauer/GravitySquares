package com.ejo.orbit;

import com.ejo.glowui.scene.elements.shape.physics.PhysicsDraggableUI;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsObjectUI;
import com.ejo.glowui.scene.elements.shape.IShape;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowlib.math.Vector;

public class PhysicsObjTest extends PhysicsDraggableUI {

    public PhysicsObjTest(IShape shape, double mass, Vector velocity, Vector acceleration) {
        super(shape, mass, velocity, acceleration);
        setDeltaT(.1f);
    }

    public void haltAtEdge() {
        if (shape instanceof RectangleUI rect) {
            if (getPos().getX() + rect.getSize().getX() > getScene().getWindow().getSize().getX()) {
                setVelocity(new Vector(0.0, getVelocity().getY()));
                setPos(new Vector(getScene().getWindow().getSize().getX() - rect.getSize().getX(), getPos().getY()));
            }
            if (getPos().getX() < 0.0) {
                setVelocity(new Vector(0.0, getVelocity().getY()));
                setPos(new Vector(0.0, getPos().getY()));
            }
            if (getPos().getY() + rect.getSize().getY() > getScene().getWindow().getSize().getY()) {
                setVelocity(new Vector(getVelocity().getX(), 0.0));
                setPos(new Vector(getPos().getX(), getScene().getWindow().getSize().getY() - rect.getSize().getY()));
            }
            if (getPos().getY() < 0.0) {
                setVelocity(new Vector(getVelocity().getX(), 0.0));
                setPos(new Vector(getPos().getX(), 0.0));
            }
        }
    }
    public void swapVelocity() {
        PhysicsObjectUI object = this;
        double mul = .1f;
        if (shape instanceof RectangleUI rect) {
            if (getPos().getX() + rect.getSize().getX() > getScene().getWindow().getSize().getX()) {
                object.setVelocity(new Vector(-mul*object.getVelocity().getX(), object.getVelocity().getY()));
                setPos(new Vector(getScene().getWindow().getSize().getX() - rect.getSize().getX(), getPos().getY()));
            }
            if (getPos().getX() < 0.0) {
                object.setVelocity(new Vector(-mul*object.getVelocity().getX(), object.getVelocity().getY()));
                setPos(new Vector(0.0, getPos().getY()));
            }
            if (getPos().getY() + rect.getSize().getY() > getScene().getWindow().getSize().getY()) {
                object.setVelocity(new Vector(object.getVelocity().getX(), -mul*object.getVelocity().getY()));
                setPos(new Vector(getPos().getX(), getScene().getWindow().getSize().getY() - rect.getSize().getY()));
            }
            if (getPos().getY() < 0.0) {
                object.setVelocity(new Vector(object.getVelocity().getX(), -mul*object.getVelocity().getY()));
                setPos(new Vector(getPos().getX(), 0.0));
            }
        }
    }
}
