package com.ejo.gravitysquares;

import com.ejo.glowlib.math.MathE;
import com.ejo.glowlib.math.VectorMod;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowui.scene.elements.shape.physics.PhysicsObjectUI;
import com.ejo.glowlib.math.Vector;
import com.ejo.gravitysquares.objects.PhysicsRectangle;

import java.util.ArrayList;
import java.util.Random;

public class PhysicsUtil {

    public static double g = 9.8;
    public static double G = 6.67 * Math.pow(10, -11);

    public static void calculateGravityForces(ArrayList<PhysicsObjectUI> objects) {
        for (PhysicsObjectUI forcedObject : objects) {
            if (forcedObject.isDisabled()) continue;
            VectorMod gravityForce = Vector.NULL.getMod();

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