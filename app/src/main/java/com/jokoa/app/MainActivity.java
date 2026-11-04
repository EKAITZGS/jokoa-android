package com.jokoa.app;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

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
import com.jokoa.app.fragmentos.FragmentoAsesor;
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
        menuInferior = findViewById(R.id.menuInferior);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Con el teclado abierto (chat del asesor) el contenido sube sobre él y la barra inferior se oculta
            // Solo cuenta como teclado si de verdad ocupa pantalla (con teclado físico puede haber foco sin teclado)
            int alturaTeclado = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            boolean teclado = insets.isVisible(WindowInsetsCompat.Type.ime()) && alturaTeclado > systemBars.bottom;
            int abajo = teclado ? alturaTeclado : systemBars.bottom;
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, abajo);
            menuInferior.setVisibility(teclado ? View.GONE : View.VISIBLE);
            return insets;
        });

        // Se carga el fragmento de inicio solo la primera vez (evita duplicarlo al recrear la actividad)
        if (savedInstanceState == null)
        {
            cargarFragmento(new FragmentoProductos());
        }

        // Atrás: desde cualquier pestaña vuelve a la Tienda; desde la Tienda sale de la app
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                if (menuInferior.getSelectedItemId() != R.id.nav_tienda)
                {
                    menuInferior.setSelectedItemId(R.id.nav_tienda);
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
                if (id == R.id.nav_tienda)
                {
                    cargarFragmento(new FragmentoProductos());
                    return true;
                }
                else if (id == R.id.nav_asesor)
                {
                    cargarFragmento(new FragmentoAsesor());
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
