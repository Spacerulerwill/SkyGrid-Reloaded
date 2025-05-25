package net.spacerulerwill.skygrid_reloaded.ui.screen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.spacerulerwill.skygrid_reloaded.Common;
import net.spacerulerwill.skygrid_reloaded.Constants;
import net.spacerulerwill.skygrid_reloaded.platform.Services;
import net.spacerulerwill.skygrid_reloaded.ui.widget.TextField;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridConfig;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridPreset;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SkyGridPresetsScreen extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 64);
    private final CustomizeSkyGridScreen parent;
    private final RegistryAccess.Frozen dynamicRegistryManager;
    private TextField textField;
    private Button selectPresetButton;
    private Button savePresetButton;
    private SkyGridPresetListWidget listWidget;

    protected SkyGridPresetsScreen(Minecraft client, CustomizeSkyGridScreen parent, RegistryAccess.Frozen dynamicRegistryManager) {
        super(Component.translatable("createWorld.customize.skygrid.presets"));
        this.parent = parent;
        this.dynamicRegistryManager = dynamicRegistryManager;
        Common.reloadCustomPresets(this.dynamicRegistryManager);
    }

    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);
        this.listWidget = this.layout.addToContents(new SkyGridPresetListWidget());
        LinearLayout rows = LinearLayout.vertical().spacing(4);
        LinearLayout row1 = LinearLayout.horizontal().spacing(8);
        this.selectPresetButton = row1.addChild(Button.builder(Component.translatable("createWorld.skygrid.customize.presets.select"), (buttonWidget) -> {
            SkyGridPresetListWidget.SkyGridPresetEntry entry = this.listWidget.getSelected();
            this.parent.setConfigFromPreset(entry.preset);
            this.parent.updateBiomeScaleSlider();
            this.minecraft.setScreen(this.parent);
        }).build());
        row1.addChild(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            this.minecraft.setScreen(this.parent);
        }).build());
        rows.addChild(row1);
        LinearLayout row2 = LinearLayout.horizontal().spacing(8);
        this.textField = row2.addChild(new PresetsTextField());
        this.savePresetButton = row2.addChild(Button.builder(Component.translatable("createWorld.skygrid.customize.presets.save"), (buttonWidget) -> {
            this.savePreset(this.textField.getValue());
        }).build());
        rows.addChild(row2);
        rows.visitWidgets(this::addRenderableWidget);
        this.layout.addToFooter(rows);
        this.layout.visitWidgets(this::addRenderableWidget);
        this.updateSelectPresetButtonActive();
        this.repositionElements();
        this.updateSaveButtonActive();
    }

    private void updateSaveButtonActive() {
        this.savePresetButton.active = !this.textField.getValue().isEmpty();
    }

    protected void repositionElements() {
        if (this.listWidget != null) {
            this.listWidget.updateSize(this.width, this.layout);
        }
        this.layout.arrangeElements();
    }

    public void updateSelectPresetButtonActive() {
        this.selectPresetButton.active = this.listWidget.getSelected() != null;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void savePreset(String name) {
        try {
            // Get hash of name
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(name.getBytes(StandardCharsets.UTF_8));

            String hashedName = Hex.encodeHexString(hash);
            // Get the preset
            SkyGridConfig currentConfig = this.parent.getCurrentSkyGridConfig();
            // Icon will be the most common block
            int maxWeight = 0;
            // Should be better
            List<Item> allItems = new ArrayList<>();
            currentConfig.overworldConfig().blocks.keySet().stream()
                    .map(Block::asItem)
                    .forEach(allItems::add);
            allItems.addAll(currentConfig.overworldConfig().chestItems.keySet());
            currentConfig.netherConfig().blocks.keySet().stream()
                    .map(Block::asItem)
                    .forEach(allItems::add);
            allItems.addAll(currentConfig.overworldConfig().chestItems.keySet());
            currentConfig.endConfig().blocks.keySet().stream()
                    .map(Block::asItem)
                    .forEach(allItems::add);
            allItems.addAll(currentConfig.overworldConfig().chestItems.keySet());
            Random random = new Random();
            Item icon = allItems.get(random.nextInt(allItems.size()));
            SkyGridPreset preset = new SkyGridPreset(icon, name, currentConfig);
            // Encode it as json
            JsonElement element = new JsonObject();
            DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, this.dynamicRegistryManager);
            DataResult<JsonElement> json = SkyGridPreset.CODEC.encode(preset, ops, element);
            String jsonString = json.getOrThrow().toString();
            // Write json to file
            String fileName = Services.PLATFORM.getConfigPath().toString() + "/" + Constants.MOD_ID + "/" + hashedName + ".json";
            try (PrintWriter writer = new PrintWriter(fileName, StandardCharsets.UTF_8)) {
                writer.write(jsonString);
            }
            // Reload necessary stuff
            Common.reloadCustomPresets(this.dynamicRegistryManager);
            this.listWidget.refreshEntries();
        } catch (Exception e) {
            Constants.LOGGER.error("Failed to save preset {}: {}", name, e);
        }
    }

    private class SkyGridPresetListWidget extends ObjectSelectionList<SkyGridPresetListWidget.SkyGridPresetEntry> {
        public SkyGridPresetListWidget() {
            super(SkyGridPresetsScreen.this.minecraft, SkyGridPresetsScreen.this.width, SkyGridPresetsScreen.this.height - 77, 33, 24);
            this.refreshEntries();
        }

        public void refreshEntries() {
            this.clearEntries();
            for (SkyGridPreset preset : Common.PRESETS) {
                this.addEntry(new SkyGridPresetEntry(preset));
            }
            for (SkyGridPreset preset : Common.CUSTOM_PRESETS) {
                this.addEntry(new SkyGridCustomPresetEntry(preset));
            }
        }

        @Override
        public void setSelected(@Nullable SkyGridPresetsScreen.SkyGridPresetListWidget.SkyGridPresetEntry entry) {
            super.setSelected(entry);
            SkyGridPresetsScreen.this.updateSelectPresetButtonActive();
        }

        public class SkyGridPresetEntry extends Entry<SkyGridPresetEntry> {
            private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.withDefaultNamespace("container/slot");
            private final SkyGridPreset preset;

            public SkyGridPresetEntry(SkyGridPreset preset) {
                this.preset = preset;
            }

            @Override
            public Component getNarration() {
                return Component.empty();
            }

            @Override
            public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.blitSprite(RenderType::guiTextured, SLOT_TEXTURE, x + 1, y + 1, 0, 18, 18);
                context.renderFakeItem(preset.item().getDefaultInstance(), x + 2, y + 2);
                context.drawString(SkyGridPresetsScreen.this.font, Component.translatable(preset.name()), x + 18 + 5, y + 3, 16777215, false);
            }
        }

        public class SkyGridCustomPresetEntry extends SkyGridPresetEntry {
            private final Button deleteButton;

            public SkyGridCustomPresetEntry(SkyGridPreset preset) {
                super(preset);
                this.deleteButton = Button.builder(Component.translatable("createWorld.skygrid.customize.presets.delete"), button -> {
                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest(preset.name().getBytes(StandardCharsets.UTF_8));
                        String hashedName = Hex.encodeHexString(hash);
                        String fileName = Services.PLATFORM.getConfigPath().toString() + "/" + Constants.MOD_ID + "/" + hashedName + ".json";
                        File file = new File(fileName);
                        Files.deleteIfExists(file.toPath());
                        Common.reloadCustomPresets(SkyGridPresetsScreen.this.dynamicRegistryManager);
                        SkyGridPresetListWidget.this.removeEntry(this);
                    } catch (Exception e) {
                        Constants.LOGGER.error("Failed to delete preset {}: {}", preset, e);
                    }
                }).width(50).build();
            }

            @Override
            public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
                this.deleteButton.setX(x + entryWidth - this.deleteButton.getWidth() - 5);
                this.deleteButton.setY(y);
                this.deleteButton.render(context, mouseX, mouseY, tickDelta);
            }

            // Allowing slider to be draggable
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (this.deleteButton.isMouseOver(mouseX, mouseY)) {
                    return this.deleteButton.mouseClicked(mouseX, mouseY, button);
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        }
    }


    protected class PresetsTextField extends TextField {
        public PresetsTextField() {
            super(SkyGridPresetsScreen.this.font, 150, 20, Component.empty());
        }

        @Override
        protected void onTextChanged() {
            SkyGridPresetsScreen.this.updateSaveButtonActive();
        }
    }

}