package com.jokoa.app.datos;

import java.util.ArrayList;
import java.util.List;

// Productos elegidos para comparar (hasta 3), compartidos por la ficha y la pantalla del comparador
public final class Comparar
{
    public static final int MAX = 3;
    private static final List<Producto> ITEMS = new ArrayList<>();

    private Comparar() { }

    public static List<Producto> items()
    {
        return ITEMS;
    }

    public static boolean es(int id)
    {
        return indice(id) >= 0;
    }

    // Devuelve false si ya hay 3 y no se pudo añadir
    public static boolean alternar(Producto p)
    {
        int i = indice(p.id);
        if (i >= 0)
        {
            ITEMS.remove(i);
            return true;
        }
        if (ITEMS.size() >= MAX)
        {
            return false;
        }
        ITEMS.add(p);
        return true;
    }

    private static int indice(int id)
    {
        for (int i = 0; i < ITEMS.size(); i++)
        {
            if (ITEMS.get(i).id == id) return i;
        }
        return -1;
    }
}
