package com.ejo.orbit;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowui.Window;

public class Main {

    public static Window window = new Window(
            "Orbit",
            new Vector(100,100),
            new Vector(800,600),
            new SceneOrbit(),
            true,4,60,60
    );

    public static void main(String[] args) {
        window.run();
        window.close();
    }
}