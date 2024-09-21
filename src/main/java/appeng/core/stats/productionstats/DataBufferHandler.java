package appeng.core.stats.productionstats;

import appeng.core.stats.ProductionStatsDataManager.TimeIntervals;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DataBufferHandler {
    private final DataBuffer dataBuffer;
    public int GRAPH_COLOR = generateColor();
    private static final int bufferSize = 20; // TODO: Change this?

    public DataBufferHandler() {
        List<TimeIntervals> intervals = new ArrayList<>(Arrays.asList(TimeIntervals.class.getEnumConstants()));
        this.dataBuffer = new DataBuffer(intervals, bufferSize);
    }

    public void writeToBuffer(double value) {
        this.dataBuffer.writeToBuffer(value);
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

    public NBTTagCompound packBuffers() {
        final NBTTagCompound data = new NBTTagCompound();
        for (TimeIntervals interval : TimeIntervals.values()) {
            DataBuffer buffer = getBuffer(interval);
            data.setTag(interval.name(), buffer.getDataNBT());
            data.setInteger(interval.name() + "TickCounter", buffer.getTickCounter());
            data.setDouble(interval.name() + "SummedData", buffer.getSummedData());
        }
        data.setInteger("GraphColor", this.GRAPH_COLOR);
        return data;
    }

    public void unpackBuffers(NBTTagCompound data) {
        for (TimeIntervals interval : TimeIntervals.values()) {
            NBTTagList values = (NBTTagList) data.getTag(interval.name());
            getBuffer(interval).setDataFromNBT(values);
            getBuffer(interval).setTickCounter(data.getInteger(interval.name() + "TickCounter"));
            getBuffer(interval).setSummedData(data.getDouble(interval.name() + "SummedData"));
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
