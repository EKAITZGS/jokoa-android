package com.jokoa.app.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jokoa.app.R;

public class FragmentoInicio extends Fragment
{
    private ImageView ivLogo;
    private ListView lvCursos;

    public FragmentoInicio()
    {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View vista = inflater.inflate(R.layout.fragment_inicio, container, false);
        ivLogo = vista.findViewById(R.id.ivLogo);
        lvCursos = vista.findViewById(R.id.lvCursos);
        return vista;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.cursos, android.R.layout.simple_list_item_1);
        lvCursos.setAdapter(adapter);
    }
}
