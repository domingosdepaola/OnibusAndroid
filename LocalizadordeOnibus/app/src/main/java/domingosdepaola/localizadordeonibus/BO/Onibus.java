package domingosdepaola.localizadordeonibus.BO;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Date;

/**
 * Created by Doo on 11/07/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Onibus {
    public  String Ordem;
    public String DataHora;
    public String Linha;
    public double Latitude;
    public  double Longitude;
    public String Velocidade;
    public String Endereco;
}
