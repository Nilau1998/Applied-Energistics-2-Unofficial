package appeng.client.gui.widgets;

import appeng.client.gui.implementations.GuiProductionStats;

public class GuiProductionStatsPanel {

    private final GuiProductionStats parent;

    public final PanelSide side;
    private final GuiProductionStatsGraph graph;
    private final GuiProductionStatsList list;
    private GuiProductionStatsIntervals intervals;
    private int widgetWidth, widgetHeight;

    public enum PanelSide {
        PRODUCTION,
        CONSUMPTION;
    }

    public GuiProductionStatsPanel(GuiProductionStats parent, PanelSide side) {
        this.parent = parent;
        this.side = side;
        this.graph = new GuiProductionStatsGraph(parent, side);
        this.list = new GuiProductionStatsList(parent, side);
        if (side.equals(PanelSide.PRODUCTION)) {
            this.intervals = new GuiProductionStatsIntervals(parent);
            this.widgetHeight += GuiProductionStatsIntervals.getHeight();
        }
        this.widgetWidth = Math.max(GuiProductionStatsGraph.getWidth(), GuiProductionStatsList.getWidth());
        this.widgetHeight += GuiProductionStatsGraph.getHeight() + GuiProductionStatsList.getHeight();
    }

    public void initGui() {
        graph.initGui();
        if (intervals != null) {
            intervals.initGui();
        }
    }

    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        graph.drawFG(offsetX, offsetY, mouseX, mouseY);
        if (intervals != null) {
            intervals.drawFG(offsetX, offsetY, mouseX, mouseY);
        }
    }

    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        graph.drawBG(offsetX, offsetY + GuiProductionStatsIntervals.getHeight());
        list.drawBG(offsetX, offsetY + GuiProductionStatsGraph.getHeight() + GuiProductionStatsIntervals.getHeight());
        if (intervals != null) {
            intervals.drawBG(offsetX, offsetY);
        }
    }

    public int getWidth() {
        return this.widgetWidth;
    }

    public int getHeight() {
        return this.widgetHeight;
    }
}