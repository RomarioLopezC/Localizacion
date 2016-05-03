package computomovil.localizacion;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private BeaconManager beaconManager;


    public static final double DISTANCE = 50.0;
    private Location center = new Location("");

    // Coordenadas de la plaza roja
    private Double x1 = -89.644263;
    private Double y1 = 21.048234;

    public static final String TAG = "NfcDemo";

    private TextView textViewLong;
    private TextView textViewLat;
    private NfcAdapter mNfcAdapter;

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
                SharedPreferences.Editor editor = getSharedPreferences("localizacion", MODE_PRIVATE).edit();
                if (distance <= DISTANCE) {
                    textViewLat.setText("Dentro GPS");
                    editor.putBoolean("gpsInRange", true);
                } else {
                    textViewLat.setText("Fuera GPS");
                    editor.putBoolean("gpsInRange", false);
                }
                editor.apply();
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
                boolean gpsInRange = getSharedPreferences("localizacion", MODE_PRIVATE).getBoolean("gpsInRange", false);
                boolean beaconInRange = getSharedPreferences("localizacion", MODE_PRIVATE).getBoolean("beaconInRange", false);
                if (gpsInRange) {
                    if (!beaconInRange) {
                        textViewLong.setText("Dentro BEACON");
                        showBeaconNotification(
                                "BIENVENIDO AL CC1",
                                "Listo para descargar la información de la asignatura?");
                        SharedPreferences.Editor editor = getSharedPreferences("localizacion", MODE_PRIVATE).edit();
                        editor.putBoolean("beaconInRange", true);
                        editor.apply();
                    }
                } else {
                    showBeaconNotification(
                            "Dentro de GPS, Fuera de BEACON",
                            "Lleva tu móvil al BEACON.");
                }
            }

            @Override
            public void onExitedRegion(Region region) {
                showBeaconNotification("SALIDA", "Nos vemos pronto");
                SharedPreferences.Editor editor = getSharedPreferences("localizacion", MODE_PRIVATE).edit();
                editor.putBoolean("beaconInRange", false);
                editor.apply();
                textViewLong.setText("Fuera BEACON");

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


        textViewLat = ((TextView) findViewById(R.id.textViewLat));
        textViewLong = ((TextView) findViewById(R.id.textViewLong));


        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mNfcAdapter.isEnabled()) {
//            textViewLong.setText("NFC is disabled.");
        } else {
//            textViewLong.setText("NFC is enabled.");
        }

        handleIntent(getIntent());
    }


    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if ("text/plain".equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
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

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                Toast.makeText(getApplicationContext(), "NDEF No soportado", Toast.LENGTH_LONG).show();
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Toast.makeText(getApplicationContext(), "Unsupported Encoding", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            boolean gpsInRange = getSharedPreferences("localizacion", MODE_PRIVATE).getBoolean("gpsInRange", false);
            boolean beaconInRange = getSharedPreferences("localizacion", MODE_PRIVATE).getBoolean("beaconInRange", false);
            if (result != null && gpsInRange && beaconInRange) {
                FetchDataTask fetchDataTask = new FetchDataTask(textViewLong);
                fetchDataTask.execute("Start");

                Toast.makeText(getApplicationContext(), "NFC LEIDO", Toast.LENGTH_LONG).show();
//                textViewLong.setText("Read content: " + result);
            }
        }
    }
}

