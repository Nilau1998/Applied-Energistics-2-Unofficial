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

    public void drawFG(final int globalOffsetX, final int globalOffsetY, final int offsetX, final int offsetY,
                       final int mouseX, final int mouseY) {

    }

    public void drawBG(final int offsetX, final int offsetY) {
        int offsetXAdjusted = this.side.equals(PanelSide.PRODUCTION) ? offsetX : offsetX + LIST_TEXTURE_WIDTH;
        parent.bindTexture("guis/productionstatslist.png");
        parent.drawTexturedModalRect(offsetXAdjusted, offsetY, 0, 0, LIST_TEXTURE_WIDTH, LIST_TEXTURE_HEIGHT);
    }

    public static int getWidth() {
        return LIST_TEXTURE_WIDTH;
    }

    public static int getHeight() {
        return LIST_TEXTURE_HEIGHT;
    }
}