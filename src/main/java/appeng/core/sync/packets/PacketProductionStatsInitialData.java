package appeng.core.sync.packets;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import org.apache.commons.io.IOUtils;

import appeng.api.storage.data.IAEStack;
import appeng.client.gui.implementations.GuiProductionStats;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

public class PacketProductionStatsInitialData extends AppEngPacket {

    private enum StackType {
        NULL,
        ITEM,
        FLUID
    }

    private static final int CHUNK_SIZE = 1024 * 1024; // Send data in 1MiB chunks

    private ByteBuf receivedData = null;

    private static final WeakHashMap<EntityPlayer, ByteBuf[]> chunkStorage = new WeakHashMap<>();

    public PacketProductionStatsInitialData(final ByteBuf stream) throws IOException {
        if (FMLCommonHandler.instance().getSide() != Side.CLIENT) {
            throw new UnsupportedOperationException("A client tried to use a client-only packet on the server.");
        }
        receivedData = stream.slice();
    }

    private PacketProductionStatsInitialData(final ByteBuf chunkData, int chunkId, int totalChunks) {
        final ByteBuf output = Unpooled.buffer(12 + chunkData.readableBytes());
        output.writeInt(this.getPacketID());
        output.writeInt(chunkId);
        output.writeInt(totalChunks);
        output.writeBytes(chunkData);
        this.configureWrite(output);
    }

    public static List<PacketProductionStatsInitialData> createChunks(
            HashMap<IAEStack, ArrayList<Double>> initialData) {
        // This works
        final ByteBuf hashmapData = serializeHashMap(initialData);

        // Compression
        final ByteBuf output = Unpooled.buffer(hashmapData.readableBytes() + 4);
        try (final ByteBufOutputStream bbos = new ByteBufOutputStream(output);
                final GZIPOutputStream gzos = new GZIPOutputStream(bbos);
                final ByteBufInputStream bbis = new ByteBufInputStream(hashmapData)) {
            IOUtils.copy(bbis, gzos);
            gzos.flush();
        } catch (IOException e) {
            AELog.error(e, "Could not compress the serialized hash map.");
        }

        // Chunk splitting
        final int chunkCount = (int) Platform.ceilDiv(output.readableBytes(), CHUNK_SIZE);
        final ArrayList<PacketProductionStatsInitialData> chunks = new ArrayList<>(chunkCount);
        for (int chunk = 0; chunk < chunkCount; chunk++) {
            final int start = CHUNK_SIZE * chunk;
            final int end = Math.min(start + CHUNK_SIZE, output.readableBytes());
            final int len = end - start;
            chunks.add(new PacketProductionStatsInitialData(output.slice(start, len), chunk, chunkCount));
        }
        return chunks;
    }

    private static ByteBuf serializeHashMap(HashMap<IAEStack, ArrayList<Double>> initialData) {
        final ByteBuf data = Unpooled.buffer(4096).order(ByteOrder.LITTLE_ENDIAN);
        try {
            data.writeInt(initialData.size());
            for (IAEStack stack : initialData.keySet()) {
                writeStack(stack, data);
                data.writeInt(initialData.get(stack).size());
                for (Double value : initialData.get(stack)) {
                    data.writeDouble(value);
                }
            }
        } catch (IOException e) {
            AELog.error(e, "Could not serialize production stats data.");
        }
        return data.slice();
    }

    private static HashMap<IAEStack, ArrayList<Double>> deserializeHashMap(ByteBuf data) {
        HashMap<IAEStack, ArrayList<Double>> initialData = new HashMap<>();
        try {
            final int size = data.readInt();
            for (int i = 0; i < size; i++) {
                IAEStack stack = readStack(data);
                int valueSize = data.readInt();
                ArrayList<Double> values = new ArrayList<>(valueSize);
                for (int j = 0; j < valueSize; j++) {
                    values.add(data.readDouble());
                }
                initialData.put(stack, values);
            }
        } catch (IOException e) {
            AELog.error(e, "Could not deserialize production stats data.");
        }
        return initialData;
    }

    private static void writeStack(IAEStack<?> stack, ByteBuf buffer) throws IOException {
        if (stack == null) {
            buffer.writeByte(StackType.NULL.ordinal());
        } else if (stack instanceof AEItemStack) {
            buffer.writeByte(StackType.ITEM.ordinal());
        } else if (stack instanceof AEFluidStack) {
            buffer.writeByte(StackType.FLUID.ordinal());
        } else {
            throw new UnsupportedOperationException("Unknown stack type: " + stack.getClass().getName());
        }
        stack.writeToPacket(buffer);
    }

    public static IAEStack<?> readStack(ByteBuf buffer) throws IOException {
        final StackType stackType = StackType.values()[buffer.readByte()];
        return switch (stackType) {
            case NULL -> null;
            case ITEM -> AEItemStack.loadItemStackFromPacket(buffer);
            case FLUID -> AEFluidStack.loadFluidStackFromPacket(buffer);
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void clientPacketData(final INetworkInfo network, final AppEngPacket packet, final EntityPlayer player) {
        if (receivedData == null) {
            return;
        }
        final int chunkId = receivedData.readInt();
        final int totalChunks = receivedData.readInt();
        if (totalChunks <= 0 || chunkId < 0 || chunkId >= totalChunks) {
            AELog.warn(
                    "Invalid chunked production stats packet received from server: Chunk %d/%d",
                    chunkId,
                    totalChunks);
            return;
        }
        if (totalChunks == 1) {
            onFullClientData(receivedData.slice().order(ByteOrder.LITTLE_ENDIAN), player);
        } else {
            boolean packetComplete = false;
            ByteBuf[] storage;
            synchronized (chunkStorage) {
                storage = chunkStorage.get(player);
                if (storage == null || storage.length != totalChunks) {
                    storage = new ByteBuf[totalChunks];
                    chunkStorage.put(player, storage);
                }
                storage[chunkId] = receivedData.slice().order(ByteOrder.LITTLE_ENDIAN);
                if (Arrays.stream(storage).noneMatch(Objects::isNull)) {
                    chunkStorage.remove(player);
                    packetComplete = true;
                }
            }
            if (packetComplete) {
                ByteBuf combined = Unpooled.wrappedBuffer(storage).order(ByteOrder.LITTLE_ENDIAN);
                onFullClientData(combined, player);
            }
        }
    }

    private static void onFullClientData(ByteBuf data, EntityPlayer player) {
        final ByteBuf decompressedData = Unpooled.buffer().order(ByteOrder.LITTLE_ENDIAN);
        try (final ByteBufOutputStream bbos = new ByteBufOutputStream(decompressedData);
                final ByteBufInputStream bbis = new ByteBufInputStream(data);
                final GZIPInputStream gzis = new GZIPInputStream(bbis)) {
            IOUtils.copy(gzis, bbos);
            bbos.flush();
        } catch (IOException e) {
            AELog.error(e, "Could not decompress the serialized hash map.");
            return;
        }
        final HashMap<IAEStack, ArrayList<Double>> deserialized;
        try {
            deserialized = deserializeHashMap(decompressedData);
        } catch (Exception e) {
            AELog.error(e, "Could not deserialize production stats sent by the server.");
            return;
        }

        final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiProductionStats) {
            ((GuiProductionStats) screen).handleInitialData(deserialized);
        }
    }
}
