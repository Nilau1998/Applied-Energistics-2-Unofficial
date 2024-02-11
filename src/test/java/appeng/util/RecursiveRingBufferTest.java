package appeng.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.junit.Test;

import appeng.core.stats.ProductionStatsManager.TimeIntervals;
import appeng.util.ringbuffer.RecursiveRingBuffer;
import appeng.util.ringbuffer.RecursiveRingBufferManager;

/**
 * Tests for {@link appeng.util.ringbuffer.RecursiveRingBufferManager}
 */
public final class RecursiveRingBufferTest {

    private final RecursiveRingBufferManager manager;

    public RecursiveRingBufferTest() {
        this.manager = new RecursiveRingBufferManager();
        for (int i = 0; i < 3300000; i++) {
            manager.writeToBuffer(1);
        }
    }

    @Test
    public void testRRBManger_bufferCreation() {
        RecursiveRingBuffer buffer = manager.getBuffer(TimeIntervals.FIVE_SECONDS);
        assertNotNull(buffer);

        buffer = manager.getBuffer(TimeIntervals.TEN_HOUR);
        assertNotNull(buffer);
        buffer = manager.getBuffer(TimeIntervals.FIVE_SECONDS);
        assertEquals(20, buffer.getBufferSize());
    }

    @Test
    public void testRRBManager_bufferGetter() {
        RecursiveRingBuffer buffer = manager.getBuffer(TimeIntervals.TEN_HOUR);
        assertEquals(buffer.getInterval(), TimeIntervals.TEN_HOUR);

        buffer = manager.getBuffer(TimeIntervals.TEN_MINUTE);
        assertEquals(buffer.getInterval(), TimeIntervals.TEN_MINUTE);

        buffer = manager.getBuffer(TimeIntervals.TWO_FIFTY_HOUR);
        assertEquals(buffer.getInterval(), TimeIntervals.TWO_FIFTY_HOUR);
    }

    @Test
    public void testRRBManager_binWriting() {
        RecursiveRingBuffer buffer = manager.getBuffer(TimeIntervals.TEN_HOUR);
        System.out.println("Buffer Average: " + buffer.average());
        assertTrue(buffer.average() > 0.95); // Avg should be about one
    }

    @Test
    public void testRRBManger_binWritingSpeed() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 3300000; i++) {
            manager.writeToBuffer(1);
        }
        long end = System.currentTimeMillis();
        System.out.println("Adding 3,300,000 values took " + (end - start) + "ms");
        assertTrue(end - start < 200);
    }

    @Test
    public void testRRBManager_tagListSpeed() {
        RecursiveRingBuffer buffer = manager.getBuffer(TimeIntervals.FIVE_SECONDS);
        long start = System.currentTimeMillis();
        NBTTagList list = buffer.getBuffer();
        long end = System.currentTimeMillis();
        long timing = end - start;
        System.out.println("TagList creation took: " + timing + "ms");
        assertTrue(timing < 10);

        assertEquals(20, list.tagCount());

        start = System.currentTimeMillis();
        buffer.setBuffer(list);
        end = System.currentTimeMillis();
        timing = end - start;
        System.out.println("Buffer setting took: " + timing + "ms");
        assertTrue(timing < 10);
    }

    @Test
    public void testRRBManager_bufferPacking() {
        long start = System.currentTimeMillis();
        NBTTagCompound tag = manager.packBuffers();
        long end = System.currentTimeMillis();
        long timing = end - start;
        System.out.println("Buffer packing took: " + timing + "ms");

        RecursiveRingBufferManager new_manager = new RecursiveRingBufferManager();
        start = System.currentTimeMillis();
        new_manager.unpackBuffers(tag);
        end = System.currentTimeMillis();
        timing = end - start;
        System.out.println("Buffer unpacking took: " + timing + "ms");

        assertTrue(new_manager.getBuffer(TimeIntervals.FIVE_SECONDS).average() > 0.95);
    }
}
