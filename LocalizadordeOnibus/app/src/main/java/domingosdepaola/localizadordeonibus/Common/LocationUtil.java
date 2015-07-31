package domingosdepaola.localizadordeonibus.Common;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.util.List;
import java.util.Locale;

/**
 * Created by Doo on 12/07/2015.
 */
public class LocationUtil {

    private Context context;

    public LocationUtil(Context context) {

        this.context = context;
    }

    public String getAddress(Double latitude, Double longitude) {

        String returnAddress = null;

        try {

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this.context, Locale.getDefault());

            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses.size() > 0) {

                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();

                returnAddress = address;

                returnAddress += " - " + city + "/" + state;
            }
        }
        catch (Exception e) {


        }

        return returnAddress;
    }
}

