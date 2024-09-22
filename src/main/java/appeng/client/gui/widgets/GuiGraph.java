package appeng.client.gui.widgets;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.AEBaseGui;
import appeng.core.stats.ProductionStatsDataManager.TimeIntervals;

public class GuiGraph {

    private class Graph {

        private double currentMax = Double.NEGATIVE_INFINITY;
        protected ArrayList<Double> graphData = new ArrayList<>();
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
                GL11.glVertex2d(
                        offsetX + i + GRAPH_LABELSPACE_X,
                        offsetY + graphHeight - getRelativeHeight(i, graphHeight) - GRAPH_LABELSPACE_Y);
            }
            GL11.glEnd();

            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        public void addData(double data) {
            if (graphData.size() == (graphWidth - GRAPH_LABELSPACE_X)) {
                graphData.remove(0);
            }
            graphData.add(data);
            for (Double i : graphData) {
                if (i > currentMax) {
                    currentMax = i;
                }
            }
        }

        private double getRelativeHeight(int index, int height) {
            if (graphData.isEmpty() || graphData.get(index) < 0.1f) {
                return 0;
            }
            return scale(Math.min(currentMax, graphData.get(index)));
        }

        // https://stats.stackexchange.com/a/281165
        private double scale(double x) {
            return ((graphHeight - GRAPH_LABELSPACE_Y) * x) / currentMax;
        }
    }

    private final AEBaseGui parent;
    private TimeIntervals timeInterval = TimeIntervals.FIVE_SECONDS;
    private final int originX;
    private final int originY;
    private final int graphHeight;
    private final int graphWidth;
    private final HashMap<Object, Graph> graphs = new HashMap<>();
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
    private double currentMax = 0;

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
        determineIntervalLabels();
        recalculateYAxisLabels(0d);
    }

    public void draw(int offsetX, int offsetY, final int mouseX, final int mouseY) {
        // Normalize to origin
        offsetX += originX;
        offsetY += originY;

        drawGrid(offsetX, offsetY);
        drawLabels(offsetX, offsetY);

        for (Graph graph : graphs.values()) {
            graph.drawGraph(offsetX, offsetY);
        }
    }

    private void drawGrid(int offsetX, int offsetY) {
        GL11.glLineWidth(0.5f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1f, 1f, 1f, 0.15f);

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
                    labelColor);

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

    public void addData(Object key, double data) {
        graphs.get(key).addData(data);
        recalculateYAxisLabels(data);
    }

    // Sets a new upper limit starting from 0
    public void recalculateYAxisLabels(double data) {
        if (data > currentMax) {
            currentMax = data;
        }
        double stepSize = data / graphNumTicksY;
        for (int i = 0; i < labelsY.length; i++) {
            labelsY[i] = formatLong(stepSize * i);
        }
    }

    public void determineIntervalLabels() {
        switch (this.timeInterval) {
            case FIVE_SECONDS:
                setXAxisLabels(TimeIntervals.FIVE_SECONDS.getSeconds());
                break;
            case ONE_MINUTES:
                setXAxisLabels(TimeIntervals.ONE_MINUTES.getSeconds());
                break;
            case TEN_MINUTES:
                setXAxisLabels(TimeIntervals.TEN_MINUTES.getSeconds());
                break;
            case ONE_HOURS:
                setXAxisLabels(TimeIntervals.ONE_HOURS.getSeconds());
                break;
            case TEN_HOURS:
                setXAxisLabels(TimeIntervals.TEN_HOURS.getSeconds());
                break;
            case FIFTY_HOURS:
                setXAxisLabels(TimeIntervals.FIFTY_HOURS.getSeconds());
                break;
            case TWO_FIFTY_HOURS:
                setXAxisLabels(TimeIntervals.TWO_FIFTY_HOURS.getSeconds());
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
        }
    }

    public String getMouseOver(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        boolean isMouseOver = mouseX >= offsetX && mouseX <= offsetX + graphWidth
                && mouseY >= offsetY
                && mouseY <= offsetY + graphHeight - GRAPH_LABELSPACE_Y;
        if (!isMouseOver) {
            return "";
        }
        int relativeX = mouseX - offsetX - GRAPH_LABELSPACE_X;
        if (relativeX >= 0 && relativeX < graphWidth - GRAPH_LABELSPACE_X) {
            int relativeY = (graphHeight + offsetY - GRAPH_LABELSPACE_Y) - mouseY;
            double yValue = (currentMax / graphHeight) * relativeY;
            double xValue = ((double) relativeX / (graphWidth - GRAPH_LABELSPACE_X)) * timeInterval.getSeconds();
            return "X: " + String.format("%.2f", xValue) + " Y: " + String.format("%.2f", yValue); // TODO: Fix x value
        }
        return "";
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

    private String formatLong(final double n) {
        double p = n;
        String level = "";
        if (p > 1000) {
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
