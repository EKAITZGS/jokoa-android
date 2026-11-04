package com.jokoa.app;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.jokoa.app.datos.Comparar;
import com.jokoa.app.datos.Producto;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Tabla con 2 o 3 productos lado a lado: precio, stock, datos clave y ficha técnica
public class ComparadorActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        List<Producto> items = Comparar.items();
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(ContextCompat.getColor(this, R.color.jokoa_fondo));
        raiz.setFitsSystemWindows(true);

        TextView titulo = celda("Comparador", true, R.color.jokoa_texto);
        titulo.setTextSize(24);
        titulo.setPadding(dp(16), dp(16), dp(16), dp(8));
        raiz.addView(titulo);

        TableLayout tabla = new TableLayout(this);
        tabla.setPadding(dp(8), 0, dp(8), dp(24));
        tabla.addView(fila("", items, p -> p.nombre, true));
        double minimo = Double.MAX_VALUE;
        for (Producto p : items) minimo = Math.min(minimo, p.precio);
        final double barato = minimo;
        tabla.addView(fila("Precio", items, p -> String.format(Locale.getDefault(), "%.2f €", p.precio)
                + (items.size() > 1 && p.precio == barato ? "\n(el más barato)" : ""), false));
        tabla.addView(fila("Datos", items, p -> p.detalle, false));
        tabla.addView(fila("Stock", items, p -> p.stock > 0 ? p.stock + " uds." : "Sin stock", false));

        Set<String> claves = new LinkedHashSet<>();
        for (Producto p : items) for (String[] d : p.ficha) claves.add(d[0]);
        for (String clave : claves)
        {
            tabla.addView(fila(clave, items, p ->
            {
                for (String[] d : p.ficha) if (d[0].equals(clave)) return d[1];
                return "-";
            }, false));
        }

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.addView(tabla);
        ScrollView vertical = new ScrollView(this);
        vertical.addView(horizontal);
        raiz.addView(vertical);
        setContentView(raiz);
    }

    private interface Dato { String de(Producto p); }

    private TableRow fila(String etiqueta, List<Producto> items, Dato dato, boolean negrita)
    {
        TableRow fila = new TableRow(this);
        fila.addView(celda(etiqueta, true, R.color.jokoa_texto_2));
        for (Producto p : items)
        {
            TextView t = celda(dato.de(p), negrita, R.color.jokoa_texto);
            t.setMaxWidth(dp(170));
            t.setMinWidth(dp(130));
            fila.addView(t);
        }
        return fila;
    }

    private TextView celda(String texto, boolean negrita, int color)
    {
        TextView t = new TextView(this);
        t.setText(texto);
        t.setTextColor(ContextCompat.getColor(this, color));
        t.setTextSize(14);
        t.setGravity(Gravity.START);
        t.setPadding(dp(8), dp(10), dp(8), dp(10));
        if (negrita) t.setTypeface(t.getTypeface(), Typeface.BOLD);
        return t;
    }

    private int dp(int v)
    {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
