package domingosdepaola.localizadordeonibus;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.*;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import domingosdepaola.localizadordeonibus.BO.Onibus;
import domingosdepaola.localizadordeonibus.Client.AsyncResponse;
import domingosdepaola.localizadordeonibus.Client.OnibusClient;
import domingosdepaola.localizadordeonibus.Client.TaskExecute;
import domingosdepaola.localizadordeonibus.Common.DateUtil;
import domingosdepaola.localizadordeonibus.Common.LocationUtil;


public class OnibusActivity extends FragmentActivity implements AsyncResponse {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    TaskExecute asyncTask = new TaskExecute(this);
    List<Marker> listMarkerCenter = new ArrayList<Marker>();
    private LatLng myPosition;
    private MapReady mapReady;
    private int countResumed;
    AlertDialog.Builder builderAguarde;
    private AlertDialog alertAguarde;

    private Map<Marker, Object> markers = new HashMap<>();
    Location myLocation;
    LocationUtil locationUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        asyncTask.delegate = this;
        setContentView(R.layout.activity_onibus);
        setUpMapIfNeeded();
        if (mMap != null) {
            configMap();
            locationUtil = new LocationUtil(this);
            final Button button = (Button) findViewById(R.id.btnBuscar);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mMap.clear();
                    setUpMap();
                    EditText txt = (EditText) findViewById(R.id.txtNumero);
                    String numeroLinha = txt.getText().toString();
                    try {
                        enableButton(false);
                        ExibeAguarde();

                        double latitudeAtual = 0;
                        double longitudeAtual = 0;
                        try{
                        latitudeAtual=  myLocation.getLatitude();
                        longitudeAtual = myLocation.getLongitude();}
                        catch (Exception ex)
                        {

                        }
                        asyncTask.execute(numeroLinha, latitudeAtual, longitudeAtual);

                    } catch (Exception ex) {
                        enableButton(true);
                        FechaAguarde();
                        String x = ex.getMessage();
                    }
                }
            });
            final Button buttonCentro = (Button) findViewById(R.id.btnCentralizar);
            buttonCentro.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setUpMap();
                }
            });
        } else {
            ExibeAlerta("ERRO", "Não foi possivel exibir o mapa, verifique se o google play services está instalado.", true);
        }
    }

    public Map<Marker, Object> getMarkers() {

        return markers;
    }

    public interface MapReady {

        void onMapReady();
    }

    public void configMap() {

        GoogleMapOptions op = new GoogleMapOptions();
        this.mMap.getUiSettings().setZoomControlsEnabled(true);
        this.mMap.getUiSettings().setMyLocationButtonEnabled(true);
        this.mMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void posCenterMap() {

    }

    public void centerMap() {


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds bounds = null;
        CameraUpdate cameraUpdate = null;

        if (this.listMarkerCenter.size() > 0) {

            for (Marker markerAtual : this.listMarkerCenter) {

                builder.include(markerAtual.getPosition());

            }

            try {

                bounds = builder.build();
                cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,400,600,10);
                mMap.animateCamera(cameraUpdate);

            } catch (IllegalStateException e) {

                cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 480, 480, 0);
                mMap.animateCamera(cameraUpdate);
            }

        } else {

            //if (this.markerViatura != null) {
            // cameraUpdate = CameraUpdateFactory.newLatLngZoom(this.markerViatura.getPosition(), 15);
            //  super.getMap().animateCamera(cameraUpdate);

            //}

        }
        boolean localizado = false;
        double latitudeAtual = 0;
        double longitudeAtual = 0;
        //try{
            //latitudeAtual=  myLocation.getLatitude();
            //longitudeAtual = myLocation.getLongitude();
            //localizado = true;
        //}
        //catch (Exception ex)
        //{
            //ExibeAlerta("Alerta","Nao foi possivel acessar o GPS, os onibus serão exibidos mas sua localização não. Ative o GPS se desejar",true);
        //}
        if(localizado) {
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (Exception ex) {
                    }
                    asyncTask.execute("Retorno");
                    //long endTime = System.currentTimeMillis()
                    //  + 20*1000;

                    // while (System.currentTimeMillis() < endTime) {
                    // synchronized (this) {
                    // try {
                    //    wait(endTime -
                    ///        System.currentTimeMillis());
                    //} catch (Exception e) {}
                    // }
                    //}
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();
        }
    }


    public void onMarkerClickListener() {

        this.mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            boolean autoCentralize = true;

            @Override
            public boolean onMarkerClick(final Marker marker) {

                mMap.stopAnimation();

                if (markers.get(marker) instanceof Onibus) {

                    moveCameraParaDialogo(0, -170, marker, new GoogleMap.CancelableCallback() {

                        @Override
                        public void onFinish() {

                            Onibus onibus = (Onibus) markers.get(marker);
                            String formattedDate = getFormatedDate(onibus.DataHora);
                            onibus.Endereco = locationUtil.getAddress(onibus.Latitude, onibus.Longitude);
                            ExibeAlerta(onibus.Linha + "- nº " + onibus.Ordem + " - hora:" + formattedDate, "Localização: " + (onibus.Endereco != null ? onibus.Endereco : ""), false);
                            // if (getOcorrenciaPinClicked() != null)
                            //getOcorrenciaPinClicked().onOcorrenciaPinCliecked(marker);
                        }

                        public void onCancel() {
                        }

                    });
                }
                return true;

            }
            //return true;
        });

        this.configMap();
    }
    private String getFormatedDate(String dataRetornada){
        String formattedDate = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date dataOnibus = format.parse(dataRetornada.replace('T',' '));
            formattedDate = DateUtil.format(dataOnibus, "HH:mm:ss");
        }catch (Exception ex){

        }
        return  formattedDate;
    }
    public void moveCameraParaDialogo(float xPixel, float yPixel, Marker marker, GoogleMap.CancelableCallback cancelableCallback) {

        this.mMap.setInfoWindowAdapter(null);
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));
        this.mMap.animateCamera(CameraUpdateFactory.scrollBy(xPixel, yPixel));
        this.mMap.animateCamera(CameraUpdateFactory.scrollBy(xPixel, yPixel), 500, cancelableCallback);

    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                this.onMarkerClickListener();
            }
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private AlertDialog alerta;

    private void ExibeAlerta(String titulo, String texto, final Boolean desculpe) { //Cria o gerador do AlertDialog
        FechaAguarde();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //define o titulo
        builder.setTitle(titulo);
        //define a mensagem
        builder.setMessage(texto);
        //define um botão como positivo
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Toast.makeText(OnibusActivity.this, desculpe ? "Desculpe" : "Obrigado", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        //builder.setNegativeButton("Negativo", new DialogInterface.OnClickListener() {
        //    public void onClick(DialogInterface arg0, int arg1) {
        //        Toast.makeText(GoogleMapActivityBus.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
        //    }
        //});
        alerta = builder.create();
        alerta.show();
    }

    private void ExibeAguarde() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //define o titulo
        builder.setTitle("Aguarde");
        //define a mensagem
        builder.setMessage("Aguarde processando...");

        this.alertAguarde = builder.create();
        this.alertAguarde.show();
    }

    private void FechaAguarde() {
        if (this.alertAguarde != null) {
            this.alertAguarde.cancel();
        }
    }
    private void setUpMap() {
        // Get LocationManager object from System Service LOCATION_SERVICE
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker").snippet("Snippet"));

        try {
            // Enable MyLocation Layer of Google Map
            mMap.setMyLocationEnabled(true);

            // Get LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // Create a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Get the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            myLocation = locationManager.getLastKnownLocation(provider);

           // boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            //boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            //if(isGPSEnabled){
             //   myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //}else{
             //   myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            //}
            // Get Current Location
            //

            // set map type
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            // Get latitude of the current location
            double latitude = myLocation.getLatitude();

            // Get longitude of the current location
            double longitude = myLocation.getLongitude();

            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);

            // Show the current location in Google Map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Zoom in the Google Map
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            myPosition = new LatLng(latitude, longitude);// = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Voce está aqui").snippet("Sua localização"));
        } catch (Exception ex) {
            String msg = ex.getMessage();
            enableButton(true);
            FechaAguarde();
            //ExibeAlerta("ERRO", "Nao foi possivel obter a sua localização. Verifique as configurações de GPS", true);
        }
    }

    private void AddMarkers(List<Onibus> lstOnibus) {
        for (int i = 0; i < lstOnibus.size(); i++) {
            Onibus onibus = lstOnibus.get(i);
            String formattedDate = getFormatedDate(onibus.DataHora);
            LatLng latLng = new LatLng(onibus.Latitude, onibus.Longitude);

//            MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(ocorrencia.getResourceIcon()));
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(onibus.Linha + "-" + onibus.Ordem).snippet(formattedDate).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
            Marker marker = this.mMap.addMarker(markerOptions);
            this.listMarkerCenter.add(marker);
            this.getMarkers().put(marker, onibus);
        }
    }

    private void enableButton(boolean enable) {
        final Button button = (Button) findViewById(R.id.btnBuscar);
        button.setEnabled(enable);
    }

    @Override
    public void processFinish(Object output) {
        this.asyncTask = new TaskExecute(this);
        asyncTask.delegate = this;
        if (output instanceof String) {
            setUpMap();

        } else {
            enableButton(true);
            FechaAguarde();

            List<Onibus> lstOnibus = (List<Onibus>) output;
            if (lstOnibus != null && lstOnibus.size() > 0) {
                AddMarkers(lstOnibus);
                centerMap();
            } else {
                ExibeAlerta("Sem resultados", "Nenhum resultado encontrado", true);
            }
        }
    }
}
