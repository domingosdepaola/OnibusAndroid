package domingosdepaola.localizadordeonibus.Client;

import android.os.AsyncTask;

import java.util.List;
import java.util.Objects;

import domingosdepaola.localizadordeonibus.BO.Onibus;
import domingosdepaola.localizadordeonibus.OnibusActivity;

/**
 * Created by Doo on 11/07/2015.
 */
public class TaskExecute extends AsyncTask {
    public AsyncResponse delegate = null;
    public String numeroLinha;
    private OnibusActivity activity;

    public TaskExecute(OnibusActivity activity) {
        this.activity = activity;
    }

    public TaskExecute() {

    }

    @Override
    protected void onPostExecute(Object result) {
        delegate.processFinish(result);
    }

    @Override
    protected Object doInBackground(Object[] params) {
        if(params.length == 1){
            return  "Retorno";
        }else {
            OnibusClient onibusClient = new OnibusClient();
            String numeroLinha = "";
            double latitude = 0;
            double longitude = 0;

            if (params.length == 3) {

                numeroLinha = params[0].toString();
                latitude = Double.parseDouble(params[1].toString());
                longitude = Double.parseDouble(params[2].toString());
            }
            return onibusClient.getOnibus(numeroLinha, latitude, longitude);
        }
    }
}
