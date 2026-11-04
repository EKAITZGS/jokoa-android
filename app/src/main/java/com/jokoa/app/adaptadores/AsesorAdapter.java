package com.jokoa.app.adaptadores;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jokoa.app.R;
import com.jokoa.app.datos.Turno;

import java.util.List;
import java.util.Locale;

// Burbujas de la conversación con el asesor; si está pensando se añade al final una burbuja de espera
public class AsesorAdapter extends RecyclerView.Adapter<AsesorAdapter.Holder>
{
    public interface OnProductoClick
    {
        void alPulsar(Turno.Tarjeta tarjeta);
    }

    private final List<Turno> turnos;
    private final OnProductoClick oyente;
    private boolean pensando;

    public AsesorAdapter(List<Turno> turnos, OnProductoClick oyente)
    {
        this.turnos = turnos;
        this.oyente = oyente;
    }

    public void setPensando(boolean pensando)
    {
        this.pensando = pensando;
        notifyDataSetChanged();
    }

    static class Holder extends RecyclerView.ViewHolder
    {
        final TextView tvTexto;
        final LinearLayout tarjetas;

        Holder(View vista)
        {
            super(vista);
            tvTexto = vista.findViewById(R.id.tvTexto);
            tarjetas = vista.findViewById(R.id.tarjetas);
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup padre, int tipo)
    {
        return new Holder(LayoutInflater.from(padre.getContext()).inflate(R.layout.item_burbuja, padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int posicion)
    {
        boolean espera = posicion == turnos.size();
        Turno turno = espera ? null : turnos.get(posicion);
        boolean mio = turno != null && turno.delUsuario;

        h.tvTexto.setText(espera ? "Buscando en el catálogo…" : turno.texto);
        h.tvTexto.setBackgroundResource(mio ? R.drawable.bg_burbuja_yo : R.drawable.bg_burbuja_bot);
        h.tvTexto.setTextColor(ContextCompat.getColor(h.itemView.getContext(), mio ? android.R.color.white : R.color.jokoa_texto));
        ((LinearLayout.LayoutParams) h.tvTexto.getLayoutParams()).gravity = mio ? Gravity.END : Gravity.START;

        h.tarjetas.removeAllViews();
        if (turno != null)
        {
            for (Turno.Tarjeta t : turno.productos)
            {
                View vista = LayoutInflater.from(h.itemView.getContext()).inflate(R.layout.item_tarjeta_asesor, h.tarjetas, false);
                ((TextView) vista.findViewById(R.id.tvNombre)).setText(t.nombre);
                ((TextView) vista.findViewById(R.id.tvPrecio)).setText(t.precio == 0 ? "Gratis" : String.format(Locale.getDefault(), "%.2f €", t.precio));
                Glide.with(vista).load(t.imagen)
                        .placeholder(R.drawable.foto_placeholder)
                        .error(R.drawable.foto_placeholder)
                        .into((ImageView) vista.findViewById(R.id.ivImagen));
                vista.setOnClickListener(v -> oyente.alPulsar(t));
                h.tarjetas.addView(vista);
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return turnos.size() + (pensando ? 1 : 0);
    }
}
