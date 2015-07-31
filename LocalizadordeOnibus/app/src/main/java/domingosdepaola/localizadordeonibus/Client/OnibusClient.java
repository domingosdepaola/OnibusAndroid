package domingosdepaola.localizadordeonibus.Client;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import domingosdepaola.localizadordeonibus.BO.Onibus;
import domingosdepaola.localizadordeonibus.Common.CryptLib;


/**
 * Created by Doo on 11/07/2015.
 */
public class OnibusClient extends BaseClient {
    public static final String CONTROLLER = "onibus";
    public String url = BaseClient.ROOT_URL + "/" + this.CONTROLLER;

    public List<Onibus> getOnibus(String numeroLinha, double latitude, double longitude) {

        try {

            HashMap<String, Object> urlVariables = new HashMap<String, Object>();
            urlVariables = PutParameters(numeroLinha,latitude,longitude);
            Onibus[] listOnibus = super.getOpen(url.concat("/getonibus?x={x}&y={y}&z={z}"), urlVariables, Onibus[].class);

            return Arrays.asList(listOnibus);
        } catch (Exception e) {

            //logar
        }

        return null;
    }
    private HashMap<String, Object> PutParameters(String numeroLinha, double latitude, double longitude) {
        try {
            CryptLib cryptLib = new CryptLib();
            String numeroLinhaCript = cryptLib.encrypt(numeroLinha, "NestorDePaola");
            String latitudeStringCript = cryptLib.encrypt(Double.toString(latitude), "NestorDePaola");
            String longitudeStringCript = cryptLib.encrypt(Double.toString(longitude), "NestorDePaola");
            HashMap<String, Object> urlVariables = new HashMap<String, Object>();

            urlVariables.put("x", numeroLinhaCript);
            urlVariables.put("y", latitudeStringCript);
            urlVariables.put("z", longitudeStringCript);
            return urlVariables;
        } catch (Exception ex) {
            return null;
        }
    }

    public String getOnibusJson(String numeroLinha) {

        try {

            String retornoJson = super.getJsonOpen(url.concat("/getonibus?numeroLinha={numeroLinha}"), super.getParameter("numeroLinha", numeroLinha));

            return retornoJson;
        } catch (Exception e) {

            //logar
        }

        return null;
    }
}
