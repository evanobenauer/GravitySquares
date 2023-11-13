package com.ejo.gravityshapes;

import com.ejo.gravityshapes.objects.PhysicsPolygon;
import com.ejo.uiphysics.elements.PhysicsObjectUI;

public class Util {

    public static boolean areObjectsInCollisionRange(PhysicsObjectUI forceObject, PhysicsObjectUI otherObject) {
        double objectDistance = forceObject.getCenter().getAdded(otherObject.getCenter().getMultiplied(-1)).getMagnitude();

        if (forceObject instanceof PhysicsPolygon forcedGon && otherObject instanceof PhysicsPolygon otherGon)
            return objectDistance <= forcedGon.getPolygon().getRadius() + otherGon.getPolygon().getRadius();

        return forceObject.isColliding(otherObject);
    }

}