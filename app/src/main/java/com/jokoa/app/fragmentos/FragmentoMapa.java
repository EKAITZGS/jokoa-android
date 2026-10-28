package com.jokoa.app.fragmentos;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jokoa.app.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class FragmentoMapa extends Fragment
{
    // Localización de Almi (Deusto, Bilbao). Coordenadas aproximadas.
    private static final double LATITUD_ALMI = 43.2711;
    private static final double LONGITUD_ALMI = -2.9459;

    private MapView mapa;

    public FragmentoMapa()
    {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // osmdroid necesita su configuración ANTES de cargar el mapa
        Context contexto = getContext().getApplicationContext();
        Configuration.getInstance().load(contexto, contexto.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue("AppAlmi-DAM/1.0 (proyecto de clase)");

        View vista = inflater.inflate(R.layout.fragment_mapa, container, false);
        mapa = vista.findViewById(R.id.mapa);
        return vista;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // Mosaicos de Esri (calles). Esri pide el orden z/y/x, por eso se redefine la URL
        OnlineTileSourceBase esriCalles = new OnlineTileSourceBase(
                "EsriCalles", 0, 19, 256, ".png",
                new String[]{"https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/"},
                "Tiles © Esri")
        {
            @Override
            public String getTileURLString(long pMapTileIndex)
            {
                return getBaseUrl()
                        + MapTileIndex.getZoom(pMapTileIndex) + "/"
                        + MapTileIndex.getY(pMapTileIndex) + "/"
                        + MapTileIndex.getX(pMapTileIndex);
            }
        };
        mapa.setTileSource(esriCalles);
        mapa.setMultiTouchControls(true);

        // Se centra el mapa en Almi con zoom
        GeoPoint almi = new GeoPoint(LATITUD_ALMI, LONGITUD_ALMI);
        mapa.getController().setZoom(17.0);
        mapa.getController().setCenter(almi);

    }

    // El mapa debe reanudarse y pausarse junto con el fragmento
    @Override
    public void onResume()
    {
        super.onResume();
        mapa.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mapa.onPause();
    }
}