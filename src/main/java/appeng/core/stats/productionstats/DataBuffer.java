package appeng.core.stats.productionstats;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;

import appeng.core.stats.ProductionStatsDataManager.TimeIntervals;

public class DataBuffer {

    private final TimeIntervals interval;
    private final int bufferSize;
    private final Deque<Double> data;
    private double summedData = 0;
    private double lastValue = 0;
    private int tickCounter = 0;
    private boolean newData = false;
    private final static int TICK_TO_SEC = 20;

    private DataBuffer childBuffer;

    public DataBuffer(List<TimeIntervals> intervals, int bufferSize) {
        this.interval = intervals.remove(0);
        this.bufferSize = bufferSize;
        this.data = new ArrayDeque<>();
        setupChild(intervals);
    }

    private void setupChild(List<TimeIntervals> intervals) {
        if (!intervals.isEmpty()) {
            this.childBuffer = new DataBuffer(intervals, this.bufferSize);
        } else {
            this.childBuffer = null;
        }
    }

    // Write to buffer without adding a data point, only ticking the buffer adds new data points
    public void addData(double value) {
        if (this.data.isEmpty()) {
            this.summedData += value;
            return;
        }
        double lastVal = this.data.removeLast();
        lastVal += value;
        this.data.add(lastVal);
        this.summedData += value;
    }

    private void compactData(double value) {
        this.data.add(value);
        if (this.data.size() > this.bufferSize) {
            this.data.pop();
        }
        this.newData = true;
        this.summedData += value;
    }

    public void tickBuffer() {
        // Tick the non-child buffer
        if (this.interval == TimeIntervals.FIVE_SECONDS) {
            this.data.add(0.0d);
            this.newData = true;
        }
        // Tick the child buffer
        if (this.childBuffer == null) {
            return;
        }
        this.childBuffer.tickBuffer();
        this.tickCounter++;
        if (this.tickCounter >= determineTickInterval()) {
            this.childBuffer.compactData(this.summedData);
            this.tickCounter = 0;
            this.summedData = 0;
        }
    }

    public boolean hasNewData() {
        if (this.newData) {
            this.newData = false;
            return true;
        }
        return false;
    }

    public DataBuffer getChildBuffer() {
        return this.childBuffer;
    }

    public boolean hasChild() {
        return this.childBuffer != null;
    }

    public TimeIntervals getInterval() {
        return this.interval;
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
        return this.tickCounter;
    }

    public void setTickCounter(int tickCounter) {
        this.tickCounter = tickCounter;
    }

    public double getSummedData() {
        return this.summedData;
    }

    public void setSummedData(double summedData) {
        this.summedData = summedData;
    }

    // Used to determine when to save the sum to the child buffer
    private int determineTickInterval() {
        switch (this.interval) {
            case FIVE_SECONDS -> {
                return intervalToBufferSize(TimeIntervals.FIVE_SECONDS.getSeconds());
            }
            case ONE_MINUTES -> {
                return intervalToBufferSize(TimeIntervals.ONE_MINUTES.getSeconds());
            }
            case TEN_MINUTES -> {
                return intervalToBufferSize(TimeIntervals.TEN_MINUTES.getSeconds());
            }
            case ONE_HOURS -> {
                return intervalToBufferSize(TimeIntervals.ONE_HOURS.getSeconds());
            }
            case TEN_HOURS -> {
                return intervalToBufferSize(TimeIntervals.TEN_HOURS.getSeconds());
            }
            case FIFTY_HOURS -> {
                return intervalToBufferSize(TimeIntervals.FIFTY_HOURS.getSeconds());
            }
            case TWO_FIFTY_HOURS -> {
                return intervalToBufferSize(TimeIntervals.TWO_FIFTY_HOURS.getSeconds());
            }
        }
        return 0;
    }

    private int intervalToBufferSize(int intervalSeconds) {
        return TICK_TO_SEC * (TICK_TO_SEC * intervalSeconds / this.bufferSize);
    }
}
