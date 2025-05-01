package spireCafe.abstracts;

import java.util.ArrayList;

import basemod.IUIElement;

public abstract class AbstractAttractionSettings {

    protected ArrayList<IUIElement> elements = new ArrayList<>();

    public AbstractAttractionSettings() {
    }

    public ArrayList<IUIElement> getElements(){
        return elements;
    }

    public abstract void enable();

    public abstract void disable();

    public abstract boolean isEnabled();
}
