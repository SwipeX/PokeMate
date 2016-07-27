package dekk.pw.pokemate.util;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

/**
 * Created by chrisgregori on 26/07/2016. github: @chrisgreg
 */
public class LatLongFromLocation {

    private String GoogleApiKey = "";
    private LatLng location;
    private double latitude;
    private double longitude;
    private final String ERROR_MESSAGE = "Couldn't find specified custom location, falling back to co-ordinates";

    public LatLongFromLocation(String GoogleApiKey) {
        this.GoogleApiKey = GoogleApiKey;
    }

    public boolean parseLocation(String locationName){

        if (locationName == null || locationName.equals("")) {
            System.out.println(ERROR_MESSAGE);
            return false;
        }

        GeoApiContext context = new GeoApiContext().setApiKey(GoogleApiKey);
        try {
            GeocodingResult[] request = GeocodingApi.newRequest(context).address(locationName).await();
            location = request[0].geometry.location;
            latitude = location.lat;
            longitude = location.lng;
            System.out.println("Found custom location to be: " + request[0].formattedAddress);
            return true;
        } catch (Exception e) {
            System.out.println(ERROR_MESSAGE);
            return false;
        }
    }

    public AtomicDouble getLatitude() {
        AtomicDouble atomicLat =  new AtomicDouble();
        atomicLat.set(latitude);
        return atomicLat;
    }

    public AtomicDouble getLongitude() {
        AtomicDouble atomicLng =  new AtomicDouble();
        atomicLng.set(longitude);
        return atomicLng;
    }

}
