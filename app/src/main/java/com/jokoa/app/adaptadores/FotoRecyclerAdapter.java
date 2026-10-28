package com.jokoa.app.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jokoa.app.InterfacesDam.IControlFotos;
import com.jokoa.app.R;
import com.jokoa.app.datos.FotoAlmi;

import java.util.ArrayList;
import com.bumptech.glide.Glide;

public class FotoRecyclerAdapter extends RecyclerView.Adapter<FotoRecyclerAdapter.FotoViewHolder>
{
    private final Context contexto;
    private final ArrayList<FotoAlmi> fotos;
    private final IControlFotos control;

    public FotoRecyclerAdapter(Context contexto, ArrayList<FotoAlmi> fotos, IControlFotos control)
    {
        this.contexto = contexto;
        this.fotos = fotos;
        this.control = control;
    }

    // Se crea la vista y se guardan sus referencias en el ViewHolder (pocas veces)
    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View vista = LayoutInflater.from(contexto).inflate(R.layout.item_foto, parent, false);
        return new FotoViewHolder(vista);
    }

    // Se rellena una vista reutilizada con los datos de esa posición (al hacer scroll)
    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position)
    {
        final FotoAlmi foto = fotos.get(position);

        // La imagen se carga con Glide
        Glide.with(contexto)
                .load(foto.getImagen())
                .into(holder.imagen);
        holder.texto.setText(foto.getNombre());

        // Click en la foto: se avisa al fragmento a través de la interfaz
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                control.fotoPulsada(foto);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return fotos.size();
    }

    // El ViewHolder cachea el findViewById: una sola vez por vista
    static class FotoViewHolder extends RecyclerView.ViewHolder
    {
        final ImageView imagen;
        final TextView texto;

        FotoViewHolder(@NonNull View itemView)
        {
            super(itemView);
            imagen = itemView.findViewById(R.id.ivFoto);
            texto = itemView.findViewById(R.id.tvFoto);
        }
    }
}
