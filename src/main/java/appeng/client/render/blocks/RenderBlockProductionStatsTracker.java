package appeng.client.render.blocks;

import java.util.EnumSet;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.util.AEColor;
import appeng.block.misc.BlockProductionStatsTracker;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.misc.TileProductionStatsTracker;

public class RenderBlockProductionStatsTracker
        extends BaseBlockRender<BlockProductionStatsTracker, TileProductionStatsTracker> {

    public RenderBlockProductionStatsTracker() {
        super(false, 0);
    }

    @Override
    public void renderInventory(final BlockProductionStatsTracker block, final ItemStack is,
            final RenderBlocks renderer, final ItemRenderType type, final Object[] obj) {
        renderer.overrideBlockTexture = ExtraBlockTextures.getMissing();
        this.renderInvBlock(EnumSet.of(ForgeDirection.SOUTH), block, is, Tessellator.instance, 0x000000, renderer);

        renderer.overrideBlockTexture = ExtraBlockTextures.MEChest.getIcon();
        this.renderInvBlock(
                EnumSet.of(ForgeDirection.UP),
                block,
                is,
                Tessellator.instance,
                this.adjustBrightness(AEColor.Transparent.whiteVariant, 0.7),
                renderer);

        renderer.overrideBlockTexture = null;
        super.renderInventory(block, is, renderer, type, obj);
    }

    @Override
    public boolean renderInWorld(final BlockProductionStatsTracker imb, final IBlockAccess world, final int x,
            final int y, final int z, final RenderBlocks renderer) {
        final TileProductionStatsTracker sp = imb.getTileEntity(world, x, y, z);
        renderer.setRenderBounds(0, 0, 0, 1, 1, 1);

        final ForgeDirection up = sp.getUp();

        this.preRenderInWorld(imb, world, x, y, z, renderer);

        final boolean result = renderer.renderStandardBlock(imb, x, y, z);

        int b = world.getLightBrightnessForSkyBlocks(x + up.offsetX, y + up.offsetY, z + up.offsetZ, 0);

        if (sp.isActive()) {
            b = 15 << 20 | 15 << 4;
        }

        Tessellator.instance.setBrightness(b);
        Tessellator.instance.setColorOpaque_I(0xffffff);
        renderer.setRenderBounds(0, 0, 0, 1, 1, 1);

        Tessellator.instance.setColorOpaque_I(sp.getColor().whiteVariant);
        IIcon ico = sp.isActive() ? ExtraBlockTextures.BlockProductionStatsTrackerOn_Light.getIcon()
                : ExtraBlockTextures.MEChest.getIcon();
        this.renderFace(x, y, z, imb, ico, renderer, up);

        if (sp.isActive()) {
            Tessellator.instance.setColorOpaque_I(sp.getColor().mediumVariant);
            ico = sp.isActive() ? ExtraBlockTextures.BlockProductionStatsTrackerOn_Medium.getIcon()
                    : ExtraBlockTextures.MEChest.getIcon();
            this.renderFace(x, y, z, imb, ico, renderer, up);

            Tessellator.instance.setColorOpaque_I(sp.getColor().blackVariant);
            ico = sp.isActive() ? ExtraBlockTextures.BlockProductionStatsTrackerOn_Dark.getIcon()
                    : ExtraBlockTextures.MEChest.getIcon();
            this.renderFace(x, y, z, imb, ico, renderer, up);
        }

        renderer.overrideBlockTexture = null;
        this.postRenderInWorld(renderer);

        return result;
    }
}
