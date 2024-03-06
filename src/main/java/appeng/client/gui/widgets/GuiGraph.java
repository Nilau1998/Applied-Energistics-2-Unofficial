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
    private final float graphTickSizeX;
    private final float graphTickSizeY;
    private static final int GRAPH_TICK_OVERLAP = 2;
    // Decenter values, so we can push the grid a bit off for ticks/labels
    private static final int GRAPH_LABELSPACE_X = 11 + GRAPH_TICK_OVERLAP;
    private static final int GRAPH_LABELSPACE_Y = 6 + GRAPH_TICK_OVERLAP;
    private final String[] labelsX;
    private final String[] labelsY;
    private float maxYScaleValue = 0;
    private float maxXScaleValue = 0;
    private float maxXLabelStringWidth = 0;

    protected GuiGraph(AEBaseGui parent, int originX, int originY, int graphWidth, int graphHeight, int numTicksX,
            int numTicksY) {
        this.parent = parent;
        this.originX = originX;
        this.originY = originY;
        this.graphWidth = graphWidth;
        this.graphHeight = graphHeight;
        this.graphNumTicksX = numTicksX;
        this.graphNumTicksY = numTicksY;
        this.graphTickSizeX = (float) graphWidth / graphNumTicksX;
        this.graphTickSizeY = (float) graphHeight / graphNumTicksY;
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
        GL11.glColor4f(255f / 255f, 150f / 255f, 150f / 255f, 255f / 255f);

        offsetX += GRAPH_LABELSPACE_X;
        offsetY -= GRAPH_LABELSPACE_Y;

        GL11.glBegin(GL11.GL_LINES);
        // Vertical (left to right)
        for (int i = 0; i < graphWidth - graphTickSizeX; i += graphTickSizeX) {
            GL11.glVertex2f(offsetX + i, offsetY + graphTickSizeX - GRAPH_TICK_OVERLAP);
            GL11.glVertex2f(offsetX + i, offsetY + graphHeight + GRAPH_TICK_OVERLAP);
        }
        // Horizontal (top to bottom)
        for (int i = graphHeight; i > 0; i -= graphTickSizeY) {
            GL11.glVertex2f(offsetX - GRAPH_TICK_OVERLAP, offsetY + i);
            GL11.glVertex2f(offsetX + graphWidth - graphTickSizeY - GRAPH_TICK_OVERLAP, offsetY + i);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glLineWidth(1.5f);
    }

    private void drawLabels(int offsetX, int offsetY) {
        if (labelsX.length == 0) {
            return;
        }

        final int labelColor = 0x969696;
        final float scale = 0.4f;

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 0);
        // X-Axis
        double totalTranslation = 0;
        for (int i = 0; i < labelsX.length; i++) {
            float stringLength = parent.getFontRenderer().getStringWidth(labelsX[i]) / 2f;
            double translation = Math.floor(graphTickSizeX / scale) - Math.floor((stringLength / 2f) / scale);
            // Move first label back
            if (i == 0) {
                translation = -Math.floor((stringLength / 2f) / scale);
            }
            GL11.glTranslated(translation, 0d, 0d);
            totalTranslation += translation;
            parent.getFontRenderer().drawString(
                    labelsX[i],
                    Math.round((offsetX + GRAPH_LABELSPACE_X) / scale),
                    Math.round((offsetY + graphHeight - GRAPH_LABELSPACE_Y + 3) / scale),
                    labelColor);
            translation = Math.floor((stringLength / 2f) / scale);
            totalTranslation += translation;
            GL11.glTranslated(translation, 0d, 0d);
        }
        GL11.glTranslated(-totalTranslation, 0d, 0d);
        // Y-Axis
        for (int i = 0; i < labelsY.length; i++) {
            float stringLength = parent.getFontRenderer().getStringWidth(labelsY[i]) * scale;
            float xPos = offsetX + GRAPH_LABELSPACE_X - GRAPH_TICK_OVERLAP - stringLength - 1;
            float yPos = offsetY + graphHeight - 10 - (i * graphTickSizeY) - (i / 1.1f);
            parent.getFontRenderer()
                    .drawString(labelsY[i], Math.round(xPos / scale), Math.round(yPos / scale), labelColor);
        }
        GL11.glPopMatrix();

        // Reset color
        GL11.glColor4f(1, 1, 1, 1);
    }

    public void addGraph(Object key, int color) {
        graphs.put(key, new Graph(color));
    }

    public void addData(Object key, float data) {
        graphs.get(key).addData(data);
    }

    // Sets a new upper limit starting from 0
    public void recalculateYAxisLabels(float newMax) {
        maxYScaleValue = newMax;
        float stepSize = maxYScaleValue / graphNumTicksY;
        for (int i = 0; i < labelsY.length; i++) {
            labelsY[i] = formatLong((long) (stepSize * i));
        }
    }

    // Sets a new upper limit starting from 0
    public void recalculateXAxisLabels(float newMax) {
        maxXScaleValue = newMax;
        float stepSize = maxXScaleValue / graphNumTicksX;
        for (int i = 0; i < labelsX.length; i++) {
            labelsX[i] = formatLong((long) (stepSize * i));
            float strWidth = parent.getFontRenderer().getStringWidth(labelsX[i]);
            if (strWidth > maxXLabelStringWidth) {
                maxXLabelStringWidth = strWidth;
            }
        }
    }

    public void toggleGridDrawing(boolean toggle) {
        doGridDrawing = toggle;
    }

    public void toggleLabelDrawing(boolean toggle) {
        doLabelDrawing = toggle;
    }

    public float getMaxYScaleValue() {
        return maxYScaleValue;
    }

    public float getMaxXScaleValue() {
        return maxXScaleValue;
    }

    public int getWidth() {
        return graphWidth;
    }

    public int getHeight() {
        return graphHeight;
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
