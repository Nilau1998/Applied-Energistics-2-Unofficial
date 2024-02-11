package appeng.util.ringbuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import appeng.core.stats.ProductionStatsManager.TimeIntervals;

public class RecursiveRingBufferManager {

    private final RecursiveRingBuffer buffer;
    private static final int bufferSize = 20;

    public RecursiveRingBufferManager() {
        List<TimeIntervals> intervals = new ArrayList<>(Arrays.asList(TimeIntervals.class.getEnumConstants()));
        this.buffer = new RecursiveRingBuffer(intervals, bufferSize);
    }

    public void writeToBuffer(float value) {
        this.buffer.writeToBuffer(value);
    }

    public RecursiveRingBuffer getBuffer(TimeIntervals interval) {
        // Is top what we want?
        if (this.buffer.getInterval().equals(interval)) {
            return this.buffer;
        }
        // Going top to bottom
        RecursiveRingBuffer buffer = this.buffer;
        while (buffer.hasChild()) {
            buffer = buffer.getChild();
            if (buffer.getInterval().equals(interval)) {
                return buffer;
            }
        }
        return null;
    }

    public NBTTagCompound packBuffers() {
        final NBTTagCompound data = new NBTTagCompound();
        for (TimeIntervals interval : TimeIntervals.values()) {
            RecursiveRingBuffer buffer = getBuffer(interval);
            data.setTag(interval.name(), buffer.getBuffer());
        }
        return data;
    }

    public void unpackBuffers(NBTTagCompound data) {
        for (TimeIntervals interval : TimeIntervals.values()) {
            NBTTagList values = (NBTTagList) data.getTag(interval.name());
            getBuffer(interval).setBuffer(values);
        }
    }
}
