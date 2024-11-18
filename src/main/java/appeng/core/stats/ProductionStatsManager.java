package appeng.core.stats;

import java.util.HashMap;

import appeng.api.storage.data.IAEStack;
import appeng.util.ringbuffer.RecursiveRingBufferManager;

public final class ProductionStatsManager {

    public enum TimeIntervals {
        FIVE_SECONDS,
        ONE_MINUTE,
        TEN_MINUTE,
        ONE_HOUR,
        TEN_HOUR,
        FIFTY_HOUR,
        TWO_FIFTY_HOUR
    }

    public boolean newBufferAdded = false;
    private final HashMap<IAEStack, RecursiveRingBufferManager> productionStatsDataBuffers;

    private static ProductionStatsManager INSTANCE;

    private ProductionStatsManager() {
        this.productionStatsDataBuffers = new HashMap<>();
    }

    public static ProductionStatsManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProductionStatsManager();
        }
        return INSTANCE;
    }

    public void writeData(IAEStack stack, float value) {
        if (this.productionStatsDataBuffers.get(stack) == null) {
            this.productionStatsDataBuffers.put(stack, new RecursiveRingBufferManager());
            this.newBufferAdded = true;
        }
        this.productionStatsDataBuffers.get(stack).writeToBuffer(value);
    }

    public void setProductionStatsDataBuffers(HashMap<IAEStack, RecursiveRingBufferManager> map) {
        this.productionStatsDataBuffers.putAll(map);
    }

    public HashMap<IAEStack, RecursiveRingBufferManager> getProductionStatsDataBuffers() {
        return this.productionStatsDataBuffers;
    }
}
