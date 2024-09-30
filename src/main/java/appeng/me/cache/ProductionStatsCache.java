package appeng.me.cache;

import java.util.ArrayList;
import java.util.List;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.productionstats.IProductionStatsGrid;
import appeng.core.stats.ProductionStatsDataManager;
import appeng.tile.misc.TileProductionStatsTracker;

public class ProductionStatsCache implements IProductionStatsGrid {

    private final IGrid myGrid;
    static final int STARTUP_DELAY = 20;
    private int startupTicks = 0;

    private ProductionStatsDataManager dataManager = null;
    private List<TileProductionStatsTracker> trackingProviders = new ArrayList<>();

    public ProductionStatsCache(final IGrid g) {
        this.myGrid = g;
    }

    @Override
    public ProductionStatsDataManager getDataManager() {
        return dataManager;
    }

    @Override
    public void onUpdateTick() {
        if (startupTicks < STARTUP_DELAY) startupTicks++;
        if (dataManager != null) {
            dataManager.onTick();
        }
    }

    @Override
    public void removeNode(final IGridNode gridNode, final IGridHost machine) {
        if (machine instanceof TileProductionStatsTracker) {
            trackingProviders.remove(machine);
            if (trackingProviders.isEmpty()) {
                dataManager = null;
            }
        }
    }

    @Override
    public void addNode(final IGridNode gridNode, final IGridHost machine) {
        if (machine instanceof TileProductionStatsTracker tileTracker) {
            if (trackingProviders.isEmpty()) {
                dataManager = new ProductionStatsDataManager();
            }
            trackingProviders.add(tileTracker);
        }
    }

    @Override
    public void onSplit(final IGridStorage destinationStorage) {}

    @Override
    public void onJoin(final IGridStorage sourceStorage) {}

    @Override
    public void populateGridStorage(final IGridStorage destinationStorage) {}

    @Override
    public boolean isAvailable() {
        return startupTicks >= STARTUP_DELAY && dataManager != null;
    }
}
