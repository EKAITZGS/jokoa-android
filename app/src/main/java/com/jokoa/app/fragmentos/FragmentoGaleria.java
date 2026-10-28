package com.jokoa.app.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.jokoa.app.R;
import com.jokoa.app.adaptadores.GaleriaPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FragmentoGaleria extends Fragment
{
    private TabLayout tabs;
    private ViewPager2 viewPager;

    public FragmentoGaleria()
    {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View vista = inflater.inflate(R.layout.fragment_galeria, container, false);
        tabs = vista.findViewById(R.id.tabs);
        viewPager = vista.findViewById(R.id.viewPager);
        return vista;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        GaleriaPagerAdapter adapter = new GaleriaPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabs, viewPager, new TabLayoutMediator.TabConfigurationStrategy()
        {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position)
            {
                if (position == 0)
                {
                    tab.setText("Interior");
                }
                else
                {
                    tab.setText("Exterior");
                }
            }
        }).attach();
    }
}
