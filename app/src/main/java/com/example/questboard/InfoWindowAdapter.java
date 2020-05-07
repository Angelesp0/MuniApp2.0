package com.example.questboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

class InfoWndowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public InfoWndowAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v =  inflater.inflate(R.layout.layout_inf, null);


        TextView descripcion = (TextView) v.findViewById(R.id.desc);
        Button editar =(Button) v.findViewById(R.id.btned);
        // TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
        descripcion.setText("Gir Forest is located in the Gujarat State of India");
                //tvLng.setText("Longitude:"+ latLng.longitude);
        return v;
    }


}