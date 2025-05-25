package net.spacerulerwill.skygrid_reloaded.ui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.List;

// A list widget of rows of clickable widgets(buttons etc.)
public class ClickableWidgetList extends ContainerObjectSelectionList<ClickableWidgetList.ListWidgetEntry> {
    private static final int ENTRY_WIDTH = 310;

    public ClickableWidgetList(Minecraft minecraftClient, List<List<AbstractWidget>> widgets, int width, int height, int headerHeight) {
        super(minecraftClient, width, height, headerHeight, 25);
        for (List<AbstractWidget> row : widgets) {
            this.addEntry(new ListWidgetEntry(row));
        }
    }

    @Override
    public int getRowWidth() {
        return ENTRY_WIDTH;
    }

    protected static class ListWidgetEntry extends Entry<ListWidgetEntry> {
        private static final int PADDING = 10;

        private final List<AbstractWidget> widgets;

        ListWidgetEntry(List<AbstractWidget> widgets) {
            this.widgets = widgets;
        }

        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (this.widgets.isEmpty()) {
                return;
            }
            int currentX = x;
            int widgetWidth = (ENTRY_WIDTH - PADDING * (this.widgets.size() - 1)) / widgets.size();
            for (AbstractWidget clickableWidget : this.widgets) {
                clickableWidget.setPosition(currentX, y);
                clickableWidget.setWidth(widgetWidth);
                clickableWidget.render(context, mouseX, mouseY, tickDelta);
                currentX += widgetWidth + PADDING;
            }
        }

        public List<? extends NarratableEntry> narratables() {
            return this.widgets;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.widgets;
        }
    }
}
