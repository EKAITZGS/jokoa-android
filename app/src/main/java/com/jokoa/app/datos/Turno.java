package com.jokoa.app.datos;

import java.util.ArrayList;
import java.util.List;

// Un mensaje de la conversación con el asesor; los del asesor pueden traer productos recomendados
public class Turno
{
    public static class Tarjeta
    {
        public final int id;
        public final String nombre, tipo, imagen;
        public final double precio;

        public Tarjeta(int id, String nombre, String tipo, double precio, String imagen)
        {
            this.id = id;
            this.nombre = nombre;
            this.tipo = tipo;
            this.precio = precio;
            this.imagen = imagen;
        }
    }

    public final boolean delUsuario;
    public final String texto;
    public final List<Tarjeta> productos = new ArrayList<>();

    public Turno(boolean delUsuario, String texto)
    {
        this.delUsuario = delUsuario;
        this.texto = texto;
    }
}
