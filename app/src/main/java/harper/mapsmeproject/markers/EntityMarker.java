package harper.mapsmeproject.markers;

import android.graphics.Canvas;
import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;
import harper.mapsmeproject.activities.MapFragment;
import harper.mapsmeproject.activities.MarkerInfoFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class EntityMarker extends MapMarker {

    private final MarkerInfoFragment markerInfoFragment;

    private GeoPoint destination;
    private float speed;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public EntityMarker(MapFragment mapFragment, MapView mapView) {
        super(mapFragment, mapView);
        markerInfoFragment = mapFragment.getInfoFragment();

        setIcon(mapFragment.getContext().getDrawable(org.osmdroid.library.R.drawable.direction_arrow));
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);
        tick();
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void tick() {
        if (destination != null || !destination.equals(getPosition())) {
            double curLat = getPosition().getLatitude();
            double curLon = getPosition().getLongitude();

            double nextLat = (destination.getLatitude() - getPosition().getLatitude()) * 0.001d * speed;
            double nextLon = (destination.getLongitude() - getPosition().getLongitude()) * 0.001d * speed;

            final GeoPoint nextPoint = new GeoPoint(curLat + nextLat, curLon + nextLon);
            setPosition(nextPoint);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        markerInfoFragment.showInfo();
        return true;
    }

    @Override
    public void onLocationUpdate(GeoPoint geoPoint) {
        if (geoPoint != null) {
            destination = geoPoint;
        }

        final List<MapMarker> markers = mapFragment.getMarkers();

        GeoPoint centerPos;
        float distance = 0f;

        if (markers.size() > 0) {
            centerPos = markers.get(0).getPosition();
        } else {
            centerPos = mapFragment.getMyLocationMarker().getPosition();
        }

        if (centerPos != null) {
            double dlat = getPosition().getLatitude() - centerPos.getLatitude();
            double dlon = getPosition().getLongitude() - centerPos.getLongitude();

            distance = (float) Math.sqrt(dlat * dlat + dlon * dlon);
        }
    }
}
