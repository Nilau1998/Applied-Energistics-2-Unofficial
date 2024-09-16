package appeng.client.gui.implementations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiProductionStatsIntervals;
import appeng.client.gui.widgets.GuiProductionStatsPanel;
import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;
import appeng.container.implementations.ContainerProductionStats;
import appeng.core.sync.GuiBridge;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartPatternTerminalEx;
import appeng.parts.reporting.PartTerminal;

public class GuiProductionStats extends AEBaseGui {

    private final ContainerProductionStats container;
    private final GuiProductionStatsPanel leftPanel;
    private final GuiProductionStatsPanel rightPanel;
    private final GuiProductionStatsIntervals intervals;
    private final Map<String, GuiButton> buttonMap = new HashMap<>();
    private GuiBridge OriginalGui;

    public GuiProductionStats(final InventoryPlayer inventoryPlayer, final Object te) {
        this(new ContainerProductionStats(inventoryPlayer, te));

        // Remember where I came from
        if (te instanceof WirelessTerminalGuiObject) {
            this.OriginalGui = GuiBridge.GUI_WIRELESS_TERM;
        }

        if (te instanceof PartTerminal) {
            this.OriginalGui = GuiBridge.GUI_ME;
        }

        if (te instanceof PartCraftingTerminal) {
            this.OriginalGui = GuiBridge.GUI_CRAFTING_TERMINAL;
        }

        if (te instanceof PartPatternTerminal) {
            this.OriginalGui = GuiBridge.GUI_PATTERN_TERMINAL;
        }

        if (te instanceof PartPatternTerminalEx) {
            this.OriginalGui = GuiBridge.GUI_PATTERN_TERMINAL_EX;
        }
    }

    protected GuiProductionStats(final ContainerProductionStats container) {
        super(container);
        this.container = container;
        this.leftPanel = new GuiProductionStatsPanel(this, PanelSide.LEFT);
        this.rightPanel = new GuiProductionStatsPanel(this, PanelSide.RIGHT);
        this.intervals = new GuiProductionStatsIntervals(this);
    }

    @Override
    public void initGui() {
        recalculateScreenSize();
        super.initGui();
        this.leftPanel.initGui();
        this.rightPanel.initGui();
        this.intervals.initGui();
        for (GuiButton button : this.buttonList) {
            this.buttonMap.put(button.displayString, button);
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.leftPanel.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.leftPanel.drawBG(offsetX, offsetY, mouseX, mouseY);
        this.rightPanel.drawBG(offsetX, offsetY, mouseX, mouseY);
        this.intervals.drawBG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        GuiButton btnClicked = this.buttonMap.get(btn.displayString);
        if (btnClicked == null) {
            return;
        }
        switch (btnClicked.displayString) {
            case "5s": {
                System.out.println(btnClicked.displayString);
                break;
            }
            case "1m": {
                System.out.println(btnClicked.displayString);
                break;
            }
            case "10m": {
                System.out.println(btnClicked.displayString);
                break;
            }
            case "1h": {
                System.out.println(btnClicked.displayString);
                break;
            }
            case "10h": {
                System.out.println(btnClicked.displayString);
                break;
            }
            case "50h": {
                System.out.println(btnClicked.displayString);
                break;
            }
            case "250h": {
                System.out.println(btnClicked.displayString);
                break;
            }
        }
    }

    public boolean hideItemPanelSlots(int tx, int ty, int tw, int th) {
        int rw = 0;
        int rh = this.ySize;

        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false;
        }

        int rx = this.guiLeft + this.xSize;
        int ry = this.guiTop;

        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;

        return (rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) && (th < ty || th > ry);
    }

    public List<GuiButton> getButtonList() {
        return this.buttonList;
    }

    protected void recalculateScreenSize() {
        this.xSize = this.leftPanel.widgetX + this.rightPanel.widgetX;
        this.ySize = this.leftPanel.widgetY + this.rightPanel.widgetY + this.intervals.getHeight();
    }
}