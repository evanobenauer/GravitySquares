package com.ejo.gravitysquares;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowui.Window;
import com.ejo.gravitysquares.scenes.TitleScene;

public class Main {

    public static Window window = new Window(
            "Orbit",
            new Vector(100,100),
            new Vector(1400,800),
            new TitleScene(),
            true,4,60,60
    );

    public static void main(String[] args) {
        window.run();
        window.close();
    }
}