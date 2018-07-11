package harper.mapsmeproject.markers;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;
import harper.mapsmeproject.activities.MapFragment;
import harper.mapsmeproject.interfaces.OnLocationUpdateListener;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapMarker extends Marker implements OnLocationUpdateListener, Marker.OnMarkerClickListener {

    protected final MapFragment mapFragment;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public MapMarker(MapFragment mapFragment, MapView mapView) {
        super(mapView);
        this.mapFragment = mapFragment;

        setAnchor(ANCHOR_CENTER, 1.0f);
        setOnMarkerClickListener(this);
    }

    @Override
    public void onLocationUpdate(GeoPoint geoPoint) {
        if (geoPoint != getPosition()) {
            setPosition(geoPoint);
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        return false;
    }
}
