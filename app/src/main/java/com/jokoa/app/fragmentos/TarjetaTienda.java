package com.jokoa.app.fragmentos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;

import com.jokoa.app.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.Locale;

// Tarjeta que se abre al tocar el marcador de una tienda (sustituye al globo gris por defecto de osmdroid)
public class TarjetaTienda extends InfoWindow
{
    public TarjetaTienda(MapView mapa)
    {
        super(R.layout.tarjeta_tienda, mapa);
    }

    @Override
    public void onOpen(Object item)
    {
        Marker marcador = (Marker) item;
        ((TextView) mView.findViewById(R.id.tvNombre)).setText(marcador.getTitle());
        ((TextView) mView.findViewById(R.id.tvDireccion)).setText(marcador.getSnippet());
        ((TextView) mView.findViewById(R.id.tvHorario)).setText(marcador.getSubDescription());
        mView.findViewById(R.id.btnComoLlegar).setOnClickListener(v ->
        {
            // Abre la ruta en la app de mapas o, si no hay, en el navegador
            Context contexto = v.getContext();
            String destino = String.format(Locale.US, "%f,%f", marcador.getPosition().getLatitude(), marcador.getPosition().getLongitude());
            Intent mapas = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + destino));
            contexto.startActivity(mapas);
        });
        // Un toque sobre la propia tarjeta no la cierra: se cierra al tocar el mapa
    }

    @Override
    public void onClose()
    {
    }
}
