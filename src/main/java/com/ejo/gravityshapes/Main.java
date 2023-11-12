package com.ejo.gravityshapes;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowui.Window;
import com.ejo.gravityshapes.scenes.TitleScene;

public class Main {

    public static Window window = new Window(
            "Gravity Shapes",
            new Vector(100,100),
            new Vector(1400,900),
            new TitleScene(),
            true,8,60,60
    );

    public static void main(String[] args) {
        window.run();
        window.close();
    }
}