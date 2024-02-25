package appeng.client.gui.widgets;

import java.util.Collection;

import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.opengl.GL11;

public abstract class GuiGraph<COLLECTION extends Collection<?>> {

    public final int width;
    public final int height;
    public final int originX;
    public final int originY;
    private int color = 0;
    protected COLLECTION graphData;

    protected GuiGraph(int w, int h, int ox, int oy, final int color, COLLECTION graphData) {
        this.width = w;
        this.height = h;
        this.originX = ox;
        this.originY = oy;
        this.color = color;
        this.graphData = graphData;
    }

    public void draw(final int mouseX, final int mouseY) {
        drawGraph();
        renderTooltip(originX, originY, mouseX, mouseY);
    }

    private void drawGraph() {
        float f3 = (float) (this.color >> 24 & 255) / 255.0F;
        float f = (float) (this.color >> 16 & 255) / 255.0F;
        float f1 = (float) (this.color >> 8 & 255) / 255.0F;
        float f2 = (float) (this.color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(f, f1, f2, f3);

        int size = graphData.size();
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i < size; i++) {
            float y = this.originY + this.height - getRelativeHeight(i, this.height);
            GL11.glVertex2f(this.originX + i, y);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void renderTooltip(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        // System.out.println(mouseX + " " + mouseY + "|" + offsetX + " " + offsetY);
    }

    protected abstract float getRelativeHeight(int index, int height);
}
