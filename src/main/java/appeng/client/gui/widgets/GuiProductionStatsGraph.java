package appeng.client.gui.widgets;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.opengl.GL11;

import appeng.api.storage.data.IAEStack;
import appeng.client.gui.implementations.GuiProductionStats;
import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;
import appeng.core.stats.ProductionStatsManager;
import appeng.core.stats.ProductionStatsManager.TimeIntervals;
import appeng.util.ringbuffer.RecursiveRingBufferManager;

public class GuiProductionStatsGraph {

    private final GuiProductionStats parent;
    private final PanelSide side;

    private static final int GRAPH_TEXTURE_WIDTH = 238;
    private static final int GRAPH_TEXTURE_HEIGHT = 111;
    private static final int GRAPH_X_ORIGIN = 8;
    private static final int GRAPH_Y_ORIGIN = 160; // No idea why it's not 102? (texture origin)
    private static final int GRAPH_HEIGHT = 95;
    private static final int GRAPH_WIDTH = 222;
    private static final int GRAPH_NUM_TICKS_X = 20;
    private static final int GRAPH_NUM_TICKS_Y = 8;
    // Decenter values , so we can push the grid a bit off for ticks/labels
    private static final int GRAPH_GRID_DECENTER_X = 20;
    private static final int GRAPH_GRID_DECENTER_Y = 2;
    private static final float GRAPH_TICK_SIZE = (float) GRAPH_WIDTH / GRAPH_NUM_TICKS_X;
    private static final int GRAPH_TICK_OVERLAP = 2; // Little lines going over the main axis
    private String[] labelsX = new String[GRAPH_NUM_TICKS_X];
    private String[] labelsY = new String[GRAPH_NUM_TICKS_Y];
    private float maxYValue = 0;
    private float bla = 0;

    private final ArrayList<GuiFloatGraph> graphs = new ArrayList<>();

    public GuiProductionStatsGraph(GuiProductionStats parent, PanelSide side) {
        this.parent = parent;
        this.side = side;

        HashMap<IAEStack, RecursiveRingBufferManager> bufferManagerMap = ProductionStatsManager.getInstance()
                .getProductionStatsDataBuffers();

        for (RecursiveRingBufferManager bufferManager : bufferManagerMap.values()) {
            graphs.add(
                    new GuiFloatGraph(
                            (int) (GRAPH_WIDTH - GRAPH_TICK_SIZE),
                            GRAPH_HEIGHT,
                            (int) (GRAPH_X_ORIGIN + GRAPH_TICK_SIZE + 1),
                            (int) (GRAPH_Y_ORIGIN),
                            bufferManager.GRAPH_COLOR));
        }
    }

    public void initGui() {
        for (int i = 0; i < this.labelsX.length; i++) {
            this.labelsX[i] = String.valueOf(i);
        }
        for (int i = 0; i < this.labelsY.length; i++) {
            this.labelsY[i] = String.valueOf(i);
        }
    }

    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        HashMap<IAEStack, RecursiveRingBufferManager> bufferManagerMap = ProductionStatsManager.getInstance()
                .getProductionStatsDataBuffers();
        int j = 0;
        for (RecursiveRingBufferManager bufferManager : bufferManagerMap.values()) {
            float val = bla; // mouseY - 1 / (1f + mouseY * j);
            if (val > this.maxYValue) {
                this.maxYValue = val;
                recalculateYAxisLabels();
            }
            graphs.get(j).addData(val);
            j += 1;
            bla += 1f;
        }
        for (GuiFloatGraph graph : graphs) {
            graph.draw(mouseX, mouseY);
        }
    }

    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        int offsetXAdjusted = this.side.equals(PanelSide.LEFT) ? offsetX : offsetX + GRAPH_TEXTURE_WIDTH;
        parent.bindTexture("guis/productionstatsgraph.png");
        parent.drawTexturedModalRect(offsetXAdjusted, offsetY, 0, 0, GRAPH_TEXTURE_WIDTH, GRAPH_TEXTURE_HEIGHT);
        drawGrid(offsetXAdjusted, offsetY);
        drawLabels(offsetXAdjusted, offsetY);
    }

    public void setXAxis(TimeIntervals interval) {
        for (int i = 0; i <= this.labelsX.length; i++) {
            switch (interval) {
                case FIVE_SECONDS: {

                }
            }
        }
    }

    private void recalculateYAxisLabels() {
        float stepSize = this.maxYValue / GRAPH_NUM_TICKS_Y;
        for (int i = 0; i < this.labelsY.length; i++) {
            this.labelsY[i] = formatLong((long) (1000 * stepSize * i));
        }
    }

    private void drawLabels(int offsetX, int offsetY) {
        if (this.labelsX.length == 0) {
            return;
        }

        final int labelColor = 0x969696;
        final float scale = 0.4f;

        for (int i = 0; i < this.labelsX.length; i++) {
            float stringLength = this.parent.getFontRenderer().getStringWidth(this.labelsX[i]) * scale;
            float xPos = offsetX + GRAPH_GRID_DECENTER_X + (i * GRAPH_TICK_SIZE) - (i / 9f);
            xPos = xPos - stringLength / 2;
            float yPos = offsetY + GRAPH_HEIGHT + 2;
            GL11.glPushMatrix();
            GL11.glScalef(scale, scale, 0);
            this.parent.getFontRenderer()
                    .drawString(this.labelsX[i], Math.round(xPos / scale), Math.round(yPos / scale), labelColor, true);
            GL11.glPopMatrix();
        }

        for (int i = 0; i < this.labelsY.length; i++) {
            float stringLength = this.parent.getFontRenderer().getStringWidth(this.labelsY[i]) * scale;
            float xPos = offsetX + GRAPH_GRID_DECENTER_X - 2 - stringLength;
            float yPos = offsetY + GRAPH_HEIGHT - 3 - (i * GRAPH_TICK_SIZE) - (i / 1.1f);
            GL11.glPushMatrix();
            GL11.glScalef(scale, scale, 0);
            this.parent.getFontRenderer()
                    .drawString(this.labelsY[i], Math.round(xPos / scale), Math.round(yPos / scale), labelColor, true);
            GL11.glPopMatrix();
        }

        // Reset color
        GL11.glColor4f(1, 1, 1, 1);
    }

    private void drawGrid(int offsetX, int offsetY) {
        GL11.glLineWidth(0.5f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(150f / 255f, 150f / 255f, 150f / 255f, 100f / 255f);

        GL11.glBegin(GL11.GL_LINES);

        int originX = offsetX + GRAPH_GRID_DECENTER_X;
        int originY = offsetY - GRAPH_GRID_DECENTER_Y;
        // Vertical
        for (int i = 0; i < GRAPH_WIDTH - 8; i += GRAPH_TICK_SIZE) {
            GL11.glVertex2f(originX + i, originY + GRAPH_TICK_SIZE - GRAPH_TICK_OVERLAP);
            GL11.glVertex2f(originX + i, originY + GRAPH_HEIGHT + GRAPH_TICK_OVERLAP);
        }
        // Horizontal
        for (int i = GRAPH_HEIGHT; i > 0; i -= GRAPH_TICK_SIZE) {
            GL11.glVertex2f(originX - GRAPH_TICK_OVERLAP, originY + i);
            GL11.glVertex2f(originX + GRAPH_WIDTH - GRAPH_TICK_SIZE, originY + i);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glLineWidth(1.5f);
    }

    private String formatLong(final long n) {
        double p = (double) n;
        String level = "";
        if (p > 1000) {
            p = ((double) n) / 100;

            final String[] preFixes = { "k", "M", "G", "T", "P", "T", "P", "E", "Z", "Y" };

            int offset = 0;
            while (p > 1000 && offset < preFixes.length) {
                p /= 1000;
                level = preFixes[offset];
                offset++;
            }
        }

        final DecimalFormat df = new DecimalFormat("#.#");
        return df.format(p) + level;
    }

    public int getWidth() {
        return GRAPH_TEXTURE_WIDTH;
    }

    public int getHeight() {
        return GRAPH_TEXTURE_HEIGHT;
    }
}
