package appeng.api.networking.productionstats;

import appeng.api.networking.IGridCache;
import appeng.core.stats.ProductionStatsDataManager;

public interface IProductionStatsGrid extends IGridCache {

    /**
     * @return true if a production stats monitor is in the network ( and only 1 )
     */
    boolean isAvailable();

    ProductionStatsDataManager getDataManager();
}
