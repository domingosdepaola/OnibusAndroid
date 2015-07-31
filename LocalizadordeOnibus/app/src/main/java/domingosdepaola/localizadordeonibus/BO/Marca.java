package domingosdepaola.localizadordeonibus.BO;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Doo on 10/07/2015.
 */
public class Marca {
    public String Titulo;
    public  double latitude;
    public  double longitude;
    private LatLng position;
    public LatLng getLatLng(){
        return position;
    }
    public void setPosition(LatLng latLng){
        position = latLng;
    }
}
