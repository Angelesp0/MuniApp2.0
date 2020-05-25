package com.example.questboard;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.questboard.directionhelpers.FetchURL;
import com.example.questboard.directionhelpers.TaskLoadedCallback;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        TaskLoadedCallback{

    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    private ArrayList<Marker> tmpRealTimeMarkers = new ArrayList<>();
    private ArrayList<Marker> realTimeMarkers = new ArrayList<>();
    // variable para crear la ruta de markadores
    private Polyline currentPolyline;
    // variable para la ubicacion del celular
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //igualamos las variables para simplificar el codigo despues
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    //la funcion se ejecuta cuando el mapa este listo
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // aqui es para ver tu posicion actual
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // habilitamos la ubicacion del dispositivo
        mMap.setMyLocationEnabled(true);
        // habilitamos el adaptador de la ventana( el cual usamos para mostrar la informacion de los marcadores
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
        // habilitamos un escuchador de click para el adaptador de ventana
        mMap.setOnInfoWindowClickListener(this);


        // buscamos en la base de datos de firebase el nodo usuarios y agregamos un evento para obtener sus datos
        mDatabase.child("usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            // la funcion verifica que los datos de los marcadores sean iguales a los de la BD
            for (Marker marker:realTimeMarkers){
                // borra los marcadores
               marker.remove();
           }


            //aqui se obtienen los datos de la BD
                //el for busca los nodos hijos de "usuarios" en la BD, y por cada uno, creara un markador
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    //en la variable "mp" estan los datos de cada uno de los nodos dentro de usuarios
                    MapsPojo mp = snapshot.getValue(MapsPojo.class);
                    //en la clase MapsPojo se encuentran los set y los get de las siguientes variables
                    //los set y get los usamos para obtener los valores y para poder distribuirlos a las-
                    //demas clases

                    // igualamos las variables de la clase MapsPojo para usarlas en esta clase
                    Double latitud = mp.getLatitud();
                    Double longitud = mp.getLongitud();
                    String titulo = mp.getTitulo();
                    String descripcion = mp.getDescripcion();

                    //comenzamos con la creacion de los marcadores
                    //usando los datos anteriores
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(latitud,longitud))
                                 .title(titulo)
                                 .snippet(descripcion);
                    tmpRealTimeMarkers.add(mMap.addMarker(markerOptions));
                }

                realTimeMarkers.clear();
                realTimeMarkers.addAll(tmpRealTimeMarkers);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    @Override
    // Esta funcion se ejecuta al clickear la ventana de los markadores
    public void onInfoWindowClick(final Marker marker) {
        // buscamos la ultima posicion registrada del dispositivo
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // obtenemos la ultima ubicacion ( location )
                        if (location != null) {
                            // extraemos la latitud y la longitud
                            Double latitud = location.getLatitude();
                            Double longitud = location.getLongitude();

                            // creamos una nueva variable de tipo LatLng
                            LatLng origen1 = new LatLng(latitud, longitud);

                            //-----------IMPORTANTE-----------//

                            // Llamamos a la funcion crearRuta y le enviamos la ubicacion del celular( origen1 )
                            // y el marcador el cual cliquemos( marker )
                            crearRuta(origen1 , marker);
                            return;
                        }
                    }
                });
    }
    // resivimos las dos ubicaciones (origen(origen1) y destino(marker))
    public void crearRuta (LatLng origen, Marker destino){
        // ejecutamos una busqueda y a la vez ejecutamos la funcion getUrl
        // en esta parte del codigo se ejecutaran las clases que se encuentran en el directorio "directionhelpers"
        // los cuales sirven para procesar la respuesta JSON que resiviremos de la funcion "getUrl"
        new FetchURL(MapsActivity.this).execute(getUrl(destino.getPosition(), origen, "driving"), "driving");
        return;
    }

    // la funcion resive el origen y el destino en tipo LatLng
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        // este link se completara con los datos de arriba y nos respondera con una respuesta tipo JSON
        // de la cual sacaremos los datos de las rutas
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    // despues de obtener la respuesta ya procesada, pintamos la ruta hacia el destino seleccionado
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
}
