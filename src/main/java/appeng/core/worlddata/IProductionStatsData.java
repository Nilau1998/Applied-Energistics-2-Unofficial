package appeng.core.worlddata;

import appeng.api.storage.data.IAEStack;
import appeng.util.ringbuffer.RecursiveRingBuffer;
import appeng.util.ringbuffer.RecursiveRingBufferManager;

import java.util.HashMap;

public interface IProductionStatsData {
    void serializeBufferMap();
    void deserializeBufferMap();
}
