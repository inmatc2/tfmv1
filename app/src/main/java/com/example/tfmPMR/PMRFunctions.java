package com.example.tfmPMR;

import com.google.android.gms.maps.model.LatLng;

import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

public class PMRFunctions {


    /**
     * FUncion que devuelve los nombres de las PMR
     *
     * @param conjuntoDePlazas
     * @return
     */
    public static String[] obtenerNombrePlazas(ArrayList<PMRData> conjuntoDePlazas) {
        String[] nombrePMR = new String[conjuntoDePlazas.size()];
        for (int i = 0; i < conjuntoDePlazas.size(); i++) {
            nombrePMR[i] = conjuntoDePlazas.get(i).getBarrio();
        }
        return nombrePMR;
    }

    private void loginFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                        }
                    }
                });
        setmAuth(mAuth);
        setUser(user);
    }

public static void firebaseActions(){
    public void firebase() {

        loginFirebase();
        if (getUser() == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error al acceder", Toast.LENGTH_LONG);
            toast.show();
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference pmrFirestore = db.collection("pmropendata");

//Obtencion todos
            pmrFirestore.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            Map<String, Object> data3 = document.getData();
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        }
    }

}
    public static ArrayList<PMRData> Places100m(Location ultimaUbicacionUser) throws IOException {
        ArrayList<PMRData> placesList = new ArrayList<PMRData>();
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
                PMRData pmr = new PMRData(nombre, comentario, location);
                pmr.setBarrio(barrio);
                placesList.add(pmr);
            }

        }


        return placesList;
    }
}
