package model;

import android.location.Location;

import java.io.IOException;
import java.util.ArrayList;

public interface MapsModel {
    void getPMR100m(Location ultimaUbicacionUser); //Llamada desde el presentador
    ArrayList<ReducedMovilityPlace> getPMRFromOpenData(Location ultimaUbicacionUser) throws IOException; //Respuesta para el presenador

}
