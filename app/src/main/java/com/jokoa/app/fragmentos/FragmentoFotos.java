package com.jokoa.app.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jokoa.app.InterfacesDam.IControlFotos;
import com.jokoa.app.R;
import com.jokoa.app.adaptadores.FotoRecyclerAdapter;
import com.jokoa.app.datos.FotoAlmi;

import java.util.ArrayList;
import android.content.res.TypedArray;

// Fragmento reutilizable: sirve para la pestaña Interior y para la Exterior (según el Bundle)
public class FragmentoFotos extends Fragment implements IControlFotos
{
    public static final String ARG_TIPO = "tipo";
    public static final int INTERIOR = 0;
    public static final int EXTERIOR = 1;

    private RecyclerView recyclerFotos;
    private final ArrayList<FotoAlmi> fotos = new ArrayList<>();

    public FragmentoFotos()
    {
        super();
    }

    // Paso de parámetros: crea la instancia y le fija los argumentos (Bundle)
    public static FragmentoFotos newInstance(Bundle argumentos)
    {
        FragmentoFotos fragmentoFotos = new FragmentoFotos();
        if (argumentos != null)
        {
            fragmentoFotos.setArguments(argumentos);
        }
        return fragmentoFotos;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View vista = inflater.inflate(R.layout.fragment_fotos, container, false);
        recyclerFotos = vista.findViewById(R.id.recyclerFotos);
        return vista;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        recyclerFotos.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rellenarFotos();

        FotoRecyclerAdapter adapter = new FotoRecyclerAdapter(getContext(), fotos, this);
        recyclerFotos.setAdapter(adapter);
    }

    private void rellenarFotos()
    {
        fotos.clear();
        int tipo = INTERIOR;
        if (getArguments() != null)
        {
            tipo = getArguments().getInt(ARG_TIPO, INTERIOR);
        }

        TypedArray imagenes;
        String[] nombres;
        if (tipo == INTERIOR)
        {
            imagenes = getResources().obtainTypedArray(R.array.fotos_interior);
            nombres = getResources().getStringArray(R.array.nombres_interior);
        }
        else
        {
            imagenes = getResources().obtainTypedArray(R.array.fotos_exterior);
            nombres = getResources().getStringArray(R.array.nombres_exterior);
        }

        for (int i = 0; i < imagenes.length(); i++)
        {
            fotos.add(new FotoAlmi(nombres[i], imagenes.getResourceId(i, -1)));
        }
        imagenes.recycle();
    }

    @Override
    public void fotoPulsada(FotoAlmi foto)
    {
        Toast.makeText(getContext(), foto.getNombre(), Toast.LENGTH_SHORT).show();

        Bundle bundle = new Bundle();
        bundle.putInt("imagen", foto.getImagen());
        FotoDialogFrag dialogo = FotoDialogFrag.newInstance(bundle);
        dialogo.show(getActivity().getSupportFragmentManager(), "foto");
    }
}
