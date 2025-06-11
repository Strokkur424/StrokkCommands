package net.strokkur.testplugin.iceacream;

public enum IceCreamType {
    VANILLA,
    CHOCOLATE,
    STRAWBERRY;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
