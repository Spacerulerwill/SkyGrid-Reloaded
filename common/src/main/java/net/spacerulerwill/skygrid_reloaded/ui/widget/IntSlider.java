package net.spacerulerwill.skygrid_reloaded.ui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class IntSlider extends AbstractSliderButton {
    private final int minValue;
    private final int maxValue;
    private final Component text;
    private final Consumer<Integer> onValueChanged;

    public IntSlider(int x, int y, int width, int height, Component text, int minValue, int maxValue, int initialValue, Consumer<Integer> onValueChanged) {
        super(x, y, width, height, Component.empty(), 0.0);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.text = text;
        this.onValueChanged = onValueChanged;

        // Clamp and set slider value
        this.value = (double) (initialValue - minValue) / (maxValue - minValue);
        this.updateMessage();
    }

    public void setValue(int value) {
        if (value < minValue || value > maxValue) {
            throw new IllegalArgumentException("Value " + value + " is out of range [" + minValue + ", " + maxValue + "]");
        }
        this.value = (double) (value - minValue) / (maxValue - minValue);
        this.onValueChanged.accept(value);
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.createMessage());
    }

    private MutableComponent createMessage() {
        int currentValue = getIntValue();
        return this.text.copy()
                .append(Component.literal(": "))
                .append(Component.literal(String.valueOf(currentValue)));
    }

    private int getIntValue() {
        return (int) Math.round(this.value * (maxValue - minValue) + minValue);
    }

    @Override
    protected void applyValue() {
        int newValue = getIntValue();
        onValueChanged.accept(newValue);
        updateMessage();
    }
}
