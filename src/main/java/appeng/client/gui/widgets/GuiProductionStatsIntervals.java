package appeng.client.gui.widgets;

import net.minecraft.client.gui.GuiButton;

import appeng.client.gui.implementations.GuiProductionStats;

public class GuiProductionStatsIntervals {

    private final GuiProductionStats parent;

    public static final int INTERVALS_TEXTURE_WIDTH = 202;
    public static final int INTERVALS_TEXTURE_HEIGHT = 28;

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
        int xOffset = this.parent.getGuiLeft() + 4;
        int yOffset = this.parent.getGuiTop() + 4;

        int indent = 28;
        int buttonWidth = 27;

        this.fiveSeconds = new GuiButton(0, xOffset, yOffset, buttonWidth, 20, "5s");
        this.parent.getButtonList().add(this.fiveSeconds);
        xOffset += indent;

        this.oneMinute = new GuiButton(1, xOffset, yOffset, buttonWidth, 20, "1m");
        this.parent.getButtonList().add(this.oneMinute);
        xOffset += indent;

        this.tenMinute = new GuiButton(2, xOffset, yOffset, buttonWidth, 20, "10m");
        this.parent.getButtonList().add(this.tenMinute);
        xOffset += indent;

        this.oneHour = new GuiButton(3, xOffset, yOffset, buttonWidth, 20, "1h");
        this.parent.getButtonList().add(this.oneHour);
        xOffset += indent;

        this.tenHour = new GuiButton(4, xOffset, yOffset, buttonWidth, 20, "10h");
        this.parent.getButtonList().add(this.tenHour);
        xOffset += indent;

        this.fiftyHour = new GuiButton(5, xOffset, yOffset, buttonWidth, 20, "50h");
        this.parent.getButtonList().add(this.fiftyHour);
        xOffset += indent;

        this.twoFiftyHour = new GuiButton(6, xOffset, yOffset, buttonWidth, 20, "250h");
        this.parent.getButtonList().add(this.twoFiftyHour);
    }

    public void drawFG(final int offsetX, final int offsetY, int mouseX, int mouseY) {

    }

    public void drawBG(final int offsetX, final int offsetY) {
        parent.bindTexture("guis/productionstatsintervals.png");
        parent.drawTexturedModalRect(offsetX, offsetY, 0, 0, INTERVALS_TEXTURE_WIDTH, INTERVALS_TEXTURE_HEIGHT);
    }

    public static int getWidth() {
        return INTERVALS_TEXTURE_WIDTH;
    }

    public static int getHeight() {
        return INTERVALS_TEXTURE_HEIGHT;
    }
}
