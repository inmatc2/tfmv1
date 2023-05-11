package presenter;

import android.location.Location;

import java.io.IOException;
import java.util.ArrayList;

import model.ReducedMovilityPlace;

public interface MapsPresenter {

    void getPMR100m(Location ultimaUbicacionUser); //Llamada al modelo

    void getPMRFromOpenData(ArrayList<ReducedMovilityPlace> results) throws IOException; //Respuesta para  la vista

}
