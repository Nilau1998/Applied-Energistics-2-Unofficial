package appeng.client.gui.widgets;

import java.util.ArrayList;

import appeng.client.gui.implementations.GuiProductionStats;
import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;

public class GuiProductionStatsGraph {

    private final GuiProductionStats parent;
    private final PanelSide side;

    public static final int GRAPH_TEXTURE_WIDTH = 238;
    public static final int GRAPH_TEXTURE_HEIGHT = 111;
    private static final int GRAPH_X_ORIGIN = 8;
    private static final int GRAPH_Y_ORIGIN = 81;
    private static final int GRAPH_HEIGHT = 74;
    private static final int GRAPH_WIDTH = 222;
    private int bla = 0;

    private final ArrayList<GuiLongGraph> graphs = new ArrayList<>();

    public GuiProductionStatsGraph(GuiProductionStats parent, PanelSide side) {
        this.parent = parent;
        this.side = side;
        for (int i = 0; i < 3000; i++) {
            graphs.add(new GuiLongGraph(parent, GRAPH_WIDTH, GRAPH_HEIGHT));
        }
    }

    public void initGui() {}

    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        for (int i = 0; i < graphs.size(); i++) {
            double sinVal = (i + 1) / 25.0;
            // graphs.get(i).addData((float) (GRAPH_HEIGHT * Math.sin(sinVal * bla)));
            graphs.get(i).addData(GRAPH_HEIGHT / (1 + i));
            graphs.get(i).drawFG(
                    GRAPH_X_ORIGIN - (GRAPH_TEXTURE_WIDTH / 2) + 1,
                    GRAPH_Y_ORIGIN - (GRAPH_TEXTURE_HEIGHT),
                    mouseX,
                    mouseY);
        }
        bla += 1;
    }

    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        int offsetXAdjusted = this.side.equals(PanelSide.LEFT) ? offsetX - (GRAPH_TEXTURE_WIDTH / 2) + 1
                : offsetX + (GRAPH_TEXTURE_WIDTH / 2) - 1;
        int offsetYAdjusted = offsetY - (GRAPH_TEXTURE_HEIGHT / 3);
        parent.bindTexture("guis/productionstatsgraph.png");
        parent.drawTexturedModalRect(offsetXAdjusted, offsetYAdjusted, 0, 0, GRAPH_TEXTURE_WIDTH, GRAPH_TEXTURE_HEIGHT);
    }

    public int getWidth() {
        return GRAPH_TEXTURE_WIDTH;
    }

    public int getHeight() {
        return GRAPH_TEXTURE_HEIGHT;
    }
}
