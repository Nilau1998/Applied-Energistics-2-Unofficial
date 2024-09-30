package appeng.tile.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.util.AEColor;
import appeng.core.stats.ProductionStatsDataManager;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import io.netty.buffer.ByteBuf;

public class TileProductionStatsTracker extends AENetworkTile implements IColorableTile {

    private AEColor paintedColor = AEColor.Transparent;
    private boolean isActive = false;

    private ProductionStatsDataManager dataManager = null;

    public TileProductionStatsTracker() {
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.getProxy().setIdlePowerUsage(2.0);
    }

    @TileEvent(TileEventType.NETWORK_READ)
    public boolean readFromStream_TileProductionStatsTracker(final ByteBuf data) {
        final boolean wasActive = this.isActive;
        this.isActive = data.readBoolean();

        final AEColor oldPaintedColor = this.paintedColor;
        this.paintedColor = AEColor.values()[data.readInt()];

        return oldPaintedColor != this.paintedColor || wasActive != this.isActive;
    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    public void writeToStream_TileProductionStatsTracker(final ByteBuf data) {
        data.writeBoolean(this.getProxy().isActive());
        data.writeByte(this.paintedColor.ordinal());
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void writeToNBT_TileProductionStatsTracker(final NBTTagCompound data) {
        data.setByte("paintedColor", (byte) this.paintedColor.ordinal());
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBT_TileProductionStatsTracker(final NBTTagCompound data) {
        if (data.hasKey("paintedColor")) {
            this.paintedColor = AEColor.values()[data.getByte("paintedColor")];
        }
    }

    @MENetworkEventSubscribe
    public void bootUpdate(final MENetworkChannelsChanged changed) {
        this.markForUpdate();
    }

    @MENetworkEventSubscribe
    public void powerUpdate(final MENetworkPowerStatusChange changed) {
        this.markForUpdate();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        this.isActive = false;
    }

    @Override
    public void onReady() {
        super.onReady();
        this.isActive = true;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public boolean isPowered() {
        return this.getProxy().isActive();
    }

    @Override
    public AEColor getColor() {
        return this.paintedColor;
    }

    @Override
    public boolean recolourBlock(final ForgeDirection side, final AEColor newPaintedColor, final EntityPlayer who) {
        if (this.paintedColor == newPaintedColor) {
            return false;
        }

        this.paintedColor = newPaintedColor;
        this.markDirty();
        this.markForUpdate();
        return true;
    }
}
