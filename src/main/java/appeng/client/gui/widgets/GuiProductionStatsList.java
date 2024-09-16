package appeng.client.gui.widgets;

import appeng.client.gui.implementations.GuiProductionStats;
import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;

public class GuiProductionStatsList {

    private final GuiProductionStats parent;
    private final PanelSide side;

    public static final int LIST_TEXTURE_WIDTH = 238;
    public static final int LIST_TEXTURE_HEIGHT = 153;

    public GuiProductionStatsList(GuiProductionStats parent, PanelSide side) {
        this.parent = parent;
        this.side = side;
    }

    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {

    }

    public void drawBG(int offsetX, int offsetY) {
        int offsetXAdjusted = this.side.equals(PanelSide.LEFT) ? offsetX - (LIST_TEXTURE_WIDTH / 2) + 1: offsetX + (LIST_TEXTURE_WIDTH / 2) - 1;
        int offsetYAdjusted = offsetY + (LIST_TEXTURE_HEIGHT / 2) - 2;
        parent.bindTexture("guis/productionstatslist.png");
        parent.drawTexturedModalRect(offsetXAdjusted, offsetYAdjusted, 0, 0, LIST_TEXTURE_WIDTH, LIST_TEXTURE_HEIGHT);
    }

    public int getWidth() {
        return LIST_TEXTURE_WIDTH;
    }

    public int getHeight() {
        return LIST_TEXTURE_HEIGHT;
    }
}