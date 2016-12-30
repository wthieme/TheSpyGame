package nl.whitedove.thespygame;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.List;

class LocationHelper {

    static String GetCountry(Context cxt) {

        if (!Helper.TestInternet(cxt)) {
            return cxt.getString(R.string.CountryUnknown);
        }

        if (Helper.mCurrentBestLocation == null) {
            return cxt.getString(R.string.CountryUnknown);
        }

        String country;
        Double lat = Helper.mCurrentBestLocation.getLatitude();
        Double lng = Helper.mCurrentBestLocation.getLongitude();
        Geocoder geocoder = new Geocoder(cxt);

        List<Address> list;
        try {
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            return cxt.getString(R.string.CountryUnknown);
        }

        if (list != null && list.size() > 0) {
            Address address = list.get(0);
            country = address.getCountryName();
            LocationHelper.SaveCountry(cxt, country);
        } else {
            return cxt.getString(R.string.CountryUnknown);
        }
        return country;
    }

    private static void SaveCountry(Context cxt, String country) {
        if (country == null || country.isEmpty() || country.equalsIgnoreCase(cxt.getString(R.string.CountryUnknown)))
            return;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("country", country);
        editor.apply();
    }
}
