package appeng.core.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import appeng.api.storage.data.IAEStack;
import appeng.core.stats.productionstats.DataBufferHandler;
import net.minecraft.nbt.NBTTagCompound;

public final class ProductionStatsDataManager {

    public enum TimeIntervals {

        FIVE_SECONDS(5),
        ONE_MINUTES(60),
        TEN_MINUTES(10 * 60),
        ONE_HOURS(60 * 60),
        TEN_HOURS(10 * 60 * 60),
        FIFTY_HOURS(50 * 60 * 60),
        TWO_FIFTY_HOURS(250 * 60 * 60);

        private final int seconds;

        TimeIntervals(int seconds) {
            this.seconds = seconds;
        }

        public int getSeconds() {
            return seconds;
        }
    }

    public boolean newBufferAdded = false;
    private final HashMap<IAEStack, DataBufferHandler> dataBuffers;
    private final HashMap<UUID, TimeIntervals> playerIntervals;

    private static ProductionStatsDataManager INSTANCE;

    public ProductionStatsDataManager() {
        this.dataBuffers = new HashMap<>();
        this.playerIntervals = new HashMap<>();
    }

    public void writeData(IAEStack stack, float value) {
        if (this.dataBuffers.get(stack) == null) {
            this.dataBuffers.put(stack, new DataBufferHandler());
            this.newBufferAdded = true;
        }
        this.dataBuffers.get(stack).writeToBuffer(value);
    }

    public ArrayList<IAEStack> getLastSummedDataEntry(UUID player) {
        ArrayList<IAEStack> summedData = new ArrayList<>();
        for (IAEStack stack : this.dataBuffers.keySet()) {
            double stackSize = dataBuffers.get(stack).getBuffer(playerIntervals.get(player)).getSummedData();
            stack.setStackSize((long) stackSize);
            summedData.add(stack);
        }
        return summedData;
    }

    // Track the player's interval in case multiple players are using the same buffer
    public void setInterval(UUID player, TimeIntervals interval) {
        this.playerIntervals.put(player, interval);
    }

    public void onTick() {
        for (DataBufferHandler buffer : this.dataBuffers.values()) {
            buffer.tickBuffer();
        }
    }
}
