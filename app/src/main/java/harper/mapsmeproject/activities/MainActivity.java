package harper.mapsmeproject.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import harper.mapsmeproject.handle.LocationHandler;
import harper.mapsmeproject.R;
import harper.mapsmeproject.interfaces.ToastHandler;
import harper.mapsmeproject.interfaces.OnLocationUpdateListener;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends AppCompatActivity implements ToastHandler, OnLocationUpdateListener {

    private static final List<String> REQUIRED_PERMISSIONS = new ArrayList<>();
    private static final int REQUEST_CODE = 1;

    private MapFragment mapFragment;
    private LocationHandler locationHandler;

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                mapFragment.getTheMap().invalidate();
            } catch(NullPointerException e) {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (checkPermissions()) {
            locationHandler = new LocationHandler(this);
            locationHandler.prepareLocationService();
        }
        setDefaultLocation();

        setContentView(R.layout.activity_main);

        mapFragment = (MapFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.map_fragment));

    }

    @Override
    public void onLocationUpdate(GeoPoint geoPoint) {
        if (geoPoint != null) {
            mapFragment.onLocationUpdate(geoPoint);
            handleToast("Location update");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationHandler != null) {
            locationHandler.startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationHandler != null) {
            locationHandler.stopLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(networkReceiver);
        super.onDestroy();
    }

    @Override
    public void handleToast(String messageToast) {
        Toast.makeText(this, String.format("Main: %s", messageToast), Toast.LENGTH_SHORT).show();
    }

    private boolean checkPermissions() {
        final List<String> requestPermissions = new ArrayList<>();

        for (String required : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, required) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(required);
            }
        }

        if (!requestPermissions.isEmpty()) {
            final String[] req = new String[requestPermissions.size()];
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(req), REQUEST_CODE);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean permissionGranted = true;

        switch (requestCode) {
            case REQUEST_CODE: {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = false;
                        break;
                    }
                }

                if (!permissionGranted) {
                    handleToast("Location permissions are required for this app");
                    checkPermissions();
                }
            }
        }
    }

    private void setDefaultLocation() {
        if (mapFragment == null || locationHandler == null) return;
        locationHandler.addLocationTask(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mapFragment.setFocusLand(location);
            }
        });
    }

    static {
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        REQUIRED_PERMISSIONS.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        REQUIRED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
}
