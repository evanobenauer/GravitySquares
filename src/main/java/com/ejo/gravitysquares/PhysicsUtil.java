package com.ejo.gravitysquares;

import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsObjectUI;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.math.VectorMod;
import com.ejo.gravitysquares.objects.PhysicsRectangle;

import java.util.ArrayList;

public class PhysicsUtil {

    public static double g = 9.8;
    public static double G = 6.67 * Math.pow(10, -11);

    public static void calculateGravityForcesAndCollisions(Scene scene, ArrayList<PhysicsRectangle> objects, double G, boolean doWallBounce) {
        for (PhysicsRectangle forceObject : objects) {
            if (!forceObject.isDisabled()) {
                if (doWallBounce) forceObject.doBounce(scene);

                VectorMod gravityForce = new VectorMod(Vector.NULL);

                //Calculate the force on obj from every other object in the list
                for (PhysicsObjectUI otherObject : objects) {
                    if (!forceObject.equals(otherObject) && !otherObject.isDisabled()) {
                        Vector objectDistance = calculateVectorBetweenObjects(otherObject, forceObject);
                        Vector objForce = objectDistance
                                .getUnitVector()
                                .getMultiplied(G * forceObject.getMass() * otherObject.getMass() / Math.pow(objectDistance.getMagnitude(), 2));

                        //TODO: Add collisions here??

                        if (!(String.valueOf(objForce.getMagnitude())).equals("NaN")) {
                            gravityForce.add(objForce);
                        }
                    }
                }

                forceObject.setNetForce(forceObject.getNetForce().getAdded(gravityForce));
            }
        }
    }


    public static void calculateGravityForces(ArrayList<PhysicsObjectUI> objects) {
        for (PhysicsObjectUI forcedObject : objects) {
            if (!forcedObject.isDisabled()) {
                VectorMod gravityForce = new VectorMod(Vector.NULL);

                //Calculate the force on the object from every other object in the list
                for (PhysicsObjectUI otherObject : objects) {
                    if (!forcedObject.equals(otherObject) && !otherObject.isDisabled()) {
                        Vector objectDistance = calculateVectorBetweenObjects(otherObject, forcedObject);
                        Vector objForce = objectDistance
                                .getUnitVector()
                                .getMultiplied(G * forcedObject.getMass() * otherObject.getMass() / Math.pow(objectDistance.getMagnitude(), 2));

                        if (!(String.valueOf(objForce.getMagnitude())).equals("NaN"))
                            gravityForce.add(objForce);
                    }
                }

                forcedObject.setNetForce(forcedObject.getNetForce().getAdded(gravityForce));
            }
        }
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

}