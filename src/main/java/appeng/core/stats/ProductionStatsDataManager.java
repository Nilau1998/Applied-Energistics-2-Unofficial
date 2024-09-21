package appeng.core.stats;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import appeng.api.storage.data.IAEStack;
import appeng.core.stats.productionstats.DataBufferHandler;
import appeng.core.stats.productionstats.ConsumerUpdate;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.world.WorldEvent;

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

    private boolean worldLoaded = false;
    public boolean newBufferAdded = false;
    private final HashMap<IAEStack, DataBufferHandler> dataBuffers;
    private final List<WeakReference<ConsumerUpdate>> consumers;

    private static ProductionStatsDataManager INSTANCE;

    private ProductionStatsDataManager() {
        this.dataBuffers = new HashMap<>();
        this.consumers = new ArrayList<>();
    }

    public static ProductionStatsDataManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProductionStatsDataManager();
        }
        return INSTANCE;
    }

    public void writeData(IAEStack stack, float value) {
        if (this.dataBuffers.get(stack) == null) {
            this.dataBuffers.put(stack, new DataBufferHandler());
            this.newBufferAdded = true;
        }
        this.dataBuffers.get(stack).writeToBuffer(value);
    }

    public void setDataBuffers(HashMap<IAEStack, DataBufferHandler> map) {
        this.dataBuffers.putAll(map);
    }

    public HashMap<IAEStack, DataBufferHandler> getDataBuffers() {
        return this.dataBuffers;
    }

    public void registerConsumer(ConsumerUpdate consumer) {
        this.consumers.add(new WeakReference<>(consumer));
    }

    public void updateConsumers() {
        Iterator<WeakReference<ConsumerUpdate>> iterator = consumers.iterator();
        while (iterator.hasNext()) {
            ConsumerUpdate consumer = iterator.next().get();
            if (consumer == null) {
                iterator.remove(); // Remove garbage collected consumer
            } else {
                consumer.update();
            }
        }
    }

    @SubscribeEvent
    public void onTick(final TickEvent ev) {
        if (ev.type == TickEvent.Type.SERVER && ev.phase == TickEvent.Phase.END) {
            if (worldLoaded) {
                for (DataBufferHandler buffer : this.dataBuffers.values()) {
                    buffer.tickBuffer();
                }
                updateConsumers();
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(final WorldEvent.Load ev) {
        worldLoaded = true;
    }

    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload ev) {
        worldLoaded = false;
    }
}
