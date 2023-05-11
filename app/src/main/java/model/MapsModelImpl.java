package model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MapsModelImpl implements  MapsModel{
    @Override
    public void getPMR100m(Location ultimaUbicacionUser) {

    }

    public static ArrayList<ReducedMovilityPlace> getPMRFromOpenData(Location ultimaUbicacionUser) throws IOException {
        ArrayList<ReducedMovilityPlace> placesList = new ArrayList<ReducedMovilityPlace>();
/**SELECT ?geo_lat ?geo_long ?rdfs_comment bif:st_distance ( bif:st_point (39.469154, -6.373242), bif:st_point (?geo_lat ,?geo_long) ) AS ?distancia
 WHERE {
 ?uri a pmr:Plaza.
 ?uri geo:long ?geo_long.
 ?uri geo:lat ?geo_lat.
 ?uri pmr:rdfsComment ?rdfs_comment.

 FILTER(bif:st_distance(bif:st_point (39.469154, -6.373242), bif:st_point (?geo_lat , ?geo_long))<= 0.1)
 }
 ORDER BY ?distancia*/
        String specificURL =
                "&query=SELECT+%3Fgeo_lat+%3Fgeo_long+%3Frdfs_comment+bif%3Ast_distance+%28+bif%3Ast_point+%28";
        specificURL = specificURL.concat(String.valueOf(ultimaUbicacionUser.getLatitude()));
        specificURL = specificURL.concat("%2C+");
        specificURL = specificURL.concat(String.valueOf(ultimaUbicacionUser.getLongitude()));
        specificURL = specificURL.concat("%29%2C+bif%3Ast_point+%28%3Fgeo_lat+%2C%3Fgeo_long%29+%29+AS+%3Fdistancia%0D%0A+WHERE+%7B%0D%0A+%3Furi+a+pmr%3APlaza.%0D%0A+%3Furi+geo%3Along+%3Fgeo_long.%0D%0A+%3Furi+geo%3Alat+%3Fgeo_lat.%0D%0A+%3Furi+pmr%3ArdfsComment+%3Frdfs_comment.%0D%0A%0D%0A+FILTER%28bif%3Ast_distance%28bif%3Ast_point+%28");
        specificURL = specificURL.concat(String.valueOf(ultimaUbicacionUser.getLatitude()));
        specificURL = specificURL.concat("%2C+");
        specificURL = specificURL.concat(String.valueOf(ultimaUbicacionUser.getLongitude()));
        specificURL = specificURL.concat("%29%2C+bif%3Ast_point+%28%3Fgeo_lat+%2C+%3Fgeo_long%29%29%3C%3D+0.1%29%0D%0A+%7D%0D%0A+ORDER+BY+%3Fdistancia&format=text%2Fhtml&timeout=0");

        NetworkFunctions networkFunctions = new NetworkFunctions(specificURL);
        networkFunctions.start();

        try {
            networkFunctions.join();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        String res = networkFunctions.resultRequest;
        String[] splitStr = res.split("<tr>");

        for (String filaHTML : splitStr) {
            if (filaHTML.contains("<td>")) {
                String[] splitCampos = filaHTML.split("<td>");
                String lat = splitCampos[1].replaceAll("[^0-9?!\\.-]", "");
                String lon = splitCampos[2].replaceAll("[^0-9?!\\.-]", "");

                LatLng location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));

                String comentario = splitCampos[3].substring(0, splitCampos[3].indexOf("^")).replaceAll("\"", "");
                String numero = comentario.replaceAll("[^0-9?!\\.]", "");
                String barrio = comentario.substring(comentario.indexOf("barrio")).replace("barrio","");
                String nombre = "PMR " + numero+ "-" +barrio;
                ReducedMovilityPlace pmr = new ReducedMovilityPlace(nombre, comentario, location);
                pmr.setBarrio(barrio);
                placesList.add(pmr);
            }

        }


        return placesList;
    }
}
