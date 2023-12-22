package com.ejo.gravityshapes;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.math.VectorMod;
import com.ejo.gravityshapes.objects.PhysicsPolygon;
import com.ejo.uiphysics.elements.PhysicsObjectUI;
import com.ejo.uiphysics.util.VectorUtil;

import java.util.ArrayList;

public class Util {

    public static boolean areObjectsInCollisionRange(PhysicsObjectUI forceObject, PhysicsObjectUI otherObject) {
        double objectDistance = forceObject.getCenter().getAdded(otherObject.getCenter().getMultiplied(-1)).getMagnitude();

        if (forceObject instanceof PhysicsPolygon forcedGon && otherObject instanceof PhysicsPolygon otherGon)
            return objectDistance <= forcedGon.getPolygon().getRadius() + otherGon.getPolygon().getRadius();

        return forceObject.isColliding(otherObject);
    }

    public static <T extends PhysicsObjectUI> Vector calculateGravityForce(double G, PhysicsObjectUI object, ArrayList<T> physicsObjects, double minDistance) {
        if (object.isPhysicsDisabled()) return Vector.NULL;

        VectorMod gravityForce = Vector.NULL.getMod();

        //Calculate the force on obj from every other object in the list
        for (PhysicsObjectUI otherObject : physicsObjects) {
            if (!object.equals(otherObject) && !otherObject.isPhysicsDisabled()) {
                Vector gravityForceFromOtherObject = calculateGravitationalField(G,otherObject,object.getCenter(),minDistance).getMultiplied(object.getMass());
                if (!(String.valueOf(gravityForceFromOtherObject.getMagnitude())).equals("NaN"))
                    gravityForce.add(gravityForceFromOtherObject);
            }
        }
        return gravityForce;
    }


    //TODO: Add distance capping to the PhysicsUI library
    public static Vector calculateGravitationalField(double G, PhysicsObjectUI object, Vector location, double minDistance) {
        Vector distance = VectorUtil.calculateVectorBetweenPoints(object.getCenter(),location);
        double distanceCapped = Math.max(distance.getMagnitude(),minDistance);
        return distance.getUnitVector().getMultiplied(G * object.getMass() / Math.pow(distanceCapped, 2));
    }

}