package harper.mapsmeproject.interfaces;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface OnMapObjectsUpdate {
    public interface Callback {
        void onUpdate(Element node);
    }
}
