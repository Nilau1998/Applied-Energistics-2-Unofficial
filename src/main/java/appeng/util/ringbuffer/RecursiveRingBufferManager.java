package appeng.util.ringbuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import appeng.core.stats.ProductionStatsManager.TimeIntervals;

public class RecursiveRingBufferManager {

    private final RecursiveRingBuffer buffer;
    public int GRAPH_COLOR = generateColor();
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
        data.setInteger("GraphColor", this.GRAPH_COLOR);
        return data;
    }

    public void unpackBuffers(NBTTagCompound data) {
        for (TimeIntervals interval : TimeIntervals.values()) {
            NBTTagList values = (NBTTagList) data.getTag(interval.name());
            getBuffer(interval).setBuffer(values);
        }
        this.GRAPH_COLOR = data.getInteger("GraphColor");
    }

    private int generateColor() {
        Random rand = new Random();
        return getColorDecimal(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
    }

    private int getColorDecimal(int red, int green, int blue) {
        int rgb = (255 << 24);
        rgb = rgb | (red << 16);
        rgb = rgb | (green << 8);
        rgb = rgb | (blue);
        return rgb;
    }
}
