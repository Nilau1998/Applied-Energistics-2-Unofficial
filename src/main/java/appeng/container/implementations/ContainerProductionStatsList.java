package appeng.container.implementations;

import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;

public class ContainerProductionStatsList {

    private final ContainerProductionStats parent;
    private final PanelSide side;

    public ContainerProductionStatsList(ContainerProductionStats parent, PanelSide side) {
        this.parent = parent;
        this.side = side;
    }

    public void detectAndSendChanges() {

    }
}
