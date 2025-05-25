package net.spacerulerwill.skygrid_reloaded.ui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class WeightSlider extends AbstractSliderButton {
    private final double minValue;
    private final double maxValue;
    private final Component text;

    public WeightSlider(int x, int y, int width, int height, Component text, double minValue, double maxValue, double initialValue) {
        super(x, y, width, height, Component.empty(), 0.0);
        this.value = (initialValue - minValue) / (maxValue - minValue);
        this.text = text;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.updateMessage();
    }

    private MutableComponent createMessage() {
        double weight = getWeight();
        String formattedValue = String.format("%.2f", weight);
        return this.text.copy()
                .append(Component.literal(": "))
                .append(Component.literal(formattedValue));
    }

    private double getWeight() {
        return this.value * (this.maxValue - this.minValue) + this.minValue;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.createMessage());
    }

    protected abstract void applyWeight(double weight);

    @Override
    protected void applyValue() {
        this.applyWeight(this.getWeight());
    }
}
