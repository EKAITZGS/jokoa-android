package com.jokoa.app.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jokoa.app.R;
import com.bumptech.glide.Glide;

public class FotoDialogFrag extends DialogFragment
{
    private ImageView ivFoto;

    public FotoDialogFrag()
    {
        super();
    }

    // Paso de parámetros: crea la instancia y le fija los argumentos (Bundle)
    public static FotoDialogFrag newInstance(Bundle argumentos)
    {
        FotoDialogFrag dialogo = new FotoDialogFrag();
        if (argumentos != null)
        {
            dialogo.setArguments(argumentos);
        }
        return dialogo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View vista = inflater.inflate(R.layout.dialog_foto, container, false);
        ivFoto = vista.findViewById(R.id.ivDialogFoto);
        return vista;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // Se carga la foto pulsada (llega en el Bundle)
        Bundle argumentos = getArguments();
        if (argumentos != null && argumentos.containsKey("imagen"))
        {
            Glide.with(getContext())
                    .load(argumentos.getInt("imagen"))
                    .into(ivFoto);
        }

        // Al hacer click en la foto se cierra el Dialog
        ivFoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });
    }
}
