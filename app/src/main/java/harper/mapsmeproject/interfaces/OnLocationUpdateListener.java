package harper.mapsmeproject.interfaces;

import android.location.Location;
import org.osmdroid.util.GeoPoint;

public interface OnLocationUpdateListener {
    void onLocationUpdate(GeoPoint geoPoint);
}
