package com.ejo.gravityshapes;

import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowlib.util.NumberUtil;
import com.ejo.glowui.scene.elements.shape.LineUI;
import com.ejo.uiphysics.elements.PhysicsObjectUI;

import java.util.ArrayList;

public class Grid {

    private ArrayList<PhysicsObjectUI>[][] grid; //A 2D array of arraylists
    private final int cellSize;
    private final Vector gridSize;
    private final Vector screenSize;

    //Currently, the grid encompasses the screen only
    // If an object is off-screen, it is added to the nearest grid pos
    // In the future, potentially resize the grid depending on the farthest object? that may make it slow though
    public <T extends PhysicsObjectUI> Grid(Vector screenSize, ArrayList<T> objects, int cellSize) {
        int gridCountX = (int) Math.round(screenSize.getX() / cellSize);
        int gridCountY = (int) Math.round(screenSize.getY() / cellSize);
        this.gridSize = new Vector(gridCountX,gridCountY);
        this.screenSize = screenSize;
        this.cellSize = cellSize;
        this.grid = new ArrayList[(int)gridSize.getY()][(int)gridSize.getX()];

        initializeCells();

        //Assign each object to its grid position
        for (PhysicsObjectUI obj : objects) {
            Vector gridIndex = new Vector(gridSize.getX() * (obj.getPos().getX() / screenSize.getX()), gridSize.getY() * (obj.getPos().getY() / screenSize.getY()));
            this.grid[NumberUtil.getBoundValue(gridIndex.getY(), 0, gridSize.getY() - 1).intValue()][NumberUtil.getBoundValue(gridIndex.getX(), 0, gridSize.getX() - 1).intValue()].add(obj);
        }

    }

    public ArrayList<PhysicsObjectUI> getSurroundingObjects(PhysicsObjectUI object) {

        //Finds the grid that the object corresponds to
        Vector gridIndex = new Vector(gridSize.getX() * (object.getPos().getX() / screenSize.getX()), gridSize.getY() * (object.getPos().getY() / screenSize.getY()));

        Vector[] directions = { //Surrounding directions
                Vector.I, //x+
                Vector.J.getAdded(Vector.I),//++
                Vector.J,//y+
                Vector.J.getAdded(Vector.I.getMultiplied(-1)),//y+x-
                Vector.I.getMultiplied(-1),//x-
                Vector.J.getAdded(Vector.I).getMultiplied(-1),//--
                Vector.J.getMultiplied(-1),//y-
                Vector.J.getAdded(Vector.I.getMultiplied(-1)).getMultiplied(-1),//x+y-
        };

        //Add all objects from surrounding cells and center cell
        ArrayList<PhysicsObjectUI> surroundingObjects = new ArrayList<>();
        surroundingObjects.addAll(grid[NumberUtil.getBoundValue(gridIndex.getY(), 0, gridSize.getY() - 1).intValue()][NumberUtil.getBoundValue(gridIndex.getX(), 0, gridSize.getX() - 1).intValue()]);

        for (Vector dir : directions) {
            int xi = ((int) gridIndex.getX()) + (int) dir.getX();
            int yi = ((int) gridIndex.getY()) + (int) dir.getY();
            if (xi < 0 || yi < 0 || xi > gridSize.getX() - 1 || yi > gridSize.getY() - 1) continue;
            surroundingObjects.addAll(grid[yi][xi]);
        }
        return surroundingObjects;
    }

    public void drawDebugGridLines() {
        //Draw Debug Grid Lines
        int countX = (int) Math.round(screenSize.getX() / cellSize);
        for (int i = 0; i <= countX; i++) {
            double x = screenSize.getX() / countX * (i);
            LineUI line = new LineUI(new Vector(x, 0), new Vector(x, screenSize.getY()), ColorE.WHITE.alpha(50), LineUI.Type.DOTTED, 1);
            line.draw();
        }

        int countY = (int) Math.round(screenSize.getY() / cellSize);
        for (int i = 0; i <= countY; i++) {
            double y = screenSize.getY() / countY * (i);
            LineUI line = new LineUI(new Vector(0, y), new Vector(screenSize.getX(), y), ColorE.WHITE.alpha(50), LineUI.Type.DOTTED, 1);
            line.draw();
        }
    }

    private void initializeCells() {
        for (int i = 0; i < gridSize.getX(); i++) {
            for (int j = 0; j < gridSize.getY(); j++) {
                grid[j][i] = new ArrayList<>();
            }
        }
    }

    public void clearCells() {
        this.grid = new ArrayList[(int)gridSize.getY()][(int)gridSize.getX()];
        for (int i = 0; i < gridSize.getX(); i++) {
            for (int j = 0; j < gridSize.getY(); j++) {
                grid[j][i].clear();
            }
        }
    }

    public ArrayList<PhysicsObjectUI>[][] getGridArray() {
        return grid;
    }

}
