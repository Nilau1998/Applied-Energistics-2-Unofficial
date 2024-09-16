package appeng.client.gui.widgets;

import java.util.ArrayList;

public class GuiFloatGraph extends GuiGraph<ArrayList<Float>> {

    private float currentMax = Float.NEGATIVE_INFINITY;

    protected GuiFloatGraph(int w, int h, int ox, int oy, int color) {
        super(w, h, ox, oy, color, new ArrayList<Float>());
    }

    public void addData(float data) {
        if (graphData.size() == width) {
            graphData.remove(0);
        }
        graphData.add(data);
        for (Float i : graphData) {
            if (i > currentMax) {
                currentMax = i;
            }
        }
    }

    @Override
    protected float getRelativeHeight(int index, int height) {
        if (graphData.isEmpty() || graphData.get(index) < 0.1f) {
            return 0;
        }
        return scale(Math.min(currentMax, graphData.get(index)));
    }

    // https://stats.stackexchange.com/a/281165
    private float scale(float x) {
        return (this.height * x) / currentMax;
    }
}