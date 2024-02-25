package appeng.client.gui.widgets;

import appeng.client.gui.implementations.GuiProductionStats;

public class GuiProductionStatsPanel {

    private final GuiProductionStats parent;

    public final PanelSide side;
    private final GuiProductionStatsGraph graph;
    private final GuiProductionStatsList list;
    public int widgetX, widgetY;

    public enum PanelSide {
        LEFT,
        RIGHT;
    }

    public GuiProductionStatsPanel(GuiProductionStats parent, PanelSide side) {
        this.parent = parent;
        this.side = side;
        this.graph = new GuiProductionStatsGraph(parent, side);
        this.list = new GuiProductionStatsList(parent, side);
        this.widgetX = Math.max(this.graph.getWidth(), this.list.getWidth());
        this.widgetY = this.graph.getHeight() + this.list.getHeight();
    }

    public void initGui() {
        graph.initGui();
    }

    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        graph.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        graph.drawBG(offsetX, offsetY + this.widgetY - this.graph.getHeight(), mouseX, mouseY);
        list.drawBG(offsetX, offsetY + this.widgetY);
    }
}
