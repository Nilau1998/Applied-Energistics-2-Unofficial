package appeng.core.stats;

import java.util.HashMap;
import java.util.UUID;

import net.minecraftforge.event.world.WorldEvent;

import appeng.api.storage.data.IAEStack;
import appeng.core.stats.productionstats.DataBufferHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

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
    private final HashMap<UUID, TimeIntervals> playerIntervals;

    private static ProductionStatsDataManager INSTANCE;

    private ProductionStatsDataManager() {
        this.dataBuffers = new HashMap<>();
        this.playerIntervals = new HashMap<>();
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

    // Track the player's interval in case multiple players are using the same buffer
    public void setInterval(UUID player, TimeIntervals interval) {
        this.playerIntervals.put(player, interval);
    }

    public TimeIntervals getInterval(UUID player) {
        return this.playerIntervals.get(player);
    }

    @SubscribeEvent
    public void onTick(final TickEvent ev) {
        if (ev.type == TickEvent.Type.SERVER && ev.phase == TickEvent.Phase.END) {
            if (worldLoaded) {
                for (DataBufferHandler buffer : this.dataBuffers.values()) {
                    buffer.tickBuffer();
                }
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
