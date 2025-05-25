package net.spacerulerwill.skygrid_reloaded.ui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.dimension.LevelStem;
import net.spacerulerwill.skygrid_reloaded.ui.util.RenderUtils;
import net.spacerulerwill.skygrid_reloaded.ui.widget.TextField;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.spacerulerwill.skygrid_reloaded.ui.screen.CustomizeSkyGridScreen.DIMENSIONS;


/// A screen that will allow you to adjust dimension specific SkyGrid features via a ListWidget
public abstract class DimensionSpecificCustomizableListWidgetScreen<T extends ObjectSelectionList.Entry<T>, V> extends Screen {
    private final static Component CLEAR_TEXT = Component.translatable("createWorld.customize.skygrid.clear");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Component title;
    private final Component textFieldPlaceholder;
    private final CustomizeSkyGridScreen parent;
    private final int entryHeight;
    protected ListWidget listWidget;
    protected SearchTextField textField;
    protected ResourceKey<LevelStem> currentDimension;
    protected SkyGridConfig currentConfig;
    private Button addButton;
    private Button deleteButton;
    private CycleButton<ResourceKey<LevelStem>> dimensionsSelector;
    private Button doneButton;
    private Button cancelButton;

    public DimensionSpecificCustomizableListWidgetScreen(CustomizeSkyGridScreen parent, ResourceKey<LevelStem> initialDimension, SkyGridConfig currentConfig, Component title, Component textFieldPlaceholder, int entryHeight) {
        super(title);
        this.title = title;
        this.textFieldPlaceholder = textFieldPlaceholder;
        this.entryHeight = entryHeight;
        this.parent = parent;
        this.currentDimension = LevelStem.OVERWORLD;
        this.currentConfig = new SkyGridConfig(currentConfig);
        this.currentDimension = initialDimension;
    }

    private void initHeader() {
        this.layout.addTitleHeader(this.title, this.font);
    }

    private void initBody() {
        this.listWidget = this.layout.addToContents(new ListWidget(this.minecraft, this.width, this.height - 117, 43, this.entryHeight));
    }

    private void initFooter() {
        LinearLayout rows = LinearLayout.vertical().spacing(4);
        // Row 1 - Done, Clear, Cancel
        LinearLayout row1 = LinearLayout.horizontal().spacing(8);
        this.doneButton = row1.addChild(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.parent.updateSkyGridConfig(this.currentConfig);
            this.onClose();
        }).width(75).build());
        this.cancelButton = row1.addChild(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            this.onClose();
        }).width(75).build());
        row1.addChild(Button.builder(CLEAR_TEXT, (button -> {
            this.onClear();
            this.listWidget.clearEntries();
            this.listWidget.setScrollAmount(0.0);
            this.updateAddButtonActive();
        })).width(75).build());
        // Row 2 - Dimension selector and Delete button
        LinearLayout row2 = LinearLayout.horizontal().spacing(8);
        this.dimensionsSelector = row2.addChild(new CycleButton.Builder<ResourceKey<LevelStem>>(value -> Component.translatable(value.location().toLanguageKey()))
                .withValues(DIMENSIONS)
                .withInitialValue(this.currentDimension)
                .create(0, 0, 158, 20, Component.translatable("createWorld.customize.skygrid.dimension"), ((button, dimension) -> {
                    this.currentDimension = dimension;
                    this.regenerateListEntries();
                    this.updateAddButtonActive();
                    this.updateDeleteButtonActive();
                })));
        this.deleteButton = row2.addChild(Button.builder(Component.translatable("createWorld.customize.skygrid.delete"), (button) -> {
            T entry = this.listWidget.getSelected();
            if (entry == null) {
                return;
            }
            this.listWidget.removeEntry(entry);
            this.onDelete(entry);
            this.updateAddButtonActive();
            this.updateDeleteButtonActive();
        }).width(75).build());
        // Row 3 - Text field and Add button
        LinearLayout row3 = LinearLayout.horizontal().spacing(8);
        this.textField = row3.addChild(new SearchTextField(font, 158, 20, this.textFieldPlaceholder));
        this.addButton = row3.addChild(Button.builder(Component.translatable("createWorld.customize.skygrid.add"), (button) -> {
            Optional<V> v = this.getSelectedThing();
            v.ifPresent(this::onAdd);
            this.updateAddButtonActive();
            this.updateDeleteButtonActive();
            this.listWidget.setScrollAmount(this.listWidget.maxScrollAmount());
        }).width(75).build());
        rows.addChild(row3);
        rows.addChild(row2);
        rows.addChild(row1);
        this.layout.addToFooter(rows);
        this.layout.setFooterHeight(80);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        AtomicBoolean mouseOverAnyWidget = new AtomicBoolean(false);

        this.layout.visitWidgets(widget -> {
            if (widget.isMouseOver(mouseX, mouseY)) {
                mouseOverAnyWidget.set(true);
            }
        });

        if (!mouseOverAnyWidget.get() && !this.textField.isMouseOver(mouseX, mouseY) && (this.textField.autocompleteListWidget == null || !this.textField.autocompleteListWidget.isMouseOver(mouseX, mouseY))) {
            // remove the autocomplete widget and unfocus the text field
            if (this.textField.autocompleteListWidget != null) {
                this.removeWidget(this.textField.autocompleteListWidget);
                this.showWidgetsForAutocompleteBox();
                this.textField.autocompleteListWidget = null;
            }
            this.textField.setFocused(false);
        } else if (this.textField.isMouseOver(mouseX, mouseY) && this.textField.autocompleteListWidget == null) {
            this.textField.doAutocompleteStuff();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    protected void init() {
        // Header for title
        this.initHeader();
        this.initBody();
        this.initFooter();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.regenerateListEntries();
        this.repositionElements();
        this.updateAddButtonActive();
        this.updateDeleteButtonActive();
    }

    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.listWidget != null) {
            this.listWidget.updateSize(this.width, this.layout);
        }
        this.textField.refreshPositions();
    }

    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void regenerateListEntries() {
        this.listWidget.replaceEntries(this.getEntriesFromConfig());
        this.listWidget.setScrollAmount(0.0);
    }

    private void updateAddButtonActive() {
        Optional<V> thing = getSelectedThing();
        this.addButton.active = thing.isPresent() && this.canAdd(thing.get());
    }

    private void updateDeleteButtonActive() {
        T entry = this.listWidget.getSelected();
        this.deleteButton.active = entry != null;
    }

    private void hideWidgetsForAutocompleteBox() {
        this.removeWidget(this.doneButton);
        this.removeWidget(this.cancelButton);
        this.removeWidget(this.dimensionsSelector);
    }

    private void showWidgetsForAutocompleteBox() {
        this.addRenderableWidget(this.doneButton);
        this.addRenderableWidget(this.cancelButton);
        this.addRenderableWidget(this.dimensionsSelector);
    }

    protected abstract void onClear();

    protected abstract Optional<V> getThingFromString(String text);

    protected abstract List<AutocompleteListWidget.Entry> getAutocompleteSuggestions(String text);

    protected abstract void onAdd(V thing);

    protected abstract boolean canAdd(V thing);

    protected abstract void onDelete(T entry);

    protected abstract List<T> getEntriesFromConfig();

    private Optional<V> getSelectedThing() {
        Optional<V> thing1 = this.getThingFromString(this.textField.getValue());
        Optional<V> thing2 = Optional.empty();

        if (this.textField.autocompleteListWidget != null) {
            var entry = this.textField.autocompleteListWidget.getSelected();
            if (entry != null) {
                thing2 = this.getThingFromString(entry.valueText);
            }
        }

        return thing2.or(() -> thing1); // Prefer thing2 if present
    }

    public static class AutocompleteListWidget extends ObjectSelectionList<AutocompleteListWidget.Entry> {
        DimensionSpecificCustomizableListWidgetScreen<?, ?> parent;

        public AutocompleteListWidget(Minecraft minecraftClient, DimensionSpecificCustomizableListWidgetScreen<?, ?> parent) {
            super(minecraftClient, 158, 44, 0, 24);
            this.parent = parent;
        }

        public int addEntry(Entry entry) {
            return super.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return this.getWidth() - 16;
        }

        @Override
        protected int scrollBarX() {
            return this.getX() + this.getWidth() - 6;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            for (int i = 0; i < this.getItemCount(); i++) {
                var entry = this.getEntry(i);
                if (entry.isMouseOver(mouseX, mouseY)) {
                    return true;
                }
            }
            return super.isMouseOver(mouseX, mouseY);
        }


        @Override
        public void setSelected(@Nullable DimensionSpecificCustomizableListWidgetScreen.AutocompleteListWidget.Entry entry) {
            super.setSelected(entry);
            this.parent.updateAddButtonActive();
        }

        @Override
        public void setSelectedIndex(int index) {
            super.setSelectedIndex(index);
            this.parent.updateAddButtonActive();
        }

        public static class Entry extends ObjectSelectionList.Entry<Entry> {
            public final String valueText;
            @Nullable
            private final Item iconItem;
            private final String displayText;
            private final Font textRenderer;

            public Entry(@Nullable Item iconItem, String displayText, String valueText, Font textRenderer) {
                this.iconItem = iconItem;
                this.displayText = displayText;
                this.valueText = valueText;
                this.textRenderer = textRenderer;
            }

            @Override
            public Component getNarration() {
                return Component.empty();
            }

            @Override
            public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                if (this.iconItem == null) {
                    context.drawString(this.textRenderer, this.displayText, x + 5, y + 5, 16777215, false);
                } else {
                    RenderUtils.renderItemIcon(this.iconItem, context, x, y);
                    context.drawString(this.textRenderer, this.displayText, x + 18 + 5, y + 5, 16777215, false);
                }
            }
        }
    }

    protected class ListWidget extends ObjectSelectionList<T> {
        public ListWidget(Minecraft minecraftClient, int i, int j, int k, int l) {
            super(minecraftClient, i, j, k, l);
        }

        @Override
        public void setSelected(@Nullable T entry) {
            super.setSelected(entry);
            DimensionSpecificCustomizableListWidgetScreen.this.updateDeleteButtonActive();
        }

        public void clearEntries() {
            super.clearEntries();
        }

        public boolean removeEntry(@NotNull T entry) {
            return super.removeEntry(entry);
        }

        public int addEntry(@NotNull T entry) {
            return super.addEntry(entry);
        }

        @Override
        public void setSelectedIndex(int index) {
            super.setSelectedIndex(index);
            DimensionSpecificCustomizableListWidgetScreen.this.updateDeleteButtonActive();
        }
    }

    protected class SearchTextField extends TextField {
        @Nullable
        public AutocompleteListWidget autocompleteListWidget;

        public SearchTextField(Font textRenderer, int x, int y, Component text) {
            super(textRenderer, x, y, text);
            this.setMaxLength(1024);
        }

        private void refreshPositions() {
            if (this.autocompleteListWidget != null) {
                this.autocompleteListWidget.setX(this.getX());
                this.autocompleteListWidget.setY(this.getY() + this.getHeight() + 4);
            }
        }

        private boolean isMouseOverAutocompleteWidget(double mouseX, double mouseY) {
            if (this.autocompleteListWidget == null) {
                return false;
            } else {
                return this.autocompleteListWidget.isMouseOver(mouseX, mouseY);
            }
        }

        private void doAutocompleteStuff() {
            DimensionSpecificCustomizableListWidgetScreen.this.updateAddButtonActive();
            List<AutocompleteListWidget.Entry> autocompleteResults = DimensionSpecificCustomizableListWidgetScreen.this.getAutocompleteSuggestions(this.getValue());
            if (autocompleteResults.isEmpty()) {
                if (this.autocompleteListWidget != null) {
                    DimensionSpecificCustomizableListWidgetScreen.this.removeWidget(this.autocompleteListWidget);
                    DimensionSpecificCustomizableListWidgetScreen.this.showWidgetsForAutocompleteBox();
                    this.autocompleteListWidget = null;
                }
            } else {
                if (this.autocompleteListWidget == null) {
                    this.autocompleteListWidget = new AutocompleteListWidget(DimensionSpecificCustomizableListWidgetScreen.this.minecraft, DimensionSpecificCustomizableListWidgetScreen.this);
                    this.refreshPositions();
                    for (AutocompleteListWidget.Entry entry : autocompleteResults) {
                        this.autocompleteListWidget.addEntry(entry);
                    }
                    DimensionSpecificCustomizableListWidgetScreen.this.addRenderableWidget(this.autocompleteListWidget);
                    DimensionSpecificCustomizableListWidgetScreen.this.hideWidgetsForAutocompleteBox();
                } else {
                    this.autocompleteListWidget.replaceEntries(autocompleteResults);
                }

            }
        }

        protected void onTextChanged() {
            this.doAutocompleteStuff();
        }
    }
}
