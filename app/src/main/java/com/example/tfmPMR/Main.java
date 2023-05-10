package com.example.tfmPMR;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


public class Main extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    //Constantes
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final String TAG = Main.class.getSimpleName();

    private GoogleMap map;

    // Punto de acceso Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean permissionUbicationOK = false;

    //Ubicacion del dispositivo
    private static Location ubicationNow;

    public static FirebaseAuth mAuth;
    public static FirebaseUser user;


    /**
     * Metodo principal
     */
    protected void onCreate(Bundle savedInstanceState) {
        //Recupera ubicacion anterior
        super.onCreate(savedInstanceState);

        // Pinta la pantalla
        setContentView(R.layout.activity_maps);

        // FusedLocationProviderClient para obtener la ubicacion actual
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Construccion del mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }


    /**
     * Funcionamiento cuando el mapa está listo
     */
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;

        // Creacion de la nueva pantalla
        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            //Metodo que no hace nada para que se llame al siguiente
            public View getInfoWindow(@NonNull Marker arg0) {
                return null;
            }

            //Metodo que pinta la pantalla inicial
            public View getInfoContents(@NonNull Marker marker) {
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                TextView snippet = infoWindow.findViewById(R.id.snippet);

                title.setText(marker.getTitle());
                snippet.setText(marker.getSnippet());
                return infoWindow;
            }
        });

        //Se obtienen los permisos de localizacion y se actualiza en el mapa
        pidePermisosdeUbicacion();
        if (this.permissionUbicationOK) {
            actualizaLocationUI();
            obtenerUbicacion();
            PMRFunctions.firebaseActions();

        }
        map.setOnMarkerClickListener(this);

    }



    /**
     * Pinta las acciones del menu
     *
     * @return Boolean.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Control de las acciones del menu
     *
     * @return Boolean.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean res = false;
        if (map != null) {
            if (this.permissionUbicationOK) {
                switch (item.getOrder()) {
                    case 0:
                        if (ubicationNow != null) {
                            try {
                                showMePlaces100m();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        break;

                }
            } else {
                Log.i(TAG, getString(R.string.permiso_Denegado));
                pidePermisosdeUbicacion();
            }
            res = true;

        }
        return res;
    }

/***********************FUNCIONES MENU*********************************************************/
    /**
     * Obtiene las plazas cercanas a 100 metros de la ubicacion del usuario. Crea los marcadores referentes, los pinta en el mapa y muestra zoom alto
     */
    private void showMePlaces100m() throws IOException {
        ArrayList<PMRData> list100Places = PMRFunctions.Places100m(ubicationNow);
        for (PMRData pmr : list100Places) {
            MarkerOptions marker = new MarkerOptions()
                    .title(pmr.getNombre())
                    .position(pmr.getUbicacion())
                    .snippet(pmr.getDescripcion());
            map.addMarker(marker);
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(ubicationNow.getLatitude(), ubicationNow.getLongitude()),
                18));

    }


/***********************FUNCIONES UBICACION Y PERMISOS*********************************************************/

    /**
     * Metodo que obtiene la ubicación actual y la posiciona en el mapa si tiene los permisos del dispositivo
     */
    private void obtenerUbicacion() {
        try {
            if (this.permissionUbicationOK) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation(); //ultima ubicacion

                //Se añade un listener para mover el mapa a la ultima ubicacion detectada. Si hay error se utilizaran la ubicacion por defecto
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    public void onComplete(@NonNull Task<Location> task) {

                        ubicationNow = task.getResult();
                        if (ubicationNow != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(ubicationNow.getLatitude(),
                                            ubicationNow.getLongitude()), DEFAULT_ZOOM));
                        }

                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Método que pide los permisos de localizacion al usuario.
     */
    private void pidePermisosdeUbicacion() {
        CharSequence text = "Permiso de ubicación no otorgado, para utilizar la aplicación debes otorgarlo.";

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.permissionUbicationOK = true;
        } else {
            Toast toast = Toast.makeText(this.getApplicationContext(), text, Toast.LENGTH_LONG);
            toast.show();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    /**
     * Método que controla el resultado de la petición de permisos de localizacion
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.permissionUbicationOK = true;
                actualizaLocationUI();
            } else {
                this.permissionUbicationOK = false;
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }

    }

    /**
     * Indica si el usuario ha otorgado los permisos de localizacion y actualiza la ubicacion de la pantalla
     */
    private void actualizaLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (this.permissionUbicationOK) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Guarda el estado del mapa
     */
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, ubicationNow);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Cuando se pulsa un marcador aparece el dialogo para indicar la ocupacion
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(marker.getTitle());

        alertDialogBuilder
                .setMessage("Hola ¿has ocupado esta plaza? ")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        //Si la respuesta es afirmativa aquí agrega tu función a realizar.
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).create().show();
        return false;
    }


    ///////////METODOS NO  USADOS///////////777

    /**
     * Obtiene las plazas cercanas a 5 metros de la ubicacion del usuario y las muestra en un dialogo.

     private void showMeBarrios() throws IOException {
     ArrayList<PMRData> conjuntoDePlazas = PMRFunctions.Places100m(ubicationNow);
     String[] arrayNombresPlazas = PMRFunctions.obtenerNombrePlazas(conjuntoDePlazas);
     //this.abrirDialogoConPlazas(conjuntoDePlazas, arrayNombresPlazas);
     }

     /**
     * Se muestra un dialogo con las plazas mas cercanas a 5 metros obtenidas. Al seleccionar una de ellas se dirige a la ubicación con un marcador

     private void abrirDialogoConPlazas(PMRData[] conjuntoDePlazas, String[] arrayNombresPlazas) {

     //Al pulsar en una plaza se obtiene la posicion y se busca en la lista. A continuacion se crea un marcador y se redirige la camara
     DialogInterface.OnClickListener listener = (dialog, pos) -> {
     PMRData pmr = conjuntoDePlazas[pos];

     map.addMarker(new MarkerOptions()
     .title(pmr.getNombre())
     .position(pmr.getUbicacion())
     .snippet(pmr.getDescripcion()));

     map.moveCamera(CameraUpdateFactory.newLatLngZoom(pmr.getUbicacion(),
     DEFAULT_ZOOM));
     };

     AlertDialog.Builder builder = new AlertDialog.Builder(this);
     //   builder.setTitle(R.string.pick_place);
     builder.setItems(arrayNombresPlazas, listener);
     builder.show();
     }



     /**
     * Se crea un marcaador para cada PMR del array, se pinta en el mapa y se muestra una con zoom alto

     private void crearMarcadoresPlazas(PMRData[] conjuntoDePlazas) {
     for (PMRData pmr : conjuntoDePlazas) {
     map.addMarker(new MarkerOptions()
     .title(pmr.getNombre())
     .position(pmr.getUbicacion())
     .snippet(pmr.getDescripcion()));
     }

     map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
     }
     */
}

