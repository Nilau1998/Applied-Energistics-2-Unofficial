package appeng.client.gui.widgets;

import java.util.Collection;
import java.util.Random;

import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.AEBaseGui;

public abstract class GuiGraph<COLLECTION extends Collection<?>> {

    private final int COLOR = generateColor();

    public final int width;
    public final int height;
    private final AEBaseGui graphHost;
    protected final COLLECTION graphData;

    protected GuiGraph(AEBaseGui graphHost, int w, int h, COLLECTION graphData) {
        this.graphHost = graphHost;
        this.width = w;
        this.height = h;
        this.graphData = graphData;
    }

    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        drawGraph(offsetX, offsetY);
        renderTooltip(offsetX, offsetY, mouseX, mouseY);
    }

    private void drawGraph(final int offsetX, final int offsetY) {
        float f3 = (float) (this.COLOR >> 24 & 255) / 255.0F;
        float f = (float) (this.COLOR >> 16 & 255) / 255.0F;
        float f1 = (float) (this.COLOR >> 8 & 255) / 255.0F;
        float f2 = (float) (this.COLOR & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(f, f1, f2, f3);

        int size = graphData.size();
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i < size; i++) {
            GL11.glVertex2f(offsetX + i, offsetY + this.height - getRelativeHeight(i, this.height));
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private int verticalBlimpStartY(int offsetY, int height, int relativeHeight, int deltaRelativeHeight) {
        return offsetY + height - relativeHeight;
    }

    private int verticalBlimpEndY(int offsetY, int height, int relativeHeight, int deltaRelativeHeight) {
        if (deltaRelativeHeight > relativeHeight) {
            return offsetY + height - relativeHeight;
        }
        return offsetY + height - relativeHeight + deltaRelativeHeight;
    }

    private int generateColor() {
        Random rand = new Random();
        return getColorDecimal(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255);
    }

    public static int getColorDecimal(int red, int green, int blue, int alpha) {
        int rgb = (alpha << 24);
        rgb = rgb | (red << 16);
        rgb = rgb | (green << 8);
        rgb = rgb | (blue);
        return rgb;
    }

    private void renderTooltip(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        // int hoverIndex = offsetX + mouseX;
        // if (hoverIndex >= 0 && hoverIndex < graphData.size()) {
        // this.graphHost.drawTooltip(0, 0, 0, "Hey!");
        // }
    }

    protected abstract int getRelativeHeight(int index, int height);
}