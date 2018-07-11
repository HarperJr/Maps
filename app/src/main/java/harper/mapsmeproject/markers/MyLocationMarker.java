package harper.mapsmeproject.markers;

import android.os.Build;
import android.support.annotation.RequiresApi;
import harper.mapsmeproject.activities.MapFragment;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MyLocationMarker extends MapMarker {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public MyLocationMarker(MapFragment mapFragment, MapView mapView) {
        super(mapFragment, mapView);

        setAnchor(Marker.ANCHOR_CENTER, 1.0f);
        setIcon(mapFragment.getContext().getDrawable(org.osmdroid.library.R.drawable.person));
    }

}
