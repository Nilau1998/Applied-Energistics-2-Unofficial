package appeng.client.gui.implementations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiProductionStatsPanel;
import appeng.client.gui.widgets.GuiProductionStatsPanel.PanelSide;
import appeng.container.implementations.ContainerProductionStats;
import appeng.core.stats.ProductionStatsDataManager.TimeIntervals;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketProductionStatsInterval;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartPatternTerminalEx;
import appeng.parts.reporting.PartTerminal;

public class GuiProductionStats extends AEBaseGui {

    private final ContainerProductionStats container;
    private final GuiProductionStatsPanel leftPanel;
    private final GuiProductionStatsPanel rightPanel;
    private final Map<Integer, GuiButton> buttonMap = new HashMap<>();
    private GuiBridge originalGui;
    private GuiButton lastClickedButton;

    public GuiProductionStats(final InventoryPlayer inventoryPlayer, final Object te) {
        this(new ContainerProductionStats(inventoryPlayer, te));

        // Remember where I came from
        if (te instanceof WirelessTerminalGuiObject) {
            this.originalGui = GuiBridge.GUI_WIRELESS_TERM;
        }

        if (te instanceof PartTerminal) {
            this.originalGui = GuiBridge.GUI_ME;
        }

        if (te instanceof PartCraftingTerminal) {
            this.originalGui = GuiBridge.GUI_CRAFTING_TERMINAL;
        }

        if (te instanceof PartPatternTerminal) {
            this.originalGui = GuiBridge.GUI_PATTERN_TERMINAL;
        }

        if (te instanceof PartPatternTerminalEx) {
            this.originalGui = GuiBridge.GUI_PATTERN_TERMINAL_EX;
        }
    }

    protected GuiProductionStats(final ContainerProductionStats container) {
        super(container);
        this.container = container;
        this.leftPanel = new GuiProductionStatsPanel(this, PanelSide.PRODUCTION);
        this.rightPanel = new GuiProductionStatsPanel(this, PanelSide.CONSUMPTION);
    }

    @Override
    public void initGui() {
        recalculateScreenSize();
        super.initGui();
        leftPanel.initGui();
        rightPanel.initGui();
        for (GuiButton button : this.buttonList) {
            buttonMap.put(button.id, button);
        }
        lastClickedButton = this.buttonMap.get(0); // Init with 5s
        lastClickedButton.enabled = false;
        NetworkHandler.instance.sendToServer(new PacketProductionStatsInterval(TimeIntervals.FIVE_SECONDS));
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        leftPanel.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        leftPanel.drawBG(offsetX, offsetY, mouseX, mouseY);
        rightPanel.drawBG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        GuiButton btnClicked = this.buttonMap.get(btn.id);
        if (btnClicked == null || btnClicked == lastClickedButton) {
            return;
        }
        if (lastClickedButton != null) {
            lastClickedButton.enabled = true;
        }
        lastClickedButton = btnClicked;
        btnClicked.enabled = false;

        TimeIntervals[] intervals = { TimeIntervals.FIVE_SECONDS, TimeIntervals.ONE_MINUTES, TimeIntervals.TEN_MINUTES,
                TimeIntervals.ONE_HOURS, TimeIntervals.TEN_HOURS, TimeIntervals.FIFTY_HOURS,
                TimeIntervals.TWO_FIFTY_HOURS };

        if (btnClicked.id >= 0 && btnClicked.id < intervals.length) {
            TimeIntervals interval = intervals[btnClicked.id];
            this.leftPanel.getGraph().setTimeInterval(interval);
            this.rightPanel.getGraph().setTimeInterval(interval);
            NetworkHandler.instance.sendToServer(new PacketProductionStatsInterval(interval));
        }
    }

    public boolean hideItemPanelSlots(int tx, int ty, int tw, int th) {
        int rw = 0;
        int rh = ySize;

        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false;
        }

        int rx = guiLeft + xSize;
        int ry = guiTop;

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
        xSize = leftPanel.getWidth() + rightPanel.getWidth();
        ySize = Math.max(leftPanel.getHeight(), rightPanel.getWidth());
    }
}
