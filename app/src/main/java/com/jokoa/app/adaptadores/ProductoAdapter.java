package com.jokoa.app.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jokoa.app.R;
import com.jokoa.app.datos.Producto;

import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.Holder>
{
    public interface OnProductoClick
    {
        void alPulsar(Producto producto, ImageView imagen);
    }

    private final List<Producto> productos;
    private final OnProductoClick oyente;

    public ProductoAdapter(List<Producto> productos, OnProductoClick oyente)
    {
        this.productos = productos;
        this.oyente = oyente;
    }

    static class Holder extends RecyclerView.ViewHolder
    {
        final ImageView ivImagen;
        final TextView tvNombre, tvDetalle, tvPrecio;

        Holder(View vista)
        {
            super(vista);
            ivImagen = vista.findViewById(R.id.ivImagen);
            tvNombre = vista.findViewById(R.id.tvNombre);
            tvDetalle = vista.findViewById(R.id.tvDetalle);
            tvPrecio = vista.findViewById(R.id.tvPrecio);
        }
    }

    // 0 = videojuego (portada vertical), 1 = móvil (foto cuadrada que se ve entera)
    @Override
    public int getItemViewType(int posicion)
    {
        return productos.get(posicion).esMovil ? 1 : 0;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup padre, int tipo)
    {
        int layout = tipo == 1 ? R.layout.item_producto_movil : R.layout.item_producto;
        return new Holder(LayoutInflater.from(padre.getContext()).inflate(layout, padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int posicion)
    {
        Producto p = productos.get(posicion);
        h.tvNombre.setText(p.nombre);
        h.tvDetalle.setText(p.detalle);
        h.tvPrecio.setText(String.format(Locale.getDefault(), "%.2f €", p.precio));
        // Glide carga en segundo plano, con imagen de espera y de error
        Glide.with(h.itemView).load(p.imagen)
                .placeholder(R.drawable.foto_placeholder)
                .error(R.drawable.foto_placeholder)
                .into(h.ivImagen);
        h.ivImagen.setTransitionName("imagen_" + p.id);
        h.itemView.setOnClickListener(v -> oyente.alPulsar(p, h.ivImagen));
    }

    @Override
    public int getItemCount()
    {
        return productos.size();
    }
}
