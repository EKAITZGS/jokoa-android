package com.jokoa.app.datos;

public class FotoAlmi
{
    private String nombre;
    private int imagen; // Apuntador a un drawable (R.drawable.xxx)

    public FotoAlmi(String nombre, int imagen)
    {
        this.nombre = nombre;
        this.imagen = imagen;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getImagen() { return imagen; }
    public void setImagen(int imagen) { this.imagen = imagen; }
}
