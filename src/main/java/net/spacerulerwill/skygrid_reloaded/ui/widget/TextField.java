package net.spacerulerwill.skygrid_reloaded.ui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class TextField extends EditBox {
    public TextField(Font textRenderer, int width, int height, Component text) {
        super(textRenderer, width, height, text);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean result = super.charTyped(chr, modifiers);
        this.onTextChanged();
        return result;
    }

    @Override
    public void setValue(String text) {
        super.setValue(text);
        this.onTextChanged();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        if (result && (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE)) {
            this.onTextChanged();
        }
        return result;
    }

    protected abstract void onTextChanged();
}
