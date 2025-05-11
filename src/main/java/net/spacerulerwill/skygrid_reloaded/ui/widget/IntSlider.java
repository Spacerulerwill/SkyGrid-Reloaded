package net.spacerulerwill.skygrid_reloaded.ui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class IntSlider extends SliderWidget {
    private final int minValue;
    private final int maxValue;
    private final Text text;
    private final Consumer<Integer> onValueChanged;

    public IntSlider(int x, int y, int width, int height, Text text, int minValue, int maxValue, int initialValue, Consumer<Integer> onValueChanged) {
        super(x, y, width, height, Text.empty(), 0.0);
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

    private MutableText createMessage() {
        int currentValue = getIntValue();
        return this.text.copy()
                .append(Text.literal(": "))
                .append(Text.literal(String.valueOf(currentValue)));
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
