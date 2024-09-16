package appeng.client.gui.widgets;

import java.util.HashMap;

import appeng.api.storage.data.IAEStack;
import appeng.client.gui.implementations.GuiProductionStats;
import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;
import appeng.core.stats.ProductionStatsManager;
import appeng.util.ringbuffer.RecursiveRingBufferManager;

public class GuiProductionStatsGraph {

    private final GuiProductionStats parent;
    private final PanelSide side;
    private final GuiGraph graph;

    private static final int GRAPH_TEXTURE_WIDTH = 238;
    private static final int GRAPH_TEXTURE_HEIGHT = 111;
    private static final int GRAPH_TEXTURE_BORDER = 8;
    private static final int GRAPH_HEIGHT = 95;
    private static final int GRAPH_WIDTH = 222;
    private float bla = 0;

    public GuiProductionStatsGraph(GuiProductionStats parent, PanelSide side) {
        this.parent = parent;
        this.side = side;
        this.graph = new GuiGraph(
                this.parent,
                GRAPH_TEXTURE_BORDER,
                GuiProductionStatsIntervals.getHeight() + GRAPH_TEXTURE_BORDER,
                GRAPH_WIDTH,
                GRAPH_HEIGHT,
                22,
                8);

        HashMap<IAEStack, RecursiveRingBufferManager> bufferManagerMap = ProductionStatsManager.getInstance()
                .getProductionStatsDataBuffers();

        for (IAEStack key : bufferManagerMap.keySet()) {
            graph.addGraph(key, bufferManagerMap.get(key).GRAPH_COLOR);
        }
    }

    public void initGui() {
        this.graph.toggleGridDrawing(true);
        this.graph.toggleLabelDrawing(true);
        this.graph.recalculateXAxisLabels(120f);
    }

    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        int offsetXAdjusted = side.equals(PanelSide.PRODUCTION) ? offsetX : offsetX + GRAPH_TEXTURE_WIDTH;
        switch (side) {
            case PRODUCTION -> {
                HashMap<IAEStack, RecursiveRingBufferManager> bufferManagerMap = ProductionStatsManager.getInstance()
                        .getProductionStatsDataBuffers();
                for (IAEStack key : bufferManagerMap.keySet()) {
                    bla = mouseX;
                    if (bla < 0) {
                        bla = 0;
                    }
                    float val = bla;
                    if (val > graph.getMaxYScaleValue()) {
                        graph.recalculateYAxisLabels(val);
                    }
                    graph.addData(key, val);
                }
                graph.draw(offsetX - parent.getGuiLeft(), offsetY - parent.getGuiTop(), mouseX, mouseY);
            }
        }
    }

    public void drawBG(final int offsetX, final int offsetY) {
        int offsetXAdjusted = side.equals(PanelSide.PRODUCTION) ? offsetX : offsetX + GRAPH_TEXTURE_WIDTH;
        parent.bindTexture("guis/productionstatsgraph.png");
        parent.drawTexturedModalRect(offsetXAdjusted, offsetY, 0, 0, GRAPH_TEXTURE_WIDTH, GRAPH_TEXTURE_HEIGHT);
    }

    public static int getWidth() {
        return GRAPH_TEXTURE_WIDTH;
    }

    public static int getHeight() {
        return GRAPH_TEXTURE_HEIGHT;
    }
}