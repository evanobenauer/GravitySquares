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

    public void doCollision(PhysicsRectangle object) {
        double weight = getMass() / (object.getMass() + getMass());
        setCenter(getCenter().getMultiplied(weight).getAdded(object.getCenter().getMultiplied(1 - weight)));
        setVelocity(getVelocity().getMultiplied(weight).getAdded(object.getVelocity().getMultiplied(1 - weight)));

        getRectangle().setColor(new ColorE((int)(getColor().getRed() * weight + object.getColor().getRed() * (1-weight)),(int)(getColor().getGreen() * weight + object.getColor().getGreen() * (1-weight)),(int)(getColor().getBlue() * weight + object.getColor().getBlue() * (1-weight))));

        setMass(getMass() + object.getMass());
        double radius = Math.pow(getMass(), (double) 1 /3);
        getRectangle().setSize(new Vector(radius,radius));

        object.setDisabled(true);
        object.setEnabled(false);
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
