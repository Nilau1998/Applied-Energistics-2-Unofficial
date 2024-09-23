package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;
import appeng.container.AEBaseContainer;

public class ContainerProductionStats extends AEBaseContainer {

    private final ContainerProductionStatsList productionPanelContainer;
    private final ContainerProductionStatsList consumptionPanelContainer;

    public ContainerProductionStats(final InventoryPlayer ip, final Object te) {
        super(ip, te);
        this.productionPanelContainer = new ContainerProductionStatsList(this, PanelSide.PRODUCTION);
        this.consumptionPanelContainer = new ContainerProductionStatsList(this, PanelSide.CONSUMPTION);
    }

    @Override
    public void detectAndSendChanges() {
        productionPanelContainer.detectAndSendChanges();
        consumptionPanelContainer.detectAndSendChanges();
        super.detectAndSendChanges();
    }
}
