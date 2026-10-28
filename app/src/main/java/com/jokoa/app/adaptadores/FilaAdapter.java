package com.jokoa.app.adaptadores;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.jokoa.app.R;

import java.util.List;

// Tarjeta de texto genérica: título, valor a la derecha, estado en píldora, detalle y botón opcional
public class FilaAdapter extends RecyclerView.Adapter<FilaAdapter.Vista>
{
    // Estados que se pintan como píldora (ver sistema de diseño: forma y color distintos en cada uno)
    public static final int SIN_ESTADO = 0, ABIERTO = 1, EN_CURSO = 2, CERRADO = 3, DEVUELTO = 4;

    public static class Fila
    {
        public final int id, estado;
        public final String titulo, subtitulo, detalle, derecha, pie, boton;

        public Fila(int id, String titulo, String subtitulo, String detalle, String derecha, String pie, String boton, int estado)
        {
            this.id = id;
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.detalle = detalle;
            this.derecha = derecha;
            this.pie = pie;
            this.boton = boton;
            this.estado = estado;
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
        mostrar(v.tvDerecha, f.derecha);
        mostrar(v.tvSubtitulo, f.subtitulo);
        mostrar(v.tvPie, f.pie);

        v.chipEstado.setVisibility(View.GONE);
        v.chipPie.setVisibility(View.GONE);
        if (f.estado == DEVUELTO)
        {
            estiloChip(v.chipPie, "Devuelto", f.estado);
        }
        else if (f.estado != SIN_ESTADO)
        {
            estiloChip(v.chipEstado, f.estado == ABIERTO ? "Abierto" : f.estado == EN_CURSO ? "En curso" : "Cerrado", f.estado);
        }

        v.btn.setVisibility(f.boton == null ? View.GONE : View.VISIBLE);
        v.btn.setText(f.boton);
        v.btn.setOnClickListener(x -> listener.alPulsar(f));
    }

    private static void mostrar(TextView tv, String texto)
    {
        boolean hay = texto != null && !texto.isEmpty();
        tv.setVisibility(hay ? View.VISIBLE : View.GONE);
        tv.setText(texto);
    }

    private static void estiloChip(TextView chip, String texto, int estado)
    {
        Context c = chip.getContext();
        float d = c.getResources().getDisplayMetrics().density;
        int fondo, color, icono;
        if (estado == ABIERTO)
        {
            fondo = R.drawable.bg_chip_coral;
            color = R.color.jokoa_coral_oscuro;
            icono = R.drawable.bg_punto_cuadrado;
        }
        else if (estado == EN_CURSO)
        {
            fondo = R.drawable.bg_chip_azul;
            color = R.color.jokoa_azul_noche;
            icono = R.drawable.bg_punto_azul;
        }
        else
        {
            fondo = R.drawable.bg_chip_exito;
            color = R.color.jokoa_exito;
            icono = R.drawable.ic_check;
        }
        chip.setVisibility(View.VISIBLE);
        chip.setText(texto);
        chip.setBackgroundResource(fondo);
        chip.setTextColor(ContextCompat.getColor(c, color));
        chip.setTextSize(12);
        chip.setTypeface(Typeface.DEFAULT_BOLD);
        chip.setPadding((int) (12 * d), (int) (6 * d), (int) (12 * d), (int) (6 * d));
        // El icono del estado mide 14 dp (el vector tiene 24 dp de serie y ensancharía la píldora)
        Drawable dibujo = ContextCompat.getDrawable(c, icono).mutate();
        int lado = icono == R.drawable.ic_check ? (int) (14 * d) : dibujo.getIntrinsicWidth();
        dibujo.setBounds(0, 0, lado, lado);
        chip.setCompoundDrawablesRelative(dibujo, null, null, null);
        chip.setCompoundDrawablePadding((int) (6 * d));
    }

    @Override
    public int getItemCount()
    {
        return filas.size();
    }

    static class Vista extends RecyclerView.ViewHolder
    {
        final TextView tvTitulo, tvSubtitulo, tvDetalle, tvDerecha, tvPie, chipEstado, chipPie;
        final MaterialButton btn;

        Vista(View vista)
        {
            super(vista);
            tvTitulo = vista.findViewById(R.id.tvTitulo);
            tvSubtitulo = vista.findViewById(R.id.tvSubtitulo);
            tvDetalle = vista.findViewById(R.id.tvDetalle);
            tvDerecha = vista.findViewById(R.id.tvDerecha);
            tvPie = vista.findViewById(R.id.tvPie);
            chipEstado = vista.findViewById(R.id.chipEstado);
            chipPie = vista.findViewById(R.id.chipPie);
            btn = vista.findViewById(R.id.btnAccion);
        }
    }
}
