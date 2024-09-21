package appeng.client.gui.widgets;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import appeng.core.stats.ProductionStatsManager.TimeIntervals;
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
    private TimeIntervals timeInterval = TimeIntervals.ONE_MINUTE;
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
    private static final int LABEL_SPACING = 2;
    // Decenter values, so we can push the grid a bit off for ticks/labels
    private static final int GRAPH_LABELSPACE_X = 11 + LABEL_SPACING;
    private static final int GRAPH_LABELSPACE_Y = 6 + LABEL_SPACING;
    private final String[] labelsX;
    private final String[] labelsY;
    private float maxYScaleValue = 0;
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
        this.graphTickSizeX = (float) (graphWidth - GRAPH_LABELSPACE_X) / graphNumTicksX;
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
        GL11.glColor4f(1f, 0f, 0f, 0.5f);

        offsetX += GRAPH_LABELSPACE_X;
        offsetY -= GRAPH_LABELSPACE_Y;

        GL11.glBegin(GL11.GL_LINES);
        // Vertical (left to right)
        for (float i = 0; i < graphWidth - GRAPH_LABELSPACE_X; i += graphTickSizeX) {
            GL11.glVertex2f(offsetX + i, offsetY + graphHeight); // Bottom
            GL11.glVertex2f(offsetX + i, offsetY + GRAPH_LABELSPACE_Y); // Top
        }
        // Horizontal (top to bottom)
        for (float i = graphHeight; i > 0; i -= graphTickSizeY) {
            GL11.glVertex2f(offsetX, offsetY + i); // Left
            GL11.glVertex2f(offsetX + graphWidth - GRAPH_LABELSPACE_X, offsetY + i); // Right
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

        final int labelColor = 0x969616;
        final float scale = 0.4f;

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 0f);

        // Y-Axis
        for (int i = 0; i < labelsY.length; i++) {
            float stringLength = parent.getFontRenderer().getStringWidth(labelsY[i]) * scale;
            float xPos = offsetX + GRAPH_LABELSPACE_X - LABEL_SPACING - stringLength;
            float yPos = offsetY + graphHeight - 10f - (i * graphTickSizeY);
            parent.getFontRenderer()
                    .drawString(labelsY[i], Math.round(xPos / scale), Math.round(yPos / scale), labelColor);
        }

        // X-Axis
        for (int i = 0; i < labelsX.length; i++) {
            float stringLength = parent.getFontRenderer().getStringWidth(labelsX[i]);
            float halfStringLength = stringLength / 2f;

            // Center current label
            GL11.glTranslated(-halfStringLength, 0d, 0d);

            parent.getFontRenderer().drawString(
                    labelsX[i],
                    Math.round((offsetX + GRAPH_LABELSPACE_X) / scale),
                    Math.round((offsetY + graphHeight - GRAPH_LABELSPACE_Y + 3) / scale),
                    labelColor
            );

            // Move back to start position and translate to next label
            GL11.glTranslated(halfStringLength + graphTickSizeX / scale, 0d, 0d);
        }
        GL11.glPopMatrix();

        // Reset color
        GL11.glColor4f(1f, 1f, 1f, 1f);
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

    public void determineIntervalLabels() {
        switch(this.timeInterval) {
            case FIVE_SECONDS:
                setXAxisLabels(5);
                break;
            case ONE_MINUTE:
                setXAxisLabels(60);
                break;
            case TEN_MINUTE:
                setXAxisLabels(10*60);
                break;
            case ONE_HOUR:
                setXAxisLabels(60*60);
                break;
            case TEN_HOUR:
                setXAxisLabels(10*60*60);
                break;
            case FIFTY_HOUR:
                setXAxisLabels(50*60*60);
                break;
            case TWO_FIFTY_HOUR:
                setXAxisLabels(250*60*60);
                break;
            default:
                setXAxisLabels(0);
        }
    }

    // Sets a new upper limit starting from 0
    public void setXAxisLabels(float seconds) {
        float stepSize = seconds / (graphNumTicksX - 1);
        for (int i = 0; i < labelsX.length; i++) {
            labelsX[i] = String.format("%.1f", stepSize * i);
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

    public int getWidth() {
        return graphWidth;
    }

    public int getHeight() {
        return graphHeight;
    }

    public void setTimeInterval(TimeIntervals interval) {
        this.timeInterval = interval;
        determineIntervalLabels();
    }

    private void renderTooltip(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        // System.out.println(mouseX + " " + mouseY + "|" + offsetX + " " + offsetY);
    }

    private String formatLong(final long n) {
        double p = (double) n;
        String level = "";
        if (p > 1000) {
            final String[] preFixes = {"k", "M", "G", "T", "P", "T", "P", "E", "Z", "Y"};

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
