package appeng.client.gui.widgets;

import net.minecraft.client.gui.GuiButton;

import appeng.client.gui.implementations.GuiProductionStats;

public class GuiProductionStatsIntervals {

    private final GuiProductionStats parent;

    public static final int INTERVALS_TEXTURE_WIDTH = 202;
    public static final int INTERVALS_TEXTURE_HEIGHT = 28;
    private static final int INTERVALS_OFFSET_X = 0;
    private static final int INTERVALS_OFFSET_Y = 126;

    private GuiButton fiveSeconds;
    private GuiButton oneMinute;
    private GuiButton tenMinute;
    private GuiButton oneHour;
    private GuiButton tenHour;
    private GuiButton fiftyHour;
    private GuiButton twoFiftyHour;

    public GuiProductionStatsIntervals(GuiProductionStats parent) {
        this.parent = parent;
    }

    public void initGui() {
        int xOffset = this.parent.getGuiLeft() + INTERVALS_OFFSET_X + 4;
        int yOffset = this.parent.getGuiTop() + INTERVALS_OFFSET_Y + 4;

        int indent = 28;
        int buttonWidth = 27;

        this.fiveSeconds = new GuiButton(0, xOffset, yOffset, buttonWidth, 20, "5s");
        this.parent.getButtonList().add(this.fiveSeconds);
        xOffset += indent;

        this.oneMinute = new GuiButton(0, xOffset, yOffset, buttonWidth, 20, "1m");
        this.parent.getButtonList().add(this.oneMinute);
        xOffset += indent;

        this.tenMinute = new GuiButton(0, xOffset, yOffset, buttonWidth, 20, "10m");
        this.parent.getButtonList().add(this.tenMinute);
        xOffset += indent;

        this.oneHour = new GuiButton(0, xOffset, yOffset, buttonWidth, 20, "1h");
        this.parent.getButtonList().add(this.oneHour);
        xOffset += indent;

        this.tenHour = new GuiButton(0, xOffset, yOffset, buttonWidth, 20, "10h");
        this.parent.getButtonList().add(this.tenHour);
        xOffset += indent;

        this.fiftyHour = new GuiButton(0, xOffset, yOffset, buttonWidth, 20, "50h");
        this.parent.getButtonList().add(this.fiftyHour);
        xOffset += indent;

        this.twoFiftyHour = new GuiButton(0, xOffset, yOffset, buttonWidth, 20, "250h");
        this.parent.getButtonList().add(this.twoFiftyHour);
    }

    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {

    }

    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {

        int offsetAdjustedX = offsetX + INTERVALS_OFFSET_X;
        int offsetAdjustedY = offsetY + INTERVALS_OFFSET_Y;

        parent.bindTexture("guis/productionstatsintervals.png");
        parent.drawTexturedModalRect(
                offsetAdjustedX,
                offsetAdjustedY,
                0,
                0,
                INTERVALS_TEXTURE_WIDTH,
                INTERVALS_TEXTURE_HEIGHT);
    }

    public int getWidth() {
        return INTERVALS_TEXTURE_WIDTH;
    }

    public int getHeight() {
        return INTERVALS_TEXTURE_HEIGHT;
    }
}