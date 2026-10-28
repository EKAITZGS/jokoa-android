package com.jokoa.app.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.jokoa.app.R;
import com.jokoa.app.adaptadores.FilaAdapter;
import com.jokoa.app.red.Api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Historial del usuario: pestaña de compras y pestaña de alquileres (con botón Devolver)
public class FragmentoPedidos extends Fragment
{
    private RecyclerView lista;
    private LinearProgressIndicator progreso;
    private TextView tvVacio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup contenedor, @Nullable Bundle estado)
    {
        View vista = inflater.inflate(R.layout.fragment_pedidos, contenedor, false);
        lista = vista.findViewById(R.id.lista);
        progreso = vista.findViewById(R.id.progreso);
        tvVacio = vista.findViewById(R.id.tvVacio);
        lista.setLayoutManager(new LinearLayoutManager(getContext()));

        TabLayout pestanas = vista.findViewById(R.id.pestanas);
        pestanas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                cargar(tab.getPosition() == 0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
        cargar(true);
        return vista;
    }

    private void cargar(boolean compras)
    {
        progreso.setVisibility(View.VISIBLE);
        tvVacio.setVisibility(View.GONE);
        Api.lista(requireContext(), compras ? "/compras" : "/alquileres",
                datos ->
                {
                    if (!isAdded()) return;
                    progreso.setVisibility(View.GONE);
                    pintar(compras ? filasCompras(datos) : filasAlquileres(datos), compras);
                },
                error ->
                {
                    if (!isAdded()) return;
                    progreso.setVisibility(View.GONE);
                    Toast.makeText(getContext(), Api.mensaje(error, "No se pudieron cargar los datos"), Toast.LENGTH_LONG).show();
                });
    }

    private void pintar(List<FilaAdapter.Fila> filas, boolean compras)
    {
        tvVacio.setText(compras ? "Todavía no has comprado nada" : "Todavía no has alquilado nada");
        tvVacio.setVisibility(filas.isEmpty() ? View.VISIBLE : View.GONE);
        lista.setAdapter(new FilaAdapter(filas, fila -> devolver(fila.id)));
        lista.scheduleLayoutAnimation();
    }

    private List<FilaAdapter.Fila> filasCompras(JSONArray datos)
    {
        List<FilaAdapter.Fila> filas = new ArrayList<>();
        for (int i = 0; i < datos.length(); i++)
        {
            JSONObject c = datos.optJSONObject(i);
            JSONArray lineas = c.optJSONArray("lineas");
            StringBuilder detalle = new StringBuilder();
            for (int j = 0; lineas != null && j < lineas.length(); j++)
            {
                JSONObject l = lineas.optJSONObject(j);
                if (j > 0) detalle.append('\n');
                detalle.append(l.optInt("cantidad")).append(" × ").append(l.optString("nombre"));
            }
            filas.add(new FilaAdapter.Fila(c.optInt("id"), "Compra #" + c.optInt("id") + " · " + c.optString("fecha"),
                    detalle.toString(), String.format(Locale.getDefault(), "%.2f €", c.optDouble("total")), null));
        }
        return filas;
    }

    private List<FilaAdapter.Fila> filasAlquileres(JSONArray datos)
    {
        List<FilaAdapter.Fila> filas = new ArrayList<>();
        for (int i = 0; i < datos.length(); i++)
        {
            JSONObject a = datos.optJSONObject(i);
            boolean devuelto = !a.isNull("fecha_devolucion");
            String detalle = "Del " + a.optString("fecha_inicio") + " al " + a.optString("fecha_fin")
                    + (devuelto ? "\nDevuelto el " + a.optString("fecha_devolucion") : "");
            filas.add(new FilaAdapter.Fila(a.optInt("id"), a.optString("videojuego"), detalle,
                    String.format(Locale.getDefault(), "%.2f €/día", a.optDouble("precio_dia")), devuelto ? null : "Devolver"));
        }
        return filas;
    }

    private void devolver(int id)
    {
        Api.enviar(requireContext(), Api.put(), "/alquileres/devolver/" + id, null,
                respuesta ->
                {
                    Toast.makeText(getContext(), "Juego devuelto", Toast.LENGTH_SHORT).show();
                    if (isAdded()) cargar(false);
                },
                error -> Toast.makeText(getContext(), Api.mensaje(error, "No se pudo devolver"), Toast.LENGTH_LONG).show());
    }
}
