package com.jokoa.app.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jokoa.app.R;

import java.util.List;

// Tarjeta de texto genérica para compras, alquileres y tickets
public class FilaAdapter extends RecyclerView.Adapter<FilaAdapter.Vista>
{
    public static class Fila
    {
        public final String titulo, detalle, derecha, boton;
        public final int id;

        public Fila(int id, String titulo, String detalle, String derecha, String boton)
        {
            this.id = id;
            this.titulo = titulo;
            this.detalle = detalle;
            this.derecha = derecha;
            this.boton = boton;
        }
    }

    public interface OnBoton { void alPulsar(Fila fila); }

    private final List<Fila> filas;
    private final OnBoton listener;

    public FilaAdapter(List<Fila> filas, OnBoton listener)
    {
        this.filas = filas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Vista onCreateViewHolder(@NonNull ViewGroup padre, int tipo)
    {
        return new Vista(LayoutInflater.from(padre.getContext()).inflate(R.layout.item_fila, padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vista v, int posicion)
    {
        Fila f = filas.get(posicion);
        v.tvTitulo.setText(f.titulo);
        v.tvDetalle.setText(f.detalle);
        v.tvDerecha.setText(f.derecha);
        v.btn.setVisibility(f.boton == null ? View.GONE : View.VISIBLE);
        v.btn.setText(f.boton);
        v.btn.setOnClickListener(x -> listener.alPulsar(f));
    }

    @Override
    public int getItemCount()
    {
        return filas.size();
    }

    static class Vista extends RecyclerView.ViewHolder
    {
        final TextView tvTitulo, tvDetalle, tvDerecha;
        final Button btn;

        Vista(View vista)
        {
            super(vista);
            tvTitulo = vista.findViewById(R.id.tvTitulo);
            tvDetalle = vista.findViewById(R.id.tvDetalle);
            tvDerecha = vista.findViewById(R.id.tvDerecha);
            btn = vista.findViewById(R.id.btnAccion);
        }
    }
}
