package com.ejo.gravitysquares;

import com.ejo.glowlib.math.VectorMod;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsObjectUI;
import com.ejo.glowlib.math.Vector;
import com.ejo.gravitysquares.objects.PhysicsRectangle;

import java.util.ArrayList;

public class PhysicsUtil {

    public static double g = 9.8;
    public static double G = 6.67 * Math.pow(10, -11);

    @Deprecated
    public static Vector calculateGravityForceAndCollide(Scene scene, PhysicsRectangle object, ArrayList<PhysicsRectangle> physicsObjects, double G, boolean doWallBounce, boolean doCollisions) {
        if (object.isDisabled()) return Vector.NULL;
        if (doWallBounce) object.doBounce(scene);

        VectorMod gravityForce = Vector.NULL.getMod();

        //Calculate the force on obj from every other object in the list
        for (PhysicsRectangle otherObject : physicsObjects) {
            if (!object.equals(otherObject) && !otherObject.isDisabled()) {

                //Do Object Collisions
                if (doCollisions && areObjectsColliding(object,otherObject)) {
                    object.doCollision(otherObject);
                    continue;
                }

                Vector gravityForceFromOtherObject = calculateGravitationalField(G,otherObject,object.getCenter()).getMultiplied(object.getMass());

                if (!(String.valueOf(gravityForceFromOtherObject.getMagnitude())).equals("NaN")) gravityForce.add(gravityForceFromOtherObject);
            }
        }
        return gravityForce;
    }

    public static Vector calculateGravityForce(PhysicsRectangle object, ArrayList<PhysicsRectangle> physicsObjects, double G) {
        if (object.isDisabled()) return Vector.NULL;

        VectorMod gravityForce = Vector.NULL.getMod();

        //Calculate the force on obj from every other object in the list
        for (PhysicsRectangle otherObject : physicsObjects) {
            if (!object.equals(otherObject) && !otherObject.isDisabled()) {
                Vector gravityForceFromOtherObject = calculateGravitationalField(G,otherObject,object.getCenter()).getMultiplied(object.getMass());
                if (!(String.valueOf(gravityForceFromOtherObject.getMagnitude())).equals("NaN")) gravityForce.add(gravityForceFromOtherObject);
            }
        }
        return gravityForce;
    }

        public static Vector calculateGravitationalField(double G, PhysicsObjectUI object, Vector location) {
        Vector distance = PhysicsUtil.calculateVectorBetweenPoints(object.getCenter(),location);
        return distance.getUnitVector().getMultiplied(G * object.getMass() / Math.pow(distance.getMagnitude(), 2));
    }


    public static boolean areObjectsColliding(PhysicsObjectUI forceObject, PhysicsObjectUI otherObject) {
        double objectDistance = forceObject.getCenter().getAdded(otherObject.getCenter().getMultiplied(-1)).getMagnitude();
        return objectDistance <= ((PhysicsRectangle) forceObject).getRectangle().getSize().getX()/2 + ((PhysicsRectangle) otherObject).getRectangle().getSize().getX()/2;
    }

    public static void calculateSurfaceGravity(ArrayList<PhysicsObjectUI> physicsObjects) {
        for (PhysicsObjectUI forcedObject : physicsObjects) {
            if (!forcedObject.isDisabled()) {
                forcedObject.setNetForce(forcedObject.getNetForce().getAdded(new Vector(0,g*forcedObject.getMass())));
            }
        }
    }

    public static Vector calculateVectorBetweenObjects(PhysicsObjectUI object1, PhysicsObjectUI object2) {
        return object1.getCenter().getAdded(object2.getCenter().getMultiplied(-1));
    }

    public static Vector calculateVectorBetweenPoints(Vector point1, Vector point2) {
        return point1.getAdded(point2.getMultiplied(-1));
    }

}