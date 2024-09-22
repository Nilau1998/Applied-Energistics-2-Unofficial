package appeng.core.worlddata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.base.Preconditions;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.AELog;
import appeng.core.stats.ProductionStatsDataManager;
import appeng.core.stats.productionstats.DataBufferHandler;
import appeng.util.item.AEItemStack;

public final class ProductionStatsData implements IProductionStatsData, IOnWorldStartable, IOnWorldStoppable {

    @Nonnull
    private final File productionStatsDirectory;
    private static final String fileName = "production_stats.dat";

    public ProductionStatsData(@Nonnull final File productionStatsDirectory) {
        Preconditions.checkNotNull(productionStatsDirectory);

        this.productionStatsDirectory = productionStatsDirectory;
    }

    @Override
    public void serializeBufferMap() {
        HashMap<IAEStack, DataBufferHandler> productionDataBuffers = ProductionStatsDataManager.getInstance()
                .getDataBuffers();
        synchronized (ProductionStatsData.class) {
            final NBTTagCompound data = this.loadProductionStatsData();
            for (IAEStack stack : productionDataBuffers.keySet()) {
                final NBTTagCompound bufferTag = new NBTTagCompound();
                stack.writeToNBT(bufferTag);
                bufferTag.setTag("buf", productionDataBuffers.get(stack).packBuffers());
                data.setTag(stack.toString(), bufferTag);
            }
            writeProductionStatsData(data);
        }
    }

    @Override
    public void deserializeBufferMap() {
        HashMap<IAEStack, DataBufferHandler> readProductionDataBuffers = new HashMap<>();

        final NBTTagCompound data = this.loadProductionStatsData();
        for (Object o : data.func_150296_c()) {
            // Reconstruct stack & buffer information
            final String name = (String) o;
            NBTTagCompound tag = data.getCompoundTag(name);
            NBTTagCompound buffer = tag.getCompoundTag("buf");
            tag.removeTag("buf");
            IAEItemStack stack = AEItemStack.loadItemStackFromNBT(tag);

            // Put stack & buffer
            DataBufferHandler manager = new DataBufferHandler();
            manager.unpackBuffers(buffer);
            readProductionDataBuffers.put(stack, manager);
        }

        ProductionStatsDataManager.getInstance().setDataBuffers(readProductionDataBuffers);
    }

    private void writeProductionStatsData(final NBTTagCompound data) {
        final File file = new File(this.productionStatsDirectory, fileName);
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(file);
            CompressedStreamTools.writeCompressed(data, fileOutputStream);
        } catch (final Throwable e) {
            AELog.debug(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (final IOException e) {
                    AELog.debug(e);
                }
            }
        }
    }

    private NBTTagCompound loadProductionStatsData() {
        NBTTagCompound data = null;
        final File file = new File(this.productionStatsDirectory, fileName);

        if (file.isFile()) {
            FileInputStream fileInputStream = null;

            try {
                fileInputStream = new FileInputStream(file);
                data = CompressedStreamTools.readCompressed(fileInputStream);
            } catch (final Throwable e) {
                data = new NBTTagCompound();
                AELog.debug(e);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (final IOException e) {
                        AELog.debug(e);
                    }
                }
            }
        } else {
            data = new NBTTagCompound();
        }

        return data;
    }

    @Override
    public void onWorldStart() {
        deserializeBufferMap();
    }

    @Override
    public void onWorldStop() {
        serializeBufferMap();
    }
}
