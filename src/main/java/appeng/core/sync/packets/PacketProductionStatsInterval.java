package appeng.core.sync.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.productionstats.IProductionStatsGrid;
import appeng.container.implementations.ContainerProductionStats;
import appeng.core.AELog;
import appeng.core.stats.ProductionStatsDataManager;
import appeng.core.stats.ProductionStatsDataManager.TimeIntervals;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.me.GridAccessException;
import appeng.parts.AEBasePart;
import appeng.tile.grid.AENetworkTile;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PacketProductionStatsInterval extends AppEngPacket {

    private final TimeIntervals intervals;

    public PacketProductionStatsInterval(final ByteBuf stream) {
        this.intervals = TimeIntervals.values()[stream.readInt()];
    }

    public PacketProductionStatsInterval(final TimeIntervals intervals) {
        this.intervals = intervals;

        final ByteBuf data = Unpooled.buffer();

        data.writeInt(this.getPacketID());
        data.writeInt(intervals.ordinal());

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player) {
        final EntityPlayerMP pmp = (EntityPlayerMP) player;
        final Container con = pmp.openContainer;

        if (con instanceof ContainerProductionStats conPS) {
            ProductionStatsDataManager dataManager = null;
            // Panels
            if (conPS.getTarget() instanceof AEBasePart part) {
                try {
                    dataManager = part.getProxy().getProductionStats().getDataManager();
                } catch (GridAccessException e) {
                    AELog.debug(e);
                }
                // Tiles
            } else if (conPS.getTarget() instanceof AENetworkTile tile) {
                try {
                    dataManager = tile.getProxy().getProductionStats().getDataManager();
                } catch (GridAccessException e) {
                    AELog.debug(e);
                }
                // Portable Cells.. yeap
            } else if (conPS.getTarget() instanceof WirelessTerminalGuiObject wirelessTerm) {
                final IGridNode n = wirelessTerm.getGridNode(ForgeDirection.UNKNOWN);
                if (n != null) {
                    final IGrid g = n.getGrid();
                    if (g != null) {
                        final IProductionStatsGrid psg = g.getCache(IProductionStatsGrid.class);
                        if (psg != null) {
                            dataManager = psg.getDataManager();
                        }
                    }
                }
            }
            if (dataManager != null) {
                System.out.println("Setting interval to " + this.intervals);
                dataManager.setInterval(player.getUniqueID(), this.intervals);
            }
        }
    }
}
