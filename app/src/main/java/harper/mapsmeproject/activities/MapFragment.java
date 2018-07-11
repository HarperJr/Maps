package harper.mapsmeproject.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import harper.mapsmeproject.R;
import harper.mapsmeproject.handle.EntityDataHandler;
import harper.mapsmeproject.handle.LocationHandler;
import harper.mapsmeproject.handle.ResourceHandler;
import harper.mapsmeproject.interfaces.OnLocationUpdateListener;
import harper.mapsmeproject.interfaces.OnMapObjectsUpdate;
import harper.mapsmeproject.markers.EntityMarker;
import harper.mapsmeproject.markers.MapMarker;
import harper.mapsmeproject.markers.MyLocationMarker;
import harper.mapsmeproject.models.MapObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.w3c.dom.*;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static harper.mapsmeproject.constants.OSMConstants.*;

public class MapFragment extends Fragment implements OnLocationUpdateListener {

    private MapView theMap;
    private MarkerInfoFragment markerInfoFragment;
    private LocationHandler locationHandler;

    private List<EntityMarker> entities = new ArrayList<>();
    private List<MapMarker> markers = new ArrayList<>();

    private SharedPreferences  sharedPreferences;
    private CompassOverlay compassOverlay;
    private MyLocationMarker myLocationMarker;

    private EntityDataHandler entityDataHandler;

    boolean firstLaunch;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstLaunch = true;
        entityDataHandler = new EntityDataHandler(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        theMap = new MapView(inflater.getContext());

        theMap.setTileSource(TileSourceFactory.MAPNIK);

        theMap.setBuiltInZoomControls(false);
        theMap.setMultiTouchControls(true);

        theMap.setMinZoomLevel(MIN_ZOOM);
        theMap.setMaxZoomLevel(MAX_ZOOM);

        theMap.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_SCROLL: {
                        if (motionEvent.getAxisValue(MotionEvent.AXIS_SCROLL) < 0.0f) {
                            theMap.getController().zoomOut();
                        } else {
                            final IGeoPoint geoPoint = theMap.getProjection()
                                    .fromPixels((int)motionEvent.getX(), (int)motionEvent.getY());
                            theMap.getController().animateTo(geoPoint);
                            theMap.getController().zoomIn();
                        }

                        return true;
                    }
                }
                return false;
            }
        });

        theMap.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN : {
                        float x = event.getX();
                        float y = event.getY();

                        final GeoPoint point = (GeoPoint) theMap.getProjection().fromPixels((int)x, (int)y);

                        if (markers.size() < 1) {
                            MapMarker marker = new MyLocationMarker(MapFragment.this, theMap);
                            if (point != null) {
                                markers.add(marker);
                                addOverlay(marker);
                            }
                        } else {
                            if (point != null) {
                                MapMarker marker = markers.get(0);
                                marker.setPosition(point);
                            }
                        }

                        break;
                    }
                }
                return theMap.performClick();
            }
        });

        final View mapContainer = inflater.inflate(R.layout.map, container, false);
        ((FrameLayout) mapContainer.findViewById(R.id.the_map)).addView(theMap);

        final MarkerInfoFragment infoFragment = (MarkerInfoFragment) getFragmentManager()
                .findFragmentByTag(getString(R.string.flex_fragment));

        if (infoFragment == null) {
            final FragmentManager fragmentManager = getFragmentManager();
            markerInfoFragment = new MarkerInfoFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.info_container, markerInfoFragment, getString(R.string.flex_fragment)).commit();
        }

        return mapContainer;
    }

    public void startLocationUpdates() {
        locationHandler = new LocationHandler(getActivity());
        locationHandler.prepareLocationService();
    }

    @Override
    @RequiresApi(Build.VERSION_CODES.M)
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initOverlays();

        theMap.getController().setZoom(7.0d);
        savePreferences(theMap);

        entityDataHandler.startUpdates(callback);
    }

    private void savePreferences(MapView map) {
        final String zoomLevel = sharedPreferences.getString(PREFS_ZOOM_LEVEL_STRING, null);

        if (zoomLevel != null) {
            map.getController().setZoom(Double.valueOf(zoomLevel));
        }

        final String orientation = sharedPreferences.getString(PREFS_ORIENTATION_STRING, null);
        if (orientation != null) {
            map.setMapOrientation(Float.valueOf(orientation), false);
        }

        final String latitudeString = sharedPreferences.getString(PREFS_LATITUDE_STRING, null);
        final String longitudeString = sharedPreferences.getString(PREFS_LONGITUDE_STRING, null);
        if (latitudeString == null || longitudeString == null) {
            final int scrollX = sharedPreferences.getInt(PREFS_SCROLL_X, 0);
            final int scrollY = sharedPreferences.getInt(PREFS_SCROLL_Y, 0);

            theMap.scrollTo(scrollX, scrollY);
        } else {
            final double latitude = Double.valueOf(latitudeString);
            final double longitude = Double.valueOf(longitudeString);

            theMap.setExpectedCenter(new GeoPoint(latitude, longitude));
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    public void initOverlays() {
        final Context context = getContext();

        compassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), theMap);
        compassOverlay.enableCompass();
        addOverlay(this.compassOverlay);

        myLocationMarker = new MyLocationMarker(this, theMap);
        addOverlay(myLocationMarker);


    }

    @Override
    public void onResume() {
        super.onResume();
        final String tileSourceName = sharedPreferences.getString(PREFS_TILE_SOURCE,
                TileSourceFactory.DEFAULT_TILE_SOURCE.name());
        try {
            final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
            theMap.setTileSource(tileSource);
        } catch (final IllegalArgumentException e) {
            theMap.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        }

        compassOverlay.setEnabled(sharedPreferences.getBoolean(PREFS_SHOW_COMPASS, true));

        theMap.onResume();
        if (locationHandler != null) {
            locationHandler.startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        final SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(PREFS_TILE_SOURCE, theMap.getTileProvider().getTileSource().name());
            edit.putString(PREFS_ORIENTATION_STRING, String.valueOf(theMap.getMapOrientation()));
            edit.putString(PREFS_LATITUDE_STRING, String.valueOf(theMap.getMapCenter().getLatitude()));
            edit.putString(PREFS_LONGITUDE_STRING, String.valueOf(theMap.getMapCenter().getLongitude()));
            edit.putString(PREFS_ZOOM_LEVEL_STRING, String.valueOf(theMap.getZoomLevelDouble()));
            edit.putBoolean(PREFS_SHOW_COMPASS, compassOverlay.isCompassEnabled());
        edit.apply();
        theMap.onPause();

        if (locationHandler != null) {
            locationHandler.stopLocationUpdates();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        theMap.onDetach();

    }

    private void addOverlay(Overlay overlay) {
        if (theMap == null) return;
        theMap.getOverlays().add(overlay);
    }

    public void setFocus(Location location, double focus) {
        if (location == null) {
            return;
        }
        theMap.getController()
                .animateTo(new GeoPoint(location.getLatitude(), location.getLongitude()));
        theMap.getController().zoomTo(focus);
    }

    public MarkerInfoFragment getInfoFragment() {
        return markerInfoFragment;
    }

    public void setFocusLand(Location location) {
        setFocus(location, MAX_ZOOM);
    }

    public MyLocationMarker getMyLocationMarker() {
        return myLocationMarker;
    }

    public MapView getTheMap() {
        return theMap;
    }


    //*A lot of hardcode here*//
    private OnMapObjectsUpdate.Callback callback = new OnMapObjectsUpdate.Callback() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onUpdate(Element node) {
            if (node == null) return;

            NodeList vehicles = node.getElementsByTagName("vehicle");
            for (int i = 0; i < vehicles.getLength(); i++) {
                Node vehicle = vehicles.item(i);
                if (vehicle != null) {
                    if (vehicle instanceof Element) {

                        Element vehProp = (Element) vehicle;

                        int id = Integer.parseInt(vehProp.getAttribute("id"));
                        double lon = Double.parseDouble(vehProp.getAttribute("x"));
                        double lat = Double.parseDouble(vehProp.getAttribute("y"));
                        double rot = Double.parseDouble(vehProp.getAttribute("angle"));

                        float speed = Float.parseFloat(vehProp.getAttribute("speed"));

                        final GeoPoint pos = new GeoPoint(lat, lon);

                        EntityMarker marker;

                        if (entities.size() > id) {
                            //update
                            marker = entities.get(id);
                            marker.onLocationUpdate(pos);
                            marker.setRotation((float)rot);
                            marker.setSpeed(speed);
                        } else {
                            //add
                            marker = new EntityMarker(MapFragment.this, theMap);
                            marker.setPosition(pos);
                            marker.setRotation((float)rot);

                            entities.add(marker);
                            addOverlay(marker);
                        }

                    }
                }
            }

        }
    };

    public List<MapMarker> getMarkers() {
        return markers;
    }

    @Override
    public void onLocationUpdate(GeoPoint geoPoint) {
        if (geoPoint != null) {
            myLocationMarker.onLocationUpdate(geoPoint);
        } if (firstLaunch) {
            theMap.getController().animateTo(geoPoint);
            firstLaunch = false;
        }
    }
}
