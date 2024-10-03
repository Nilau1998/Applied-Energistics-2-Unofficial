package appeng.container.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;

import appeng.api.networking.productionstats.IProductionStatsGrid;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;
import appeng.core.AELog;
import appeng.core.stats.ProductionStatsDataManager;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketProductionStatsInitialData;
import appeng.core.sync.packets.PacketProductionStatsUpdate;
import appeng.util.Platform;

public class ContainerProductionStatsList {

    private final ContainerProductionStats parent;
    private final PanelSide side;

    private boolean needsInitialData = false;

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
                // Send all rate entries to the client
                if (this.needsInitialData) {
                    this.needsInitialData = false;
                    final List<PacketProductionStatsInitialData> initialData;
                    HashMap<IAEStack, ArrayList<Double>> data = dataManager.getRates(playerUUID);
                    if (!data.isEmpty()) {
                        initialData = PacketProductionStatsInitialData.createChunks(dataManager.getRates(playerUUID));
                        for (PacketProductionStatsInitialData packet : initialData) {
                            NetworkHandler.instance.sendTo(packet, (EntityPlayerMP) parent.getInventoryPlayer().player);
                        }
                    }
                }

                // Send the last rate entry to the client
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

    public void sendInitialData() {
        this.needsInitialData = true;
    }
}
