package com.jokoa.app.datos;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

// Producto de la tienda (videojuego, móvil o artículo: ordenador, tablet, gaming) tal como lo devuelve la API
public class Producto implements Serializable
{
    public static final String VIDEOJUEGO = "videojuego";
    public static final String MOVIL = "movil";
    public static final String ARTICULO = "articulo";

    // Rutas de la API por tipo (para abrir la ficha desde el asesor)
    public static String ruta(String tipo)
    {
        return tipo.equals(VIDEOJUEGO) ? "/videojuegos" : tipo.equals(MOVIL) ? "/moviles" : "/articulos";
    }

    public final int id;
    public final String nombre;
    public final String descripcion;
    public final double precio;
    public final int stock;
    public final String imagen;   // primera imagen, o vacío
    public final String detalle;  // línea extra: marca/RAM, plataforma/PEGI o marca y datos clave
    public final String tipo;
    public final boolean esJuego; // los juegos llevan portada vertical; el resto, foto cuadrada que se ve entera
    public final String categoria; // solo artículos: portatil, tablet, consola...
    public final double alquilerDia; // solo videojuegos
    // Datos para las píldoras de la ficha (las vacías no se pintan)
    public final String etiqueta1, etiqueta2, etiqueta3;
    // Garantía ampliada disponible (móviles, tablets y ordenadores): meses y precio por unidad
    public final int[] garantiaMeses;
    public final double[] garantiaPrecio;
    // Ficha técnica de los artículos: pares [nombre, valor]
    public final ArrayList<String[]> ficha = new ArrayList<>();

    // Qué datos se resumen en las píldoras de cada categoría (igual que la web)
    private static final String[][] RESUMEN = {
            {"portatil", "procesador", "ram_gb"}, {"sobremesa", "procesador", "ram_gb"}, {"pc_gaming", "grafica", "ram_gb"},
            {"tablet", "pantalla", "almacenamiento_gb"}, {"consola", "almacenamiento", "resolucion"}, {"mando", "compatible"},
            {"auriculares", "tipo", "compatible"}, {"teclado", "formato", "tipo"}, {"raton", "conexion", "peso_g"},
            {"monitor", "tamano", "frecuencia_hz"}, {"silla", "material", "reposabrazos"}};

    private static final String[][] NOMBRES = {
            {"ram_gb", "RAM"}, {"almacenamiento_gb", "Almacenamiento"}, {"peso_kg", "Peso"}, {"peso_g", "Peso"}, {"peso_max_kg", "Peso máximo"},
            {"frecuencia_hz", "Frecuencia"}, {"dpi_max", "DPI máximo"}, {"tamano", "Tamaño"}, {"grafica", "Gráfica"}, {"bateria", "Batería"},
            {"conexion", "Conexión"}, {"microfono", "Micrófono"}, {"iluminacion", "Iluminación"}, {"resolucion", "Resolución"}, {"lapiz", "Lápiz"},
            {"ideal_para", "Ideal para"}, {"cinco_g", "5G"}, {"camara", "Cámara"}, {"subgenero", "Subgénero"}};

    private static final String[][] UNIDADES = {
            {"ram_gb", " GB"}, {"almacenamiento_gb", " GB"}, {"peso_kg", " kg"}, {"peso_g", " g"}, {"peso_max_kg", " kg"}, {"frecuencia_hz", " Hz"}};

    public Producto(JSONObject j, String tipo)
    {
        this.tipo = tipo;
        esJuego = tipo.equals(VIDEOJUEGO);
        id = j.optInt("id");
        nombre = j.optString("nombre");
        descripcion = j.optString("descripcion");
        precio = j.optDouble("precio");
        stock = j.optInt("stock");
        alquilerDia = j.optDouble("precio_alquiler_dia", 0);
        categoria = j.optString("categoria", "");
        JSONArray imagenes = j.optJSONArray("imagenes");
        imagen = (imagenes != null && imagenes.length() > 0) ? imagenes.optString(0) : "";

        JSONArray garantias = j.optJSONArray("garantias");
        int n = garantias == null ? 0 : garantias.length();
        garantiaMeses = new int[n];
        garantiaPrecio = new double[n];
        for (int i = 0; i < n; i++)
        {
            JSONObject g = garantias.optJSONObject(i);
            garantiaMeses[i] = g.optInt("meses");
            garantiaPrecio[i] = g.optDouble("precio");
        }

        if (tipo.equals(MOVIL))
        {
            etiqueta1 = j.optString("marca");
            etiqueta2 = j.optInt("almacenamiento_gb") + " GB";
            etiqueta3 = j.optInt("ram_gb") + " GB RAM";
        }
        else if (tipo.equals(ARTICULO))
        {
            JSONObject car = j.optJSONObject("caracteristicas");
            String[] datos = {"", ""};
            for (String[] fila : RESUMEN)
            {
                if (fila[0].equals(categoria) && car != null)
                {
                    for (int k = 1; k < fila.length && k <= 2; k++)
                    {
                        if (car.has(fila[k]))
                        {
                            datos[k - 1] = valor(fila[k], car.opt(fila[k]));
                        }
                    }
                }
            }
            etiqueta1 = j.optString("marca");
            etiqueta2 = datos[0];
            etiqueta3 = datos[1];
            for (Iterator<String> it = car == null ? new ArrayList<String>().iterator() : car.keys(); it.hasNext(); )
            {
                String clave = it.next();
                if (!clave.equals("etiquetas"))
                {
                    ficha.add(new String[]{nombreDato(clave), valor(clave, car.opt(clave))});
                }
            }
        }
        else
        {
            etiqueta1 = j.optString("plataforma");
            etiqueta2 = j.optString("genero");
            etiqueta3 = "PEGI " + j.optInt("pegi");
        }
        StringBuilder d = new StringBuilder();
        for (String e : new String[]{etiqueta1, etiqueta2, etiqueta3})
        {
            if (!e.isEmpty())
            {
                d.append(d.length() > 0 ? " · " : "").append(e);
            }
        }
        detalle = d.toString();
    }

    private static String nombreDato(String clave)
    {
        for (String[] f : NOMBRES)
        {
            if (f[0].equals(clave))
            {
                return f[1];
            }
        }
        return Character.toUpperCase(clave.charAt(0)) + clave.substring(1).replace('_', ' ');
    }

    private static String valor(String clave, Object v)
    {
        if (v instanceof Boolean)
        {
            return (Boolean) v ? "Sí" : "No";
        }
        if (v instanceof JSONArray)
        {
            JSONArray a = (JSONArray) v;
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < a.length(); i++)
            {
                s.append(i > 0 ? ", " : "").append(a.optString(i));
            }
            return s.toString();
        }
        String texto = String.valueOf(v);
        if (v instanceof Number)
        {
            for (String[] u : UNIDADES)
            {
                if (u[0].equals(clave))
                {
                    return texto + u[1];
                }
            }
        }
        return texto;
    }
}
