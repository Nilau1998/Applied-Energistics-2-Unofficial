package appeng.container.implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;

import appeng.api.storage.data.IAEStack;
import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;
import appeng.container.AEBaseContainer;
import appeng.core.AELog;
import appeng.core.stats.ProductionStatsDataManager;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketProductionStatsUpdate;
import appeng.util.Platform;

public class ContainerProductionStatsList {

    private final AEBaseContainer parent;
    private final PanelSide side;

    private List<IAEStack> items = new ArrayList<>();

    public ContainerProductionStatsList(AEBaseContainer parent, PanelSide side) {
        this.parent = parent;
        this.side = side;
    }

    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            UUID playerUUID = parent.getInventoryPlayer().player.getUniqueID();
            ProductionStatsDataManager dataManager = ProductionStatsDataManager.getInstance();
            ArrayList<IAEStack> data = dataManager.getLastSummedData(playerUUID);
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
