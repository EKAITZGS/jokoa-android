package com.jokoa.app.datos;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

// Producto de la tienda (móvil o videojuego) tal como lo devuelve la API
public class Producto implements Serializable
{
    public final int id;
    public final String nombre;
    public final String descripcion;
    public final double precio;
    public final int stock;
    public final String imagen;   // primera imagen, o vacío
    public final String detalle;  // línea extra: marca/RAM o plataforma/PEGI
    public final boolean esMovil;
    public final double alquilerDia; // solo videojuegos

    public Producto(JSONObject j, boolean esMovil)
    {
        this.esMovil = esMovil;
        id = j.optInt("id");
        nombre = j.optString("nombre");
        descripcion = j.optString("descripcion");
        precio = j.optDouble("precio");
        stock = j.optInt("stock");
        alquilerDia = j.optDouble("precio_alquiler_dia", 0);
        JSONArray imagenes = j.optJSONArray("imagenes");
        imagen = (imagenes != null && imagenes.length() > 0) ? imagenes.optString(0) : "";
        if (esMovil)
        {
            detalle = j.optString("marca") + " · " + j.optInt("almacenamiento_gb") + " GB · "
                    + j.optInt("ram_gb") + " GB RAM";
        }
        else
        {
            detalle = j.optString("plataforma") + " · " + j.optString("genero")
                    + " · PEGI " + j.optInt("pegi");
        }
    }
}
