package appeng.core.stats.productionstats;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;

import appeng.core.stats.ProductionStatsDataManager.TimeIntervals;

public class DataBuffer {

    private final TimeIntervals interval;
    private final int bufferSize;
    private final Deque<Double> data;
    private double summedData = 0;
    private int tickCounter = 0;

    private DataBuffer childBuffer;

    public DataBuffer(List<TimeIntervals> intervals, int bufferSize) {
        this.interval = intervals.remove(0);
        this.bufferSize = bufferSize;
        this.data = new ArrayDeque<>();
        setupChild(intervals);
    }

    private void setupChild(List<TimeIntervals> intervals) {
        if (!intervals.isEmpty()) {
            childBuffer = new DataBuffer(intervals, this.bufferSize);
        } else {
            childBuffer = null;
        }
    }

    public void writeToBuffer(double value) {
        this.data.add(value);
        if (this.data.size() > this.bufferSize) {
            this.data.pop();
        }
        this.summedData += value;
    }

    public void tickBuffer() {
        if (childBuffer == null) {
            return;
        }
        childBuffer.tickBuffer();
        this.tickCounter++;
        if (this.tickCounter >= determineTickInterval()) {
            this.childBuffer.writeToBuffer(this.summedData);
            this.tickCounter = 0;
            this.summedData = 0;
        }
    }

    public DataBuffer getChildBuffer() {
        return childBuffer;
    }

    public boolean hasChild() {
        return childBuffer != null;
    }

    public TimeIntervals getInterval() {
        return interval;
    }

    public NBTTagList getDataNBT() {
        final NBTTagList data = new NBTTagList();
        for (Double datum : this.data) {
            data.appendTag(new NBTTagDouble(datum));
        }
        return data;
    }

    public void setDataFromNBT(NBTTagList data) {
        for (int i = 0; i < data.tagCount(); i++) {
            this.data.add(data.func_150309_d(i));
        }
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public void setTickCounter(int tickCounter) {
        this.tickCounter = tickCounter;
    }

    public double getSummedData() {
        return summedData;
    }

    public void setSummedData(double summedData) {
        this.summedData = summedData;
    }

    private int determineTickInterval() {
        switch (interval) {
            case FIVE_SECONDS -> {
                return 1; // Every tick
            }
            case ONE_MINUTES -> {
                return 10; // Every .5 seconds
            }
            case TEN_MINUTES -> {
                return 100; // Every 5 seconds
            }
            default -> {
                return 200; // Every 10 seconds
            }
        }
    }
}
