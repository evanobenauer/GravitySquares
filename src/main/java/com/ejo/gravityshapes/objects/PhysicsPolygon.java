package com.ejo.gravityshapes.objects;

import com.ejo.glowlib.math.Angle;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.shape.RegularPolygonUI;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsDraggableUI;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsObjectUI;
import com.ejo.glowlib.math.Vector;
import com.ejo.gravityshapes.PhysicsUtil;

public class PhysicsPolygon extends PhysicsDraggableUI {

    private final RegularPolygonUI rect;

    public double spin = 0;

    public PhysicsPolygon(RegularPolygonUI shape, double mass, Vector velocity, Vector acceleration) {
        super(shape, mass, velocity, acceleration);
        this.rect = shape;
        setDeltaT(.1f);
    }

    @Override
    public void tickElement(Scene scene, Vector mousePos) {
        super.tickElement(scene,mousePos);
        getPolygon().setRotation(new Angle(getPolygon().getRotation().getRadians() + spin));
    }

    public void doCollision(PhysicsPolygon object) {
        double weight = getMass() / (object.getMass() + getMass());

        //Set states
        setCenter(getCenter().getMultiplied(weight).getAdded(object.getCenter().getMultiplied(1 - weight)));
        setVelocity(getVelocity().getMultiplied(weight).getAdded(object.getVelocity().getMultiplied(1 - weight)));

        //Set average color
        getPolygon().setColor(new ColorE((int)(getColor().getRed() * weight + object.getColor().getRed() * (1-weight)),(int)(getColor().getGreen() * weight + object.getColor().getGreen() * (1-weight)),(int)(getColor().getBlue() * weight + object.getColor().getBlue() * (1-weight))));

        //Set average polygon type
        getPolygon().setVertexCount((int)Math.ceil(getPolygon().getVertexCount() * weight + object.getPolygon().getVertexCount() * (1-weight)));

        //Set spin
        spinObjectFromCollision(object,.1);

        //Set mass and radius
        setMass(getMass() + object.getMass());
        double density = 1;
        double radius = Math.pow(getMass()/density * 3/(4*Math.PI), (double) 1/3); //Volume is calculated as a sphere
        getPolygon().setRadius(radius);

        //Delete old object
        object.setDisabled(true);
        object.setEnabled(false);
    }

    public void spinObjectFromCollision(PhysicsPolygon object, double maxAddableSpin) {
        //main object is reference frame. Other object is moving object

        //Calculate perpendicularity of velocity compared to the position
        Vector otherObjRefPos = PhysicsUtil.calculateVectorBetweenObjects(object,this);
        Vector otherObjRefVelocity = object.getVelocity().getAdded(getVelocity().getMultiplied(-1));
        double perpendicularity = otherObjRefPos.getUnitVector().getCross(otherObjRefVelocity.getUnitVector()).getMagnitude();

        //Calculate the sign of the velocity compared to the position
        int sign;
        Angle referencePosAngle = new Angle(-Math.atan2(otherObjRefPos.getY(),otherObjRefPos.getX())); //+180 to -180
        Angle referenceVelocityAngle = new Angle(-Math.atan2(otherObjRefVelocity.getY(),otherObjRefVelocity.getX())); //+180 to -180

        if (otherObjRefPos.getY() < 0) { //Top Half
            if (referenceVelocityAngle.getDegrees() + 180 > 240) referenceVelocityAngle = new Angle(referenceVelocityAngle.getRadians() - 2*Math.PI);
            sign = (referenceVelocityAngle.getDegrees() + 180 < referencePosAngle.getDegrees()) ? -1 : 1;
        } else { //Bottom Half
            if (referenceVelocityAngle.getDegrees() - 180 < -240) referenceVelocityAngle = new Angle(referenceVelocityAngle.getRadians() + 2*Math.PI);
            sign = (referenceVelocityAngle.getDegrees() - 180 < referencePosAngle.getDegrees()) ? -1 : 1;
        }

        double massWeight = getMass() / (object.getMass() + getMass());
        double velocityWeight = Math.min(otherObjRefVelocity.getMagnitude() / 100,1);

        double collisionSpin = sign * (maxAddableSpin * perpendicularity) * massWeight * velocityWeight;
        double weightedCombineSpin = (spin * massWeight) + (object.spin*(1-massWeight));

        spin = collisionSpin + weightedCombineSpin;
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

    public RegularPolygonUI getPolygon() {
        return rect;
    }

}
