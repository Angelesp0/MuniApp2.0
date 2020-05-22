package com.example.questboard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    private ArrayList<Marker> tmpRealTimeMarkers = new ArrayList<>();
    private ArrayList<Marker> realTimeMarkers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // aqui es para ver tu posicion actual
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));


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
}
