package domingosdepaola.localizadordeonibus.Client;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import domingosdepaola.localizadordeonibus.Common.CryptLib;
import domingosdepaola.localizadordeonibus.Common.DateUtil;

/**
 * Created by Doo on 11/07/2015.
 */
public class BaseClient {

    //public static final String ROOT_URL = "http://10.2.11.115/PadSamuWebApi/api/";
    public static final String ROOT_URL = "http://www.localizadordeonibus.com.br/OnibusWebAPI/api";
    //public static final String ROOT_URL = "http://192.168.0.2/OnibusWebAPI/api";
    //public static final String ROOT_URL = "http://10.2.11.192/SamuWebApi/api/";
    //public static final String ROOT_URL = "http://10.2.11.5/SamuWebApi/api/";
    protected RestTemplate restTemplate;
    private CryptLib cryptLib;

    public BaseClient() {

        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
        try {

            this.setCryptLib(new CryptLib());
        } catch (Exception e) {


        }
    }

   public <T> T getCripted(String url, HashMap<String, Object> urlVariables, Class<T> returnType, String key) {

        try {

            String encryptedJson = this.restTemplate.exchange(url, HttpMethod.GET, null, String.class, urlVariables).getBody();
            String decryptedJson = this.cryptLib.decrypt(encryptedJson, key);


            ObjectMapper objectMapper = new ObjectMapper();
            T object = objectMapper.readValue(decryptedJson, returnType);

            return object;
        } catch (Exception e) {

            String message = e.getMessage();
            //logar
        }

        return null;
    }
    public <T> T getFromHttpRequest(String url,Class<T> returnType){
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(url));
            org.apache.http.HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            String resposta = sb.toString();

            ObjectMapper objectMapper = new ObjectMapper();
            T object = objectMapper.readValue(resposta, returnType);

            return object;

        }catch (Exception ex){
            return null;
        }
    }
    public <T> T getOpen(String url, HashMap<String, Object> urlVariables, Class<T> returnType) {

        try {

            String retornoJson = this.restTemplate.exchange(url, HttpMethod.GET, null, String.class, urlVariables).getBody();

            ObjectMapper objectMapper = new ObjectMapper();
            T object = objectMapper.readValue(retornoJson, returnType);

            return object;
        } catch (Exception e) {

            String message = e.getMessage();
            //logar
        }

        return null;
    }
    public String getJsonOpen(String url, HashMap<String, Object> urlVariables) {

        try {

            String retornoJson = this.restTemplate.exchange(url, HttpMethod.GET, null, String.class, urlVariables).getBody();

            return retornoJson;
        } catch (Exception e) {

            String message = e.getMessage();
            //logar
        }

        return null;
    }
    private <T> T sendRequest(String url, HashMap<String, Object> urlVariables, Class<T> returnType, String key, Object objectBody, HttpMethod httpMethod) {

        try {

            ObjectMapper objectMapper = new ObjectMapper();

            String decryptedObject = objectMapper.writeValueAsString(objectBody);
            String encryptedObject = this.cryptLib.encrypt(decryptedObject, key);

            HttpEntity<String> httpEntity = new HttpEntity<String>(encryptedObject);

            String encryptedJson = this.restTemplate.exchange(url, httpMethod, httpEntity, String.class, urlVariables).getBody();
            String decryptedJson = null;

            if (!returnType.getClass().toString().equals(Boolean.class.toString())) {

                try {

                    decryptedJson = this.cryptLib.decrypt(encryptedJson, key);
                } catch (Exception e) {


                }
            }

            T object = null;

            if (decryptedJson != null)
                object = objectMapper.readValue(decryptedJson, returnType);
            else
                object = objectMapper.readValue(encryptedJson, returnType);

            return object;
        } catch (Exception e) {

            String message = e.getMessage();
            //logar
        }

        return null;
    }

    public <T> T post(String url, HashMap<String, Object> urlVariables, Class<T> returnType, String key, Object objectBody) {

        return this.sendRequest(url, urlVariables, returnType, key, objectBody, HttpMethod.POST);
    }

    public <T> T put(String url, HashMap<String, Object> urlVariables, Class<T> returnType, String key, Object objectBody) {

        return this.sendRequest(url, urlVariables, returnType, key, objectBody, HttpMethod.PUT);
    }

    protected HashMap<String, Object> getDefaultParameters(String token) {

        return null;
    }

    protected HashMap<String, Object> getDefaultParameters(String token, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, UnsupportedEncodingException {

        HashMap<String, Object> urlVariables = new HashMap<String, Object>();

        Date date = new Date();
        String dateKey = this.getDateKey(key, date);

        String encryptedToken = this.getCryptLib().encrypt(token, dateKey);
        String dateRandomKey = this.getCryptLib().encrypt(this.getDateKeyRandom(date), key);

        urlVariables.put("x", encryptedToken);
        urlVariables.put("y", dateRandomKey);

        return urlVariables;
    }
    protected   HashMap<String, Object> getParameter(String parameter,String valor)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();

        urlVariables.put(parameter, valor);

        return urlVariables;
    }

    protected HashMap<String, Object> getDefaultParametersWithZ(String token, String key, Object ZObject) {
        try {
            HashMap<String, Object> urlVariables = getDefaultParameters(token, key);
            ObjectMapper objectMapper = new ObjectMapper();

            String decryptedObject = objectMapper.writeValueAsString(ZObject);
            String encryptedObject = this.cryptLib.encrypt(decryptedObject, key);
            urlVariables.put("z", encryptedObject);
            return urlVariables;
        } catch (Exception ex) {
            return null;
        }
    }

    public String getDateKey(String key, Date date) {

        String formattedDate = DateUtil.format(date, "yyyyMMddHHmmssSSS");

        String returnKey = key + formattedDate;

        return returnKey;
    }

    protected String getDateKeyRandom(Date date) {

        String randomNumber = this.getRandomNumber();
        String formattedDate = DateUtil.format(date, "yyyyMMddHHmmssSSS");
        char[] randomArray = randomNumber.toCharArray();
        char[] dateArray = formattedDate.toCharArray();

        //ano
        randomArray[2] = dateArray[0];
        randomArray[7] = dateArray[1];
        randomArray[13] = dateArray[2];
        randomArray[19] = dateArray[3];

        //mes
        randomArray[4] = dateArray[4];
        randomArray[16] = dateArray[5];

        //dia
        randomArray[0] = dateArray[6];
        randomArray[32] = dateArray[7];

        //hora
        randomArray[9] = dateArray[8];
        randomArray[22] = dateArray[9];

        //minuto
        randomArray[5] = dateArray[10];
        randomArray[34] = dateArray[11];

        //segundo
        randomArray[10] = dateArray[12];
        randomArray[23] = dateArray[13];

        //milisegundo
        randomArray[40] = dateArray[14];
        randomArray[36] = dateArray[15];
        randomArray[38] = dateArray[16];

        return String.valueOf(randomArray);
    }

    private String getRandomNumber() {

        StringBuilder randomNumber = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 41; i++)
            randomNumber.append(random.nextInt(9));

        return randomNumber.toString();
    }

    public CryptLib getCryptLib() {
        return cryptLib;
    }

    public void setCryptLib(CryptLib cryptLib) {
        this.cryptLib = cryptLib;
    }
}

