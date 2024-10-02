package appeng.client.gui.widgets;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiProductionStats;
import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;
import appeng.container.implementations.ContainerProductionStatsList;

import java.util.List;

public class GuiProductionStatsList {

    private ContainerProductionStatsList container;

    private final GuiProductionStats parent;
    private final PanelSide side;

    public static final int LIST_TEXTURE_WIDTH = 238;
    public static final int LIST_TEXTURE_HEIGHT = 153;
    private static final int SCROLLBAR_X_OFFSET = 218;
    private static final int SCROLLBAR_Y_OFFSET = 147;
    private static final int SCROLLBAR_TEXTURE_HEIGHT = 137;

    private final GuiScrollbar scrollbar;

    public GuiProductionStatsList(GuiProductionStats parent, PanelSide side) {
        this.parent = parent;
        this.side = side;

        this.scrollbar = new GuiScrollbar();
        updateScrollbar();
    }

    public void initGui() {
        initScrollbar();
    }

    public void updateScrollbar() {
        scrollbar.setHeight(SCROLLBAR_TEXTURE_HEIGHT);
    }

    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        scrollbar.draw(parent);
    }

    public void drawBG(final int offsetX, final int offsetY) {
        int offsetXAdjusted = this.side.equals(PanelSide.PRODUCTION) ? offsetX : offsetX + LIST_TEXTURE_WIDTH;
        parent.bindTexture("guis/productionstatslist.png");
        parent.drawTexturedModalRect(offsetXAdjusted, offsetY, 0, 0, LIST_TEXTURE_WIDTH, LIST_TEXTURE_HEIGHT);
        updateScrollbar();
    }

    public void handleDataUpdate(List<IAEItemStack> data) {

    }

    public void mouseClicked(final int x, final int y, final int button) {
        if (scrollbar != null) {
            scrollbar.click(parent, x, y);
        }
    }

    public void mouseClickMove(final int x, final int y) {
        if (scrollbar != null) {
            scrollbar.click(parent, x, y);
        }
    }

    private void initScrollbar() {
        scrollbar.setTop(SCROLLBAR_Y_OFFSET);
        scrollbar.setWidth(12);
        switch (side) {
            case PRODUCTION -> scrollbar.setLeft(SCROLLBAR_X_OFFSET);
            case CONSUMPTION -> scrollbar.setLeft(SCROLLBAR_X_OFFSET + LIST_TEXTURE_WIDTH);
        }
        scrollbar.setRange(0, 7, 1);
    }

    public static int getWidth() {
        return LIST_TEXTURE_WIDTH;
    }

    public static int getHeight() {
        return LIST_TEXTURE_HEIGHT;
    }
}
