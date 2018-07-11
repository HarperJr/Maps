package harper.mapsmeproject.handle;

import android.app.Activity;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import harper.mapsmeproject.activities.MainActivity;
import harper.mapsmeproject.activities.MapFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;


public class LocationHandler {
    private static final int LOCATION_SERVICE_UPDATES_INTERVAL = 10000;

    private final MainActivity activity;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    public LocationHandler(final Activity activity) {
        this.activity = (MainActivity)activity;

        locationCallback = new LocationCallback() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        final GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        ((MainActivity) activity).onLocationUpdate(geoPoint);
                    }
                }
            }
        };
    }


    public void addLocationTask(OnSuccessListener<Location> onSuccessListener) throws SecurityException {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(activity, onSuccessListener);
    }

    public void prepareLocationService() {
        LocationSettingsRequest.Builder builder = createLocationBuilder();

        SettingsClient settingsClient = LocationServices.getSettingsClient(activity);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                activity.handleToast("Success: location response");
            }
        });

        task.addOnFailureListener(activity, new OnFailureListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onFailure(@NonNull Exception e) {
                activity.handleToast("Failure: location response");
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    private LocationSettingsRequest.Builder createLocationBuilder() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_SERVICE_UPDATES_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        return builder;
    }

    public void startLocationUpdates() throws SecurityException {
        if (fusedLocationProviderClient == null) return;
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    public void stopLocationUpdates() {
        if (fusedLocationProviderClient == null) return;
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

}
