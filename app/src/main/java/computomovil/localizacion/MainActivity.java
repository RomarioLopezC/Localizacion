package computomovil.localizacion;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private BeaconManager beaconManager;
    private boolean beaconInRange = false;
    private boolean gpsInRange = false;

    public static final double DISTANCE = 50.0;
    private Location center = new Location("");

    // Coordenadas de la plaza roja
    private Double x1 = -89.644263;
    private Double y1 = 21.048234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        center.setLatitude(21.048234);
        center.setLongitude(-89.644263);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double distance = center.distanceTo(location);
                if (distance <= DISTANCE) {
                    ((TextView) findViewById(R.id.textViewLong)).setText("Dentro");
                } else {
                    ((TextView) findViewById(R.id.textViewLong)).setText(location.getLongitude() + " ~ " + location.getLatitude());
                }
                gpsInRange = isInRange(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });

        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (gpsInRange) {
                    if (!beaconInRange) {
                        showBeaconNotification(
                                "BIENVENIDO AL CC1",
                                "Listo para descargar la información de la asignatura?");
                        beaconInRange = true;
                    }
                } else {
                    showBeaconNotification(
                            "Dentro de beacon, Fuera de GPS",
                            "Lleva tu móvil a la Facultad de Matemáticas.");
                }
            }

            @Override
            public void onExitedRegion(Region region) {
                showBeaconNotification("SALIDA", "Nos vemos pronto");
                beaconInRange = false;
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        63463, 21120));
            }
        });
    }

    public void showBeaconNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private boolean isInRange(Location location) {
        return Math.abs(x1 - location.getLongitude()) <= .000232 &&
                Math.abs(y1 - location.getLatitude()) <= .00058;
    }
}
