package com.jokoa.app.adaptadores;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jokoa.app.fragmentos.FragmentoFotos;

public class GaleriaPagerAdapter extends FragmentStateAdapter
{
    public GaleriaPagerAdapter(@NonNull Fragment fragment)
    {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position)
    {
        Bundle bundle = new Bundle();
        if (position == 0)
        {
            bundle.putInt(FragmentoFotos.ARG_TIPO, FragmentoFotos.INTERIOR);
        }
        else
        {
            bundle.putInt(FragmentoFotos.ARG_TIPO, FragmentoFotos.EXTERIOR);
        }
        return FragmentoFotos.newInstance(bundle);
    }

    @Override
    public int getItemCount()
    {
        return 2; // Interior y Exterior
    }
}
