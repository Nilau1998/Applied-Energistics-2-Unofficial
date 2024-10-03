package appeng.core.stats.productionstats;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;

import appeng.core.stats.ProductionStatsDataManager.TimeIntervals;

public class DataBuffer {

    private final TimeIntervals interval;
    private final int bufferSize;
    private final Deque<Double> rates; // Used to store the rates
    private final Deque<Double> incomingData; // Used to store the incoming data
    private int tickCounter = 0;
    private boolean newData = false;//
    private final static int TICK_TO_SEC = 20;

    private DataBuffer childBuffer;

    public DataBuffer(List<TimeIntervals> intervals, int bufferSize) {
        this.interval = intervals.remove(0);
        this.bufferSize = bufferSize;
        this.rates = new ArrayDeque<>();
        this.incomingData = new ArrayDeque<>();
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
        if (this.incomingData.isEmpty()) {
            return;
        }
        double lastVal = this.incomingData.removeLast();
        lastVal += value;
        this.incomingData.add(lastVal);
    }

    // Pass a data point down into the child buffer (this because recursion)
    private void passToChild(double value) {
        this.rates.add(value);
        if (this.rates.size() > this.bufferSize) {
            this.rates.pop();
        }
        this.newData = true;
    }

    public void tickBuffer() {
        this.tickCounter++;
        // 5 second interval has new data every tick
        if (this.interval == TimeIntervals.FIVE_SECONDS) {
            this.newData = true;
            // Write 0 every tick, addData will actually write new data, if there is any
            this.incomingData.add(0d);
            if (this.incomingData.size() > this.bufferSize) {
                this.incomingData.pop();
            }
            calculateRate();
        }
        if (this.childBuffer == null) {
            return;
        }
        this.childBuffer.tickBuffer();
        if (this.tickCounter >= determineTickInterval()) {
            this.childBuffer.passToChild(this.rates.getLast());
            this.tickCounter = 0;
        }
    }

    /*
     * Calculate at which rate items are being written into incoming date. Since this data comes in every tick, the
     * incomingData deque carries the last 5 seconds. Meaning that the sum of incomingData is items per 5 seconds.
     */
    private void calculateRate() {
        double ratio = ((1 / 5d) / (1 / 60d)); // 1/5s to 1/60s
        double rate = this.incomingData.stream().mapToDouble(Double::doubleValue).sum() * ratio;
        this.rates.add(rate);
        if (this.rates.size() > this.bufferSize) {
            this.rates.pop();
        }
    }

    public boolean hasNewData() {
        if (this.newData) {
            this.newData = false;
            return true;
        }
        return false;
    }

    public double getLastDataPoint() {
        if (!this.rates.isEmpty()) {
            return this.rates.getLast();
        } else {
            return 0d;
        }
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

    public NBTTagCompound getBufferNBT() {
        final NBTTagCompound data = new NBTTagCompound();
        data.setTag("rates", getRatesNBT());
        data.setTag("incomingData", getIncomingDataNBT());
        data.setInteger("tickCounter", this.tickCounter);
        return data;
    }

    private NBTTagList getRatesNBT() {
        final NBTTagList data = new NBTTagList();
        for (Double datum : this.rates) {
            data.appendTag(new NBTTagDouble(datum));
        }
        return data;
    }

    private NBTTagList getIncomingDataNBT() {
        final NBTTagList data = new NBTTagList();
        for (Double datum : this.incomingData) {
            data.appendTag(new NBTTagDouble(datum));
        }
        return data;
    }

    public void setBufferNBT(NBTTagCompound data) {
        NBTTagList nbtRates = (NBTTagList) data.getTag("rates");
        setRatesFromNBT(nbtRates);
        NBTTagList incomingData = (NBTTagList) data.getTag("incomingData");
        setIncomingDataFromNBT(incomingData);
        this.tickCounter = data.getInteger("tickCounter");
    }

    private void setRatesFromNBT(NBTTagList data) {
        for (int i = 0; i < data.tagCount(); i++) {
            this.rates.add(data.func_150309_d(i));
        }
    }

    private void setIncomingDataFromNBT(NBTTagList data) {
        for (int i = 0; i < data.tagCount(); i++) {
            this.incomingData.add(data.func_150309_d(i));
        }
    }

    public int getTickCounter() {
        return this.tickCounter;
    }

    public void setTickCounter(int tickCounter) {
        this.tickCounter = tickCounter;
    }

    public ArrayList<Double> getRates() {
        return new ArrayList<>(this.rates);
    }

    // Used to determine when to save the sum to the child buffer
    private int determineTickInterval() {
        switch (this.interval) {
            case FIVE_SECONDS -> {
                return intervalToBufferSize(TimeIntervals.FIVE_SECONDS.getSeconds()); // 20
            }
            case ONE_MINUTES -> {
                return intervalToBufferSize(TimeIntervals.ONE_MINUTES.getSeconds()); // 240
            }
            case TEN_MINUTES -> {
                return intervalToBufferSize(TimeIntervals.TEN_MINUTES.getSeconds()); // 2,400
            }
            case ONE_HOURS -> {
                return intervalToBufferSize(TimeIntervals.ONE_HOURS.getSeconds()); // 14,400
            }
            case TEN_HOURS -> {
                return intervalToBufferSize(TimeIntervals.TEN_HOURS.getSeconds()); // 144,000
            }
            case FIFTY_HOURS -> {
                return intervalToBufferSize(TimeIntervals.FIFTY_HOURS.getSeconds()); // 720,000
            }
            case TWO_FIFTY_HOURS -> {
                return intervalToBufferSize(TimeIntervals.TWO_FIFTY_HOURS.getSeconds()); // 3,600,000
            }
        }
        return 0;
    }

    private int intervalToBufferSize(int intervalSeconds) {
        return TICK_TO_SEC * (TICK_TO_SEC * intervalSeconds / this.bufferSize);
    }
}
