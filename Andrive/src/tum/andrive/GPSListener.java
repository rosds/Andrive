package tum.andrive;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;

/**
 *  The GPSListener class mantains the location and speed updates during the
 *  application runtime.
 *
 */
public class GPSListener extends Activity implements LocationListener
{
    private LocationManager locationManager;
    private Activity activity;
    private long time;
    private float speed;
    private float accurary;

    GPSListener(Activity act) {
        locationManager = (LocationManager) act.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        activity = act;
    }

    @Override
    public void onLocationChanged(Location location) {
        time = location.getTime();
        if (location.hasAccuracy() && location.hasSpeed()) {
            speed = location.getSpeed() / 3.6f;
            accurary = location.getAccuracy();
        }
        String info = "Speed: " + speed + "Km/h";
        ((TextView)activity.findViewById(R.id.info_text)).setText(info);
    }

    @Override
    public void onProviderDisabled(String Provider) {
        Toast.makeText(activity.getApplicationContext(), "GPS disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String Provider) {
        Toast.makeText(activity.getApplicationContext(), "GPS enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}
