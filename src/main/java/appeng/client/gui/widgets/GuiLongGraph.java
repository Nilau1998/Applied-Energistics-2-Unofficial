package appeng.client.gui.widgets;

import appeng.client.gui.AEBaseGui;

import java.util.ArrayList;

public class GuiLongGraph extends GuiGraph<ArrayList<Float>> {

    private long currentScale = 100;
    protected GuiLongGraph(AEBaseGui graphHost, int w, int h) {
        super(graphHost, w, h, new ArrayList<Float>());
    }

    public void addData(float data) {
        if (graphData.size() == width - 1) {
            graphData.remove(0);
        }
        graphData.add(data);
    }

    @Override
    protected int getRelativeHeight(int index, int height) {
        if (graphData.isEmpty()) {
            return 0;
        }
        float data = graphData.get(index);
        double val = data * height / (double) currentScale;
        if (val < height && val > 0) {
            return (int) val;
        } else if (val < 0) {
            return 0;
        }
        return height;
    }
}
