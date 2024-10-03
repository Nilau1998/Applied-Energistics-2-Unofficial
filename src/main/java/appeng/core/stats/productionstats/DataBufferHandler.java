package appeng.core.stats.productionstats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

import appeng.core.AEConfig;
import appeng.core.stats.ProductionStatsDataManager.TimeIntervals;

public class DataBufferHandler {

    private final DataBuffer dataBuffer;
    private static final int bufferSize = AEConfig.instance.productionStatsBufferSize;

    public DataBufferHandler() {
        List<TimeIntervals> intervals = new ArrayList<>(Arrays.asList(TimeIntervals.class.getEnumConstants()));
        this.dataBuffer = new DataBuffer(intervals, bufferSize);
    }

    public void addData(double value) {
        this.dataBuffer.addData(value);
    }

    public void tickBuffer() {
        this.dataBuffer.tickBuffer();
    }

    public DataBuffer getBuffer(TimeIntervals interval) {
        // Is top what we want?
        if (this.dataBuffer.getInterval().equals(interval)) {
            return this.dataBuffer;
        }
        // Going top to bottom
        DataBuffer buffer = this.dataBuffer;
        while (buffer.hasChild()) {
            buffer = buffer.getChildBuffer();
            if (buffer.getInterval().equals(interval)) {
                return buffer;
            }
        }
        return null;
    }

    public boolean hasPublishableData(TimeIntervals interval) {
        DataBuffer buffer = getBuffer(interval);
        return buffer != null && buffer.hasNewData();
    }

    public NBTTagCompound packBuffers() {
        final NBTTagCompound data = new NBTTagCompound();
        for (TimeIntervals interval : TimeIntervals.values()) {
            DataBuffer buffer = getBuffer(interval);
            data.setTag(interval.name(), buffer.getBufferNBT());
        }
        return data;
    }

    public void unpackBuffers(NBTTagCompound data) {
        for (TimeIntervals interval : TimeIntervals.values()) {
            NBTTagCompound values = (NBTTagCompound) data.getTag(interval.name());
            getBuffer(interval).setBufferNBT(values);
        }
    }
}
