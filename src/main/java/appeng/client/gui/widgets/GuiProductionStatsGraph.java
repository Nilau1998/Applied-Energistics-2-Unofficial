package appeng.client.gui.widgets;

import java.util.HashMap;

import appeng.core.stats.ProductionStatsDataManager;
import appeng.core.stats.productionstats.DataBufferHandler;
import org.lwjgl.input.Keyboard;

import appeng.api.storage.data.IAEStack;
import appeng.client.gui.implementations.GuiProductionStats;
import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;

public class GuiProductionStatsGraph {

    private final GuiProductionStats parent;
    private final PanelSide side;
    private final GuiGraph graph;

    private static final int GRAPH_TEXTURE_WIDTH = 238;
    private static final int GRAPH_TEXTURE_HEIGHT = 111;
    private static final int GRAPH_TEXTURE_BORDER = 8;
    private static final int GRAPH_HEIGHT = 95;
    private static final int GRAPH_WIDTH = 222;

    public GuiProductionStatsGraph(GuiProductionStats parent, PanelSide side) {
        this.parent = parent;
        this.side = side;
        this.graph = new GuiGraph(
                this.parent,
                GRAPH_TEXTURE_BORDER,
                GuiProductionStatsIntervals.getHeight() + GRAPH_TEXTURE_BORDER,
                GRAPH_WIDTH,
                GRAPH_HEIGHT,
                19,
                8);
        ProductionStatsDataManager.getInstance().registerConsumer(this::updateGraphData);
        HashMap<IAEStack, DataBufferHandler> bufferManagerMap = ProductionStatsDataManager.getInstance()
                .getDataBuffers();
        for (IAEStack key : bufferManagerMap.keySet()) {
            graph.addGraph(key, bufferManagerMap.get(key).GRAPH_COLOR);
        }
    }

    public void initGui() {}

    public void updateGraphData() {
        // TODO: Implement this method
        for (IAEStack key : ProductionStatsDataManager.getInstance().getDataBuffers().keySet()) {
            graph.addData(key, 50);
        }
    }

    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        switch (side) {
            case PRODUCTION -> {
                graph.draw(offsetX - parent.getGuiLeft(), offsetY - parent.getGuiTop(), mouseX, mouseY);
            }
        }
        drawTooltip(offsetX, offsetY, mouseX, mouseY);
    }

    public void drawBG(final int offsetX, final int offsetY) {
        int offsetXAdjusted = side.equals(PanelSide.PRODUCTION) ? offsetX : offsetX + GRAPH_TEXTURE_WIDTH;
        parent.bindTexture("guis/productionstatsgraph.png");
        parent.drawTexturedModalRect(offsetXAdjusted, offsetY, 0, 0, GRAPH_TEXTURE_WIDTH, GRAPH_TEXTURE_HEIGHT);
    }

    public void drawTooltip(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        String graphInfo = graph.getMouseOver(offsetX + GRAPH_TEXTURE_BORDER, offsetY + GuiProductionStatsIntervals.getHeight() + GRAPH_TEXTURE_BORDER, mouseX, mouseY);
        if (graphInfo.isEmpty()) {
            return;
        }
        this.parent.drawTooltip(mouseX - 180, mouseY - 60, 0, graphInfo);
    }

    public static int getWidth() {
        return GRAPH_TEXTURE_WIDTH;
    }

    public static int getHeight() {
        return GRAPH_TEXTURE_HEIGHT;
    }

    public GuiGraph getGraph() {
        return this.graph;
    }
}
