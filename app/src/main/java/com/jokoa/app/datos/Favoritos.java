package com.jokoa.app.datos;

import android.content.Context;
import android.widget.Toast;

import com.jokoa.app.red.Api;

import java.util.HashSet;
import java.util.Set;

// Ids de los productos con corazón del usuario, compartidos por la tienda y la ficha
public final class Favoritos
{
    private static final Set<Integer> IDS = new HashSet<>();

    private Favoritos() { }

    public static boolean es(int id)
    {
        return IDS.contains(id);
    }

    public static void cargar(Context c, Runnable alTerminar)
    {
        Api.lista(c, "/favoritos", respuesta ->
        {
            IDS.clear();
            for (int i = 0; i < respuesta.length(); i++)
            {
                IDS.add(respuesta.optInt(i));
            }
            if (alTerminar != null)
            {
                alTerminar.run();
            }
        }, error -> { });
    }

    // El corazón cambia al instante y se deshace si el servidor falla
    public static void alternar(Context c, int id, Runnable alCambiar)
    {
        boolean era = IDS.contains(id);
        poner(id, !era);
        alCambiar.run();
        Api.enviar(c, era ? Api.delete() : Api.put(), "/favoritos/" + id, new org.json.JSONObject(),
                respuesta -> { },
                error ->
                {
                    poner(id, era);
                    alCambiar.run();
                    Toast.makeText(c, Api.mensaje(error, "No se pudo actualizar tus favoritos"), Toast.LENGTH_SHORT).show();
                });
    }

    private static void poner(int id, boolean marcado)
    {
        if (marcado)
        {
            IDS.add(id);
        }
        else
        {
            IDS.remove(id);
        }
    }
}
