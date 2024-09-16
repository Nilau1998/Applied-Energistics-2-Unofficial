package appeng.util.ringbuffer;

import appeng.core.stats.ProductionStatsManager.TimeIntervals;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalDouble;

public class RecursiveRingBuffer {

    private final TimeIntervals interval;
    private final int bufferSize;
    private final Deque<Float> buffer;
    private RecursiveRingBuffer childBuffer;
    private int childPassCounter = 0;
    private float lastWrittenValue = 0;
    private boolean isNewLastValue = false;

    public RecursiveRingBuffer(List<TimeIntervals> intervals, int bufferSize) {
        this.interval = intervals.remove(0);
        this.bufferSize = bufferSize;
        this.buffer = new ArrayDeque<>();
        setupChild(intervals);
    }

    private void setupChild(List<TimeIntervals> intervals) {
        if (intervals.size() >= 1) {
            childBuffer = new RecursiveRingBuffer(intervals, this.bufferSize);
        } else {
            childBuffer = null;
        }
    }

    public void writeToBuffer(float value) {
        this.buffer.add(value);
        if (this.buffer.size() > this.bufferSize) {
            this.buffer.pop();
        }
        this.lastWrittenValue = value;
        this.isNewLastValue = true;

        if (this.childBuffer == null) {
            return;
        }

        this.childPassCounter += 1;
        if (this.childPassCounter == this.bufferSize) {
            this.childBuffer.writeToBuffer(average());
            this.childPassCounter = 0;
        }
    }

    public float average() {
        OptionalDouble avg = this.buffer.stream().mapToDouble(a -> a).average();
        return avg.isPresent() ? (float) avg.getAsDouble() : 0;
    }

    public RecursiveRingBuffer getChild() {
        return this.childBuffer;
    }

    public boolean hasChild() {
        return this.childBuffer != null;
    }

    public TimeIntervals getInterval() { return this.interval; }

    public NBTTagList getBuffer() {
        final NBTTagList values = new NBTTagList();
        Iterator<Float> iterator = this.buffer.iterator();
        for (int i = 0; i < this.bufferSize && iterator.hasNext(); i++) {
            float val = iterator.next();
            values.appendTag(new NBTTagFloat(val));
        }
        return values;
    }

    public void setBuffer(NBTTagList values) {
        for (int i = 0; i < values.tagCount(); i++) {
            this.buffer.add(values.func_150308_e(i));
        }
    }

    public int getBufferSize() {
        return this.buffer.size();
    }

    public float getLastWrittenValue() {
        if (this.isNewLastValue) {
            this.isNewLastValue = false;
            return this.lastWrittenValue;
        } else {
            return 0;
        }
    }
}