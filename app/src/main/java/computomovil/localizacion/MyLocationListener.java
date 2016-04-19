package computomovil.localizacion;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by romarin on 4/12/16.
 */
public class MyLocationListener implements android.location.LocationListener {
    Activity activity;
    Double x1 = -89.644379;
    Double y1 = 21.048191;

    //X = .000122
    //Y = .00003


    public MyLocationListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isInRange(location)) {
            ((TextView) this.activity.findViewById(R.id.textViewLong)).setText("Dentro");
        } else {
            ((TextView) this.activity.findViewById(R.id.textViewLong)).setText(location.getLongitude() + " ~ " + location.getLatitude());
        }
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

    private boolean isInRange(Location location) {
        System.out.println(Math.abs(x1 - location.getLongitude()));

        return Math.abs(x1 - location.getLongitude()) <= .000232 &&
                Math.abs(y1 - location.getLatitude()) <= .00058;
    }

}
