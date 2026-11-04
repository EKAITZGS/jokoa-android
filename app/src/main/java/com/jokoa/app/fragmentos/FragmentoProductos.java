package com.jokoa.app.fragmentos;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jokoa.app.DetalleActivity;
import com.jokoa.app.R;
import com.jokoa.app.adaptadores.ProductoAdapter;
import com.jokoa.app.datos.Favoritos;
import com.jokoa.app.datos.Producto;
import com.jokoa.app.red.Api;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// Tienda: Juegos, Móviles, Ordenadores, Tablets y Gaming (los tres últimos salen de /articulos y se filtran por categoría)
public class FragmentoProductos extends Fragment
{
    private static final String[] SECCIONES = {"Juegos", "Móviles", "Ordenadores", "Tablets", "Gaming"};
    // Categorías de artículo de cada sección (juegos y móviles no tienen)
    private static final String[][] CATEGORIAS = {null, null,
            {"portatil", "sobremesa", "pc_gaming"}, {"tablet"},
            {"consola", "mando", "auriculares", "teclado", "raton", "monitor", "silla"}};
    private static final String[][] NOMBRES_CATEGORIA = {
            {"portatil", "Portátiles"}, {"sobremesa", "Sobremesa"}, {"pc_gaming", "PC gaming Jokoa"}, {"tablet", "Tablets"},
            {"consola", "Consolas"}, {"mando", "Mandos"}, {"auriculares", "Auriculares"}, {"teclado", "Teclados"},
            {"raton", "Ratones"}, {"monitor", "Monitores"}, {"silla", "Sillas gaming"}};

    // Lo ya descargado de cada fuente: no se vuelve a pedir al cambiar de sección
    private List<Producto> juegos, moviles, articulos;
    private final List<Producto> productos = new ArrayList<>();
    private int seccion = 0;
    private String categoria = ""; // "" = todas las de la sección
    private String texto = "";
    private int orden = 0; // 0 nombre, 1 precio ascendente, 2 precio descendente
    private boolean soloStock = false;
    private boolean soloFavoritos = false;
    private LinearLayout esqueleto, bloqueError;
    private RecyclerView lista;
    private View progreso, scrollCategoria;
    private ChipGroup chipsCategoria;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup contenedor, @Nullable Bundle estado)
    {
        View vista = inflater.inflate(R.layout.fragment_productos, contenedor, false);
        ((TextView) vista.findViewById(R.id.tvTitulo)).setText("Tienda");
        esqueleto = vista.findViewById(R.id.esqueleto);
        bloqueError = vista.findViewById(R.id.bloqueError);
        lista = vista.findViewById(R.id.lista);
        progreso = vista.findViewById(R.id.progreso);
        scrollCategoria = vista.findViewById(R.id.scrollCategoria);
        chipsCategoria = vista.findViewById(R.id.chipsCategoria);
        lista.setLayoutManager(new LinearLayoutManager(getContext()));
        lista.setAdapter(new ProductoAdapter(productos, (producto, imagen) ->
        {
            // La foto de la tarjeta crece hasta la ficha (transición compartida)
            Intent intent = new Intent(requireContext(), DetalleActivity.class);
            intent.putExtra("producto", producto);
            ActivityOptions opciones = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), imagen, imagen.getTransitionName());
            startActivity(intent, opciones.toBundle());
        }));
        vista.findViewById(R.id.btnReintentar).setOnClickListener(v -> cargar());

        ((TextView) vista.findViewById(R.id.etBuscar)).addTextChangedListener(new android.text.TextWatcher()
        {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }

            @Override
            public void afterTextChanged(android.text.Editable s)
            {
                texto = s.toString().trim().toLowerCase(java.util.Locale.getDefault());
                if (fuente() != null)
                {
                    mostrar();
                }
            }
        });
        Chip chipOrden = vista.findViewById(R.id.chipOrden);
        chipOrden.setOnClickListener(v ->
        {
            orden = (orden + 1) % 3;
            chipOrden.setText(orden == 0 ? "Orden: nombre" : orden == 1 ? "Precio: menor a mayor" : "Precio: mayor a menor");
            mostrar();
        });
        ((Chip) vista.findViewById(R.id.chipFavoritos)).setOnCheckedChangeListener((b, marcado) ->
        {
            soloFavoritos = marcado;
            if (fuente() != null)
            {
                mostrar();
            }
        });
        ((Chip) vista.findViewById(R.id.chipStock)).setOnCheckedChangeListener((b, marcado) ->
        {
            soloStock = marcado;
            if (fuente() != null)
            {
                mostrar();
            }
        });

        ChipGroup chipsSeccion = vista.findViewById(R.id.chipsSeccion);
        for (int i = 0; i < SECCIONES.length; i++)
        {
            Chip chip = crearChip(inflater, chipsSeccion, SECCIONES[i]);
            chip.setChecked(i == seccion);
            final int indice = i;
            chip.setOnClickListener(v ->
            {
                seccion = indice;
                categoria = "";
                pintarCategorias();
                cargar();
            });
        }
        pintarCategorias();

        // Las tarjetas grises de espera laten mientras llegan los datos
        esqueleto.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.pulso));
        cargar();
        return vista;
    }

    // Al volver de la ficha puede haber cambiado algún corazón
    @Override
    public void onResume()
    {
        super.onResume();
        Favoritos.cargar(requireContext(), () ->
        {
            if (isAdded() && fuente() != null && soloFavoritos)
            {
                mostrar();
            }
        });
    }

    private Chip crearChip(LayoutInflater inflater, ChipGroup grupo, String texto)
    {
        Chip chip = (Chip) inflater.inflate(R.layout.item_chip_filtro, grupo, false);
        chip.setText(texto);
        grupo.addView(chip);
        return chip;
    }

    // Segunda fila de chips (Todo + categorías de la sección) solo si la sección tiene más de una categoría
    private void pintarCategorias()
    {
        chipsCategoria.removeAllViews();
        String[] categorias = CATEGORIAS[seccion];
        scrollCategoria.setVisibility(categorias != null && categorias.length > 1 ? View.VISIBLE : View.GONE);
        if (categorias == null || categorias.length < 2)
        {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        Chip todo = crearChip(inflater, chipsCategoria, "Todo");
        todo.setChecked(true);
        todo.setOnClickListener(v -> elegirCategoria(""));
        for (String c : categorias)
        {
            Chip chip = crearChip(inflater, chipsCategoria, nombreCategoria(c));
            chip.setOnClickListener(v -> elegirCategoria(c));
        }
    }

    private void elegirCategoria(String c)
    {
        categoria = c;
        mostrar();
    }

    private static String nombreCategoria(String c)
    {
        for (String[] n : NOMBRES_CATEGORIA)
        {
            if (n[0].equals(c))
            {
                return n[1];
            }
        }
        return c;
    }

    // Qué lista ya descargada alimenta la sección actual (null si falta por pedir)
    private List<Producto> fuente()
    {
        return seccion == 0 ? juegos : seccion == 1 ? moviles : articulos;
    }

    private void cargar()
    {
        bloqueError.setVisibility(View.GONE);
        if (fuente() != null)
        {
            mostrar();
            return;
        }
        esqueleto.setVisibility(View.VISIBLE);
        progreso.setVisibility(View.VISIBLE);
        final int pedida = seccion;
        final String tipo = pedida == 0 ? Producto.VIDEOJUEGO : pedida == 1 ? Producto.MOVIL : Producto.ARTICULO;
        Api.lista(requireContext(), Producto.ruta(tipo),
                respuesta ->
                {
                    if (!isAdded())
                    {
                        return;
                    }
                    List<Producto> recibidos = new ArrayList<>();
                    for (int i = 0; i < respuesta.length(); i++)
                    {
                        JSONObject j = respuesta.optJSONObject(i);
                        if (j != null)
                        {
                            recibidos.add(new Producto(j, tipo));
                        }
                    }
                    if (pedida == 0)
                    {
                        juegos = recibidos;
                    }
                    else if (pedida == 1)
                    {
                        moviles = recibidos;
                    }
                    else
                    {
                        articulos = recibidos;
                    }
                    // Si el usuario cambió de sección mientras cargaba, esa respuesta solo queda guardada
                    if (pedida == seccion || (pedida >= 2 && seccion >= 2))
                    {
                        mostrar();
                    }
                },
                error ->
                {
                    if (!isAdded())
                    {
                        return;
                    }
                    esqueleto.clearAnimation();
                    esqueleto.setVisibility(View.GONE);
                    progreso.setVisibility(View.GONE);
                    bloqueError.setVisibility(View.VISIBLE);
                });
    }

    // Pinta la sección y la categoría elegidas con lo ya descargado
    private void mostrar()
    {
        productos.clear();
        List<String> permitidas = CATEGORIAS[seccion] == null ? null : java.util.Arrays.asList(CATEGORIAS[seccion]);
        for (Producto p : fuente())
        {
            boolean deLaSeccion = permitidas == null || (permitidas.contains(p.categoria) && (categoria.isEmpty() || categoria.equals(p.categoria)));
            boolean coincide = texto.isEmpty() || (p.nombre + " " + p.detalle).toLowerCase(java.util.Locale.getDefault()).contains(texto);
            if (deLaSeccion && coincide && (!soloStock || p.stock > 0) && (!soloFavoritos || Favoritos.es(p.id)))
            {
                productos.add(p);
            }
        }
        productos.sort(orden == 1 ? (a, b) -> Double.compare(a.precio, b.precio)
                : orden == 2 ? (a, b) -> Double.compare(b.precio, a.precio)
                : (a, b) -> a.nombre.compareToIgnoreCase(b.nombre));
        esqueleto.clearAnimation();
        esqueleto.setVisibility(View.GONE);
        progreso.setVisibility(View.GONE);
        lista.getAdapter().notifyDataSetChanged();
        lista.scheduleLayoutAnimation();
    }
}
