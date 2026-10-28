package com.jokoa.app.fragmentos;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.jokoa.app.R;
import com.jokoa.app.red.Api;

import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
import java.util.List;

// Mapa con las tiendas de Jokoa (GET /tiendas): un marcador por tienda con dirección, horario y teléfono
public class FragmentoTiendas extends Fragment
{
    private MapView mapa;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // osmdroid necesita su configuración ANTES de cargar el mapa
        Context contexto = requireContext().getApplicationContext();
        Configuration.getInstance().load(contexto, contexto.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue("Jokoa-DAM/1.0 (proyecto de clase)");

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
                "EsriGrisClaro", 0, 16, 256, ".png",
                new String[]{"https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/"},
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
        // Un toque en el mapa cierra la tarjeta abierta
        mapa.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver()
        {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint punto)
            {
                InfoWindow.closeAllInfoWindowsOn(mapa);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint punto)
            {
                return false;
            }
        }));
        // Vista inicial sobre Bilbao mientras llegan las tiendas
        mapa.getController().setZoom(11.0);
        mapa.getController().setCenter(new GeoPoint(43.29, -2.96));

        Api.lista(requireContext(), "/tiendas",
                datos ->
                {
                    if (!isAdded()) return;
                    List<GeoPoint> puntos = new ArrayList<>();
                    for (int i = 0; i < datos.length(); i++)
                    {
                        JSONObject t = datos.optJSONObject(i);
                        GeoPoint punto = new GeoPoint(t.optDouble("latitud"), t.optDouble("longitud"));
                        puntos.add(punto);

                        Marker marcador = new Marker(mapa);
                        marcador.setPosition(punto);
                        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marcador.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pin_tienda));
                        marcador.setInfoWindow(new TarjetaTienda(mapa));
                        marcador.setInfoWindowAnchor(Marker.ANCHOR_CENTER, 0.05f);
                        marcador.setTitle(t.optString("nombre"));
                        marcador.setSnippet(t.optString("direccion"));
                        marcador.setSubDescription(t.optString("horario") + "\nTel. " + t.optString("telefono"));
                        marcador.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pin_tienda));
                        marcador.setInfoWindow(new TarjetaTienda(mapa));
                        marcador.setInfoWindowAnchor(Marker.ANCHOR_CENTER, 0.05f);
                        mapa.getOverlays().add(marcador);
                    }
                    if (puntos.size() > 1)
                    {
                        // Se ajusta el zoom para que se vean todas las tiendas (tras dibujar el mapa)
                        mapa.post(() -> mapa.zoomToBoundingBox(BoundingBox.fromGeoPoints(puntos), true, 120));
                    }
                    mapa.invalidate();
                },
                error ->
                {
                    if (isAdded())
                    {
                        Toast.makeText(getContext(), Api.mensaje(error, "No se pudieron cargar las tiendas"), Toast.LENGTH_LONG).show();
                    }
                });
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
