package appeng.core.sync.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.implementations.GuiProductionStats;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/*
 * Essentially this class is similar to PacketMEInventoryUpdate, but it is used to update production statistics.
 */
public class PacketProductionStatsUpdate extends AppEngPacket {

    private static final int UNCOMPRESSED_PACKET_BYTE_LIMIT = 16 * 1024 * 1024;
    private static final int OPERATION_BYTE_LIMIT = 2 * 1024;
    private static final int TEMP_BUFFER_SIZE = 1024;
    private static final int STREAM_MASK = 0xff;

    // input.
    @Nullable
    private final List<IAEItemStack> list;
    // output...
    private final byte ref;

    @Nullable
    private final ByteBuf data;

    @Nullable
    private final GZIPOutputStream compressFrame;

    private int writtenBytes = 0;
    private boolean empty = true;

    public PacketProductionStatsUpdate(final ByteBuf stream) throws IOException {
        this.data = null;
        this.compressFrame = null;
        this.list = new LinkedList<>();
        this.ref = stream.readByte();

        final GZIPInputStream gzReader = new GZIPInputStream(new InputStream() {

            @Override
            public int read() throws IOException {
                if (stream.readableBytes() <= 0) {
                    return -1;
                }

                return stream.readByte() & STREAM_MASK;
            }
        });

        final ByteBuf uncompressed = Unpooled.buffer(stream.readableBytes());
        final byte[] tmp = new byte[TEMP_BUFFER_SIZE];
        while (gzReader.available() != 0) {
            final int bytes = gzReader.read(tmp);
            if (bytes > 0) {
                uncompressed.writeBytes(tmp, 0, bytes);
            }
        }
        gzReader.close();

        while (uncompressed.readableBytes() > 0) {
            this.list.add(AEItemStack.loadItemStackFromPacket(uncompressed));
        }

        this.empty = this.list.isEmpty();
    }

    public PacketProductionStatsUpdate() throws IOException {
        this((byte) 0);
    }

    public PacketProductionStatsUpdate(final byte ref) throws IOException {
        this.ref = ref;
        this.data = Unpooled.buffer(OPERATION_BYTE_LIMIT);
        this.data.writeInt(this.getPacketID());
        this.data.writeByte(this.ref);

        this.compressFrame = new GZIPOutputStream(new OutputStream() {

            @Override
            public void write(final int value) throws IOException {
                PacketProductionStatsUpdate.this.data.writeByte(value);
            }
        });

        this.list = null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void clientPacketData(final INetworkInfo network, final AppEngPacket packet, final EntityPlayer player) {
        final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiProductionStats) {
            ((GuiProductionStats) screen).handleDataUpdate(list);
        }
    }

    @Nullable
    @Override
    public FMLProxyPacket getProxy() {
        try {
            this.compressFrame.close();

            this.configureWrite(this.data);
            return super.getProxy();
        } catch (final IOException e) {
            AELog.debug(e);
        }

        return null;
    }

    public void appendItem(final IAEStack is) throws IOException, BufferOverflowException {
        final ByteBuf tmp = Unpooled.buffer(OPERATION_BYTE_LIMIT);
        is.writeToPacket(tmp);

        this.compressFrame.flush();
        if (this.writtenBytes + tmp.readableBytes() > UNCOMPRESSED_PACKET_BYTE_LIMIT) {
            throw new BufferOverflowException();
        } else {
            this.writtenBytes += tmp.readableBytes();
            this.compressFrame.write(tmp.array(), 0, tmp.readableBytes());
            this.empty = false;
        }
    }

    public int getLength() {
        return this.data.readableBytes();
    }

    public boolean isEmpty() {
        return this.empty;
    }
}
