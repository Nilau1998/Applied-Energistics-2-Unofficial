package appeng.client.gui.widgets;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.AEBaseGui;

public class GuiGraph {

    private class Graph {

        private float currentMax = Float.NEGATIVE_INFINITY;
        protected ArrayList<Float> graphData = new ArrayList<>();
        private int color = 0;

        public Graph(final int color) {
            this.color = color;
        }

        private void drawGraph(final int offsetX, final int offsetY) {
            float f3 = (float) (color >> 24 & 255) / 255.0F;
            float f = (float) (color >> 16 & 255) / 255.0F;
            float f1 = (float) (color >> 8 & 255) / 255.0F;
            float f2 = (float) (color & 255) / 255.0F;

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glColor4f(f, f1, f2, f3);

            GL11.glBegin(GL11.GL_LINE_STRIP);

            for (int i = 0; i < graphData.size(); i++) {
                GL11.glVertex2f(
                        offsetX + i + GRAPH_LABELSPACE_X,
                        offsetY + graphHeight - getRelativeHeight(i, graphHeight) - GRAPH_LABELSPACE_Y);
            }
            GL11.glEnd();

            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        public void addData(float data) {
            if (graphData.size() == (graphWidth - GRAPH_LABELSPACE_X)) {
                graphData.remove(0);
            }
            graphData.add(data);
            for (Float i : graphData) {
                if (i > currentMax) {
                    currentMax = i;
                }
            }
        }

        private float getRelativeHeight(int index, int height) {
            if (graphData.isEmpty() || graphData.get(index) < 0.1f) {
                return 0;
            }
            return scale(Math.min(currentMax, graphData.get(index)));
        }

        // https://stats.stackexchange.com/a/281165
        private float scale(float x) {
            return ((graphHeight - GRAPH_LABELSPACE_Y) * x) / currentMax;
        }
    }

    private final AEBaseGui parent;
    private final int originX;
    private final int originY;
    private final int graphHeight;
    private final int graphWidth;
    private final HashMap<Object, Graph> graphs = new HashMap<>();
    private boolean doGridDrawing = false;
    private boolean doLabelDrawing = false;
    private final int graphNumTicksX;
    private final int graphNumTicksY;
    private final float graphTickSize;
    // Little lines going over the main axis
    private static final int GRAPH_TICK_OVERLAP = 2;
    // Decenter values, so we can push the grid a bit off for ticks/labels
    private static final int GRAPH_LABELSPACE_X = 11 + GRAPH_TICK_OVERLAP;
    private static final int GRAPH_LABELSPACE_Y = 6 + GRAPH_TICK_OVERLAP;
    private final String[] labelsX;
    private final String[] labelsY;
    private float maxYScaleValue = 0;
    private float maxXScaleValue = 0;

    /**
     * Origin shall be set from top left of whatever window/screen is being targeted.
     */
    protected GuiGraph(AEBaseGui parent, int originX, int originY, int graphWidth, int graphHeight, int numTicksX,
            int numTicksY) {
        this.parent = parent;
        this.originX = originX;
        this.originY = originY;
        this.graphWidth = graphWidth;
        this.graphHeight = graphHeight;
        this.graphNumTicksX = numTicksX;
        this.graphNumTicksY = numTicksY;
        this.graphTickSize = (float) graphWidth / graphNumTicksX;
        this.labelsX = new String[graphNumTicksX];
        this.labelsY = new String[graphNumTicksY];
    }

    public void draw(int offsetX, int offsetY, final int mouseX, final int mouseY) {
        // Normalize to origin
        offsetX += originX;
        offsetY += originY;

        if (doGridDrawing) {
            drawGrid(offsetX, offsetY);
        }
        if (doLabelDrawing) {
            drawLabels(offsetX, offsetY);
        }

        for (Graph graph : graphs.values()) {
            graph.drawGraph(offsetX, offsetY);
        }
    }

    private void drawGrid(int offsetX, int offsetY) {
        GL11.glLineWidth(0.5f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(255f / 255f, 100f / 255f, 100f / 255f, 255f / 255f);

        GL11.glBegin(GL11.GL_LINES);

        offsetX += GRAPH_LABELSPACE_X;
        offsetY -= GRAPH_LABELSPACE_Y;

        // Vertical
        for (int i = 0; i < graphWidth - graphTickSize; i += graphTickSize) {
            GL11.glVertex2f(offsetX + i, offsetY + graphTickSize - GRAPH_TICK_OVERLAP);
            GL11.glVertex2f(offsetX + i, offsetY + graphHeight + GRAPH_TICK_OVERLAP);
        }
        // Horizontal
        for (int i = graphHeight; i > 0; i -= graphTickSize) {
            GL11.glVertex2f(offsetX - GRAPH_TICK_OVERLAP, offsetY + i);
            GL11.glVertex2f(offsetX + graphWidth - graphTickSize - GRAPH_TICK_OVERLAP - 1, offsetY + i);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glLineWidth(1.5f);
    }

    private void drawLabels(int offsetX, int offsetY) {
        if (this.labelsX.length == 0) {
            return;
        }

        final int labelColor = 0x969696;
        final float scale = 0.4f;

        for (int i = 0; i < labelsX.length; i++) {
            float stringLength = parent.getFontRenderer().getStringWidth(labelsX[i]) * scale;
            float xPos = offsetX + GRAPH_LABELSPACE_X + (i * graphTickSize) - (i / 9f);
            xPos = xPos - stringLength / 2;
            float yPos = offsetY + graphHeight - GRAPH_LABELSPACE_Y / 2;
            GL11.glPushMatrix();
            GL11.glScalef(scale, scale, 0);
            this.parent.getFontRenderer()
                    .drawString(labelsX[i], Math.round(xPos / scale), Math.round(yPos / scale), labelColor, true);
            GL11.glPopMatrix();
        }

        for (int i = 0; i < labelsY.length; i++) {
            float stringLength = parent.getFontRenderer().getStringWidth(labelsY[i]) * scale;
            float xPos = offsetX + GRAPH_LABELSPACE_X - GRAPH_TICK_OVERLAP - stringLength - 1;
            float yPos = offsetY + graphHeight - 10 - (i * graphTickSize) - (i / 1.1f);
            GL11.glPushMatrix();
            GL11.glScalef(scale, scale, 0);
            parent.getFontRenderer()
                    .drawString(labelsY[i], Math.round(xPos / scale), Math.round(yPos / scale), labelColor, true);
            GL11.glPopMatrix();
        }

        // Reset color
        GL11.glColor4f(1, 1, 1, 1);
    }

    public void addGraph(Object key, int color) {
        this.graphs.put(key, new Graph(color));
    }

    public void addData(Object key, float data) {
        this.graphs.get(key).addData(data);
    }

    // Sets a new upper limit starting from 0
    public void recalculateYAxisLabels(float newMax) {
        this.maxYScaleValue = newMax;
        float stepSize = maxYScaleValue / graphNumTicksY;
        for (int i = 0; i < labelsY.length; i++) {
            this.labelsY[i] = formatLong((long) (stepSize * i));
        }
    }

    // Sets a new upper limit starting from 0
    public void recalculateXAxisLabels(float newMax) {
        this.maxXScaleValue = newMax;
        float stepSize = maxXScaleValue / graphNumTicksX;
        for (int i = 0; i < labelsX.length; i++) {
            this.labelsX[i] = formatLong((long) (stepSize * i));
        }
    }

    public void toggleGridDrawing(boolean toggle) {
        this.doGridDrawing = toggle;
    }

    public void toggleLabelDrawing(boolean toggle) {
        this.doLabelDrawing = toggle;
    }

    public float getMaxYScaleValue() {
        return this.maxYScaleValue;
    }

    public float getMaxXScaleValue() {
        return this.maxXScaleValue;
    }

    public int getWidth() {
        return this.graphWidth;
    }

    public int getHeight() {
        return this.graphHeight;
    }

    private void renderTooltip(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        // System.out.println(mouseX + " " + mouseY + "|" + offsetX + " " + offsetY);
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
}
