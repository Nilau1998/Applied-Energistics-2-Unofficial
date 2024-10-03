package appeng.container.implementations;

import appeng.api.networking.productionstats.IProductionStatsGrid;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;
import appeng.core.AELog;
import appeng.core.stats.ProductionStatsDataManager;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketProductionStatsUpdate;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ContainerProductionStatsList {

    private final ContainerProductionStats parent;
    private final PanelSide side;

    public ContainerProductionStatsList(ContainerProductionStats parent, PanelSide side) {
        this.parent = parent;
        this.side = side;
    }

    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            UUID playerUUID = this.parent.getInventoryPlayer().player.getUniqueID();
            IProductionStatsGrid pg = this.parent.getGrid().getCache(IProductionStatsGrid.class);
            ProductionStatsDataManager dataManager = pg.getDataManager();
            if (dataManager != null) {
                ArrayList<IAEStack> data = dataManager.getLastRateEntry(playerUUID);
                if (!data.isEmpty()) {
                    try {
                        PacketProductionStatsUpdate packet = new PacketProductionStatsUpdate();
                        for (IAEStack stack : data) {
                            packet.appendItem(stack);
                        }
                        NetworkHandler.instance.sendTo(packet, (EntityPlayerMP) parent.getInventoryPlayer().player);
                    } catch (final IOException e) {
                        AELog.debug(e);
                    }
                }
            }

        }
    }
}
