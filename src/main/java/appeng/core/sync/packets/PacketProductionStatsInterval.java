package appeng.core.sync.packets;

import net.minecraft.entity.player.EntityPlayer;

import appeng.core.stats.ProductionStatsDataManager;
import appeng.core.stats.ProductionStatsDataManager.TimeIntervals;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
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
        ProductionStatsDataManager.getInstance().setInterval(player.getUniqueID(), this.intervals);
    }
}
