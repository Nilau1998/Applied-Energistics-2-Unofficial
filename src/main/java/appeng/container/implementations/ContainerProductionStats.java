package appeng.container.implementations;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;
import appeng.container.AEBaseContainer;

public class ContainerProductionStats extends AEBaseContainer {

    private final ContainerProductionStatsList productionPanelContainer;
    private final ContainerProductionStatsList consumptionPanelContainer;

    private IGrid grid;

    public ContainerProductionStats(final InventoryPlayer ip, final Object te) {
        super(ip, te);
        this.productionPanelContainer = new ContainerProductionStatsList(this, PanelSide.PRODUCTION);
        this.consumptionPanelContainer = new ContainerProductionStatsList(this, PanelSide.CONSUMPTION);

        final IActionHost host = this.getActionHost();
        if (host != null) {
            final IGridNode gn = host.getActionableNode();
            if (gn != null) {
                this.grid = gn.getGrid();
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        productionPanelContainer.detectAndSendChanges();
        consumptionPanelContainer.detectAndSendChanges();
        super.detectAndSendChanges();
    }

    public IGrid getGrid() {
        return grid;
    }
}
