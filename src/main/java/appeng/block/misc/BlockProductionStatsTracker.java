package appeng.block.misc;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.block.AEBaseTileBlock;
import appeng.client.render.blocks.RenderBlockProductionStatsTracker;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileProductionStatsTracker;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockProductionStatsTracker extends AEBaseTileBlock {

    public BlockProductionStatsTracker() {
        super(Material.iron);

        this.setTileEntity(TileProductionStatsTracker.class);
        this.setFeature(EnumSet.of(AEFeature.ProductionStats));
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected RenderBlockProductionStatsTracker getRenderer() {
        return new RenderBlockProductionStatsTracker();
    }

    @Override
    public boolean onActivated(final World w, final int x, final int y, final int z, final EntityPlayer p,
            final int side, final float hitX, final float hitY, final float hitZ) {
        if (p.isSneaking()) {
            return false;
        }

        final TileProductionStatsTracker tg = this.getTileEntity(w, x, y, z);
        if (tg != null) {
            if (Platform.isClient()) {
                return true;
            }

            Platform.openGUI(p, tg, ForgeDirection.getOrientation(side), GuiBridge.GUI_PRODUCTION_STATS_TRACKER);
            return true;
        }
        return false;
    }
}
