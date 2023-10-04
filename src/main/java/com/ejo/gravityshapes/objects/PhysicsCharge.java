package com.ejo.gravityshapes.objects;

import com.ejo.glowlib.math.Angle;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.shape.RegularPolygonUI;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsDraggableUI;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsObjectUI;
import com.ejo.gravityshapes.PhysicsUtil;

public class PhysicsCharge extends PhysicsDraggableUI {

    private final RegularPolygonUI rect;

    private double charge;

    public PhysicsCharge(RegularPolygonUI shape, double mass, double charge, Vector velocity, Vector acceleration) {
        super(shape, mass, velocity, acceleration);
        this.rect = shape;
        this.charge = charge;
        setDeltaT(.1f);
    }

    @Override
    public void tickElement(Scene scene, Vector mousePos) {
        super.tickElement(scene,mousePos);
    }

    public void doCollision(PhysicsCharge object) {
        double weight = getMass() / (object.getMass() + getMass());

        //Set states
        setCenter(getCenter().getMultiplied(weight).getAdded(object.getCenter().getMultiplied(1 - weight)));
        setVelocity(getVelocity().getMultiplied(weight).getAdded(object.getVelocity().getMultiplied(1 - weight)));

        //Set mass and radius
        setMass(getMass() + object.getMass());
        double density = 1;
        double radius = Math.pow(getMass()/density * 3/(4*Math.PI), (double) 1/3); //Volume is calculated as a sphere
        getPolygon().setRadius(radius);

        //Set charge
        setCharge(getCharge() + object.getCharge());

        //Set color
        if (getCharge() > 0) getPolygon().setColor(ColorE.BLUE);
        if (getCharge() < 0) getPolygon().setColor(ColorE.RED);
        if (getCharge() == 0) getPolygon().setColor(ColorE.GRAY);

        //Delete old object
        object.setDisabled(true);
        object.setEnabled(false);
    }

    public void doBounce(Scene scene) {
        PhysicsObjectUI object = this;
        double mul = .1f;
        if (getPos().getX() + getPolygon().getRadius() > scene.getSize().getX()) {
            object.setVelocity(new Vector(-mul * object.getVelocity().getX(), object.getVelocity().getY()));
            setPos(new Vector(scene.getSize().getX() - getPolygon().getRadius(), getPos().getY()));
        }
        if (getPos().getX() - getPolygon().getRadius() < 0.0) {
            object.setVelocity(new Vector(-mul * object.getVelocity().getX(), object.getVelocity().getY()));
            setPos(new Vector(getPolygon().getRadius(), getPos().getY()));
        }
        if (getPos().getY() + getPolygon().getRadius() > scene.getSize().getY()) {
            object.setVelocity(new Vector(object.getVelocity().getX(), -mul * object.getVelocity().getY()));
            setPos(new Vector(getPos().getX(), scene.getSize().getY() - getPolygon().getRadius()));
        }
        if (getPos().getY() - getPolygon().getRadius() < 0.0) {
            object.setVelocity(new Vector(object.getVelocity().getX(), -mul * object.getVelocity().getY()));
            setPos(new Vector(getPos().getX(), getPolygon().getRadius()));
        }
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public double getCharge() {
        return charge;
    }

    public RegularPolygonUI getPolygon() {
        return rect;
    }

}
