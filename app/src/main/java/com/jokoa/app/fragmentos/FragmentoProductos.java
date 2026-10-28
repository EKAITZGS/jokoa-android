package com.jokoa.app.fragmentos;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jokoa.app.DetalleActivity;
import com.jokoa.app.R;
import com.jokoa.app.adaptadores.ProductoAdapter;
import com.jokoa.app.datos.Producto;
import com.jokoa.app.red.Api;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// Lista de móviles o de videojuegos: se elige con el argumento "ruta" (/moviles o /videojuegos)
public class FragmentoProductos extends Fragment
{
    public static FragmentoProductos nuevo(String ruta, String titulo)
    {
        FragmentoProductos f = new FragmentoProductos();
        Bundle args = new Bundle();
        args.putString("ruta", ruta);
        args.putString("titulo", titulo);
        f.setArguments(args);
        return f;
    }

    private final List<Producto> productos = new ArrayList<>();
    private LinearLayout esqueleto, bloqueError;
    private RecyclerView lista;
    private View progreso;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup contenedor, @Nullable Bundle estado)
    {
        View vista = inflater.inflate(R.layout.fragment_productos, contenedor, false);
        ((TextView) vista.findViewById(R.id.tvTitulo)).setText(getArguments().getString("titulo"));
        esqueleto = vista.findViewById(R.id.esqueleto);
        bloqueError = vista.findViewById(R.id.bloqueError);
        lista = vista.findViewById(R.id.lista);
        progreso = vista.findViewById(R.id.progreso);
        lista.setLayoutManager(new LinearLayoutManager(getContext()));
        lista.setAdapter(new ProductoAdapter(productos, (producto, imagen) ->
        {
            // La foto de la tarjeta crece hasta la ficha (transición compartida)
            Intent intent = new Intent(requireContext(), DetalleActivity.class);
            intent.putExtra("producto", producto);
            ActivityOptions opciones = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), imagen, imagen.getTransitionName());
            startActivity(intent, opciones.toBundle());
        }));
        vista.findViewById(R.id.btnReintentar).setOnClickListener(v -> cargar());

        // Las tarjetas grises de espera laten mientras llegan los datos
        esqueleto.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.pulso));
        cargar();
        return vista;
    }

    private void cargar()
    {
        bloqueError.setVisibility(View.GONE);
        esqueleto.setVisibility(View.VISIBLE);
        progreso.setVisibility(View.VISIBLE);
        final String ruta = getArguments().getString("ruta");
        final boolean esMovil = ruta.equals("/moviles");
        Api.lista(requireContext(), ruta,
                respuesta ->
                {
                    productos.clear();
                    for (int i = 0; i < respuesta.length(); i++)
                    {
                        JSONObject j = respuesta.optJSONObject(i);
                        if (j != null)
                        {
                            productos.add(new Producto(j, esMovil));
                        }
                    }
                    esqueleto.clearAnimation();
                    esqueleto.setVisibility(View.GONE);
                    progreso.setVisibility(View.GONE);
                    lista.getAdapter().notifyDataSetChanged();
                    lista.scheduleLayoutAnimation();
                },
                error ->
                {
                    esqueleto.clearAnimation();
                    esqueleto.setVisibility(View.GONE);
                    progreso.setVisibility(View.GONE);
                    bloqueError.setVisibility(View.VISIBLE);
                });
    }
}
