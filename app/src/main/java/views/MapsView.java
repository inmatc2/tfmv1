package views;

import android.location.Location;

import java.util.ArrayList;

import model.ReducedMovilityPlace;

public interface MapsView {

    void getLocation();
    void getPermissions();
    void getPMR100m(Location ultimaUbicacionUser);
    void drawMarkers(ArrayList<ReducedMovilityPlace> results);

}
