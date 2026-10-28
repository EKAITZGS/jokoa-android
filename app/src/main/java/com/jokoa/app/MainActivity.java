package com.jokoa.app;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.jokoa.app.fragmentos.FragmentoPedidos;
import com.jokoa.app.fragmentos.FragmentoPerfil;
import com.jokoa.app.fragmentos.FragmentoProductos;
import com.jokoa.app.fragmentos.FragmentoTiendas;

public class MainActivity extends AppCompatActivity
{
    private BottomNavigationView menuInferior;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        menuInferior = findViewById(R.id.menuInferior);

        // Se carga el fragmento de inicio solo la primera vez (evita duplicarlo al recrear la actividad)
        if (savedInstanceState == null)
        {
            cargarFragmento(FragmentoProductos.nuevo("/videojuegos", "Videojuegos"));
        }

        // Atrás: desde Móviles o Perfil vuelve a Juegos; desde Juegos sale de la app
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                if (menuInferior.getSelectedItemId() != R.id.nav_juegos)
                {
                    menuInferior.setSelectedItemId(R.id.nav_juegos);
                }
                else
                {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        menuInferior.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                int id = item.getItemId();
                if (id == R.id.nav_juegos)
                {
                    cargarFragmento(FragmentoProductos.nuevo("/videojuegos", "Videojuegos"));
                    return true;
                }
                else if (id == R.id.nav_moviles)
                {
                    cargarFragmento(FragmentoProductos.nuevo("/moviles", "Móviles"));
                    return true;
                }
                else if (id == R.id.nav_tiendas)
                {
                    cargarFragmento(new FragmentoTiendas());
                    return true;
                }
                else if (id == R.id.nav_pedidos)
                {
                    cargarFragmento(new FragmentoPedidos());
                    return true;
                }
                else if (id == R.id.nav_perfil)
                {
                    cargarFragmento(new FragmentoPerfil());
                    return true;
                }
                return false;
            }
        });
    }

    // Sustituye el fragmento del contenedor con animación
    private void cargarFragmento(Fragment fragmento)
    {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.entrada_fragmento, R.anim.salida_fragmento)
                .replace(R.id.contenedor, fragmento)
                .commit();
    }
}
