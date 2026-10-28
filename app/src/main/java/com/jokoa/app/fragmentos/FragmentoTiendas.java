package com.jokoa.app.fragmentos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.jokoa.app.R;
import com.jokoa.app.red.Api;

import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Mapa con las tiendas de Jokoa (GET /tiendas): pin azul por tienda, el seleccionado en coral y su tarjeta abajo
public class FragmentoTiendas extends Fragment
{
    private MapView mapa;
    private View tarjeta;
    private final List<Marker> marcadores = new ArrayList<>();
    private final List<JSONObject> tiendas = new ArrayList<>();
    private int seleccionada = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // osmdroid necesita su configuración ANTES de cargar el mapa
        Context contexto = requireContext().getApplicationContext();
        Configuration.getInstance().load(contexto, contexto.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue("Jokoa-DAM/1.0 (proyecto de clase)");

        View vista = inflater.inflate(R.layout.fragment_tiendas, container, false);
        mapa = vista.findViewById(R.id.mapa);
        tarjeta = vista.findViewById(R.id.tarjeta);
        vista.findViewById(R.id.btnComoLlegar).setOnClickListener(v ->
        {
            if (seleccionada < 0) return;
            JSONObject t = tiendas.get(seleccionada);
            String destino = String.format(Locale.US, "%f,%f", t.optDouble("latitud"), t.optDouble("longitud"));
            // Abre la ruta en la app de mapas o, si no hay, en el navegador
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + destino)));
        });
        return vista;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // Mosaicos grises de Esri: dejan destacar los pines. Esri pide el orden z/y/x
        OnlineTileSourceBase esriGris = new OnlineTileSourceBase(
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
        mapa.setTileSource(esriGris);
        mapa.setMultiTouchControls(true);
        mapa.getController().setZoom(11.0);
        mapa.getController().setCenter(new GeoPoint(43.29, -2.96));

        Api.lista(requireContext(), "/tiendas",
                datos ->
                {
                    if (!isAdded()) return;
                    List<GeoPoint> puntos = new ArrayList<>();
                    for (int i = 0; i < datos.length(); i++)
                    {
                        final int indice = i;
                        JSONObject t = datos.optJSONObject(i);
                        tiendas.add(t);
                        GeoPoint punto = new GeoPoint(t.optDouble("latitud"), t.optDouble("longitud"));
                        puntos.add(punto);

                        Marker marcador = new Marker(mapa);
                        marcador.setPosition(punto);
                        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marcador.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pin_azul));
                        marcador.setOnMarkerClickListener((m, v) ->
                        {
                            seleccionar(indice);
                            return true;
                        });
                        marcadores.add(marcador);
                        mapa.getOverlays().add(marcador);
                    }
                    if (puntos.size() > 1)
                    {
                        // Se ajusta el zoom para que se vean todas las tiendas (tras dibujar el mapa)
                        mapa.post(() -> mapa.zoomToBoundingBox(BoundingBox.fromGeoPoints(puntos), true, 160));
                    }
                    if (!tiendas.isEmpty())
                    {
                        seleccionar(0);
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

    private void seleccionar(int indice)
    {
        if (seleccionada >= 0)
        {
            marcadores.get(seleccionada).setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pin_azul));
        }
        seleccionada = indice;
        marcadores.get(indice).setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pin_tienda));
        // El pin seleccionado se dibuja encima de los demás
        mapa.getOverlays().remove(marcadores.get(indice));
        mapa.getOverlays().add(marcadores.get(indice));

        JSONObject t = tiendas.get(indice);
        ((TextView) tarjeta.findViewById(R.id.tvNombre)).setText(t.optString("nombre"));
        ((TextView) tarjeta.findViewById(R.id.tvDireccion)).setText(t.optString("direccion"));
        ((TextView) tarjeta.findViewById(R.id.tvHorario)).setText(t.optString("horario"));
        ((TextView) tarjeta.findViewById(R.id.tvTelefono)).setText("Tel. " + t.optString("telefono"));
        if (tarjeta.getVisibility() != View.VISIBLE)
        {
            tarjeta.setAlpha(0f);
            tarjeta.setVisibility(View.VISIBLE);
            tarjeta.animate().alpha(1f).setDuration(220).start();
        }
        mapa.invalidate();
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
