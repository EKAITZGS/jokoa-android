package com.jokoa.app.red;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jokoa.app.LoginActivity;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// Cola Volley única y peticiones a la API de Symfony (la misma BD para Android e Ionic)
public class Api
{
    public static final String BASE = "https://jokoa.duckdns.org/ws";

    private static RequestQueue cola;

    private static RequestQueue cola(Context c)
    {
        if (cola == null)
        {
            cola = Volley.newRequestQueue(c.getApplicationContext());
        }
        return cola;
    }

    // Si hay sesión iniciada, cabecera Authorization: Bearer <token>
    private static Map<String, String> cabeceras(Context c)
    {
        Map<String, String> cabeceras = new HashMap<>();
        String token = new Sesion(c).getToken();
        if (token != null)
        {
            cabeceras.put("Authorization", "Bearer " + token);
        }
        return cabeceras;
    }

    // Un 401 con sesión guardada significa token caducado (8 h): se cierra la sesión y se vuelve al login
    private static Response.ErrorListener sesionCaducada(Context c, Response.ErrorListener error)
    {
        return e ->
        {
            Sesion sesion = new Sesion(c);
            if (e.networkResponse != null && e.networkResponse.statusCode == 401 && sesion.iniciada())
            {
                sesion.cerrar();
                Toast.makeText(c, "Tu sesión ha caducado, inicia sesión otra vez", Toast.LENGTH_LONG).show();
                c.startActivity(new Intent(c, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
            else
            {
                error.onErrorResponse(e);
            }
        };
    }

    // Petición que devuelve un objeto JSON
    public static void enviar(Context c, int metodo, String ruta, JSONObject cuerpo,
                              Response.Listener<JSONObject> ok, Response.ErrorListener error)
    {
        final Context contexto = c;
        cola(c).add(new JsonObjectRequest(metodo, BASE + ruta, cuerpo, ok, sesionCaducada(c, error))
        {
            @Override
            public Map<String, String> getHeaders()
            {
                return cabeceras(contexto);
            }
        });
    }

    // GET que devuelve una lista JSON (moviles, videojuegos...)
    public static void lista(Context c, String ruta,
                             Response.Listener<JSONArray> ok, Response.ErrorListener error)
    {
        final Context contexto = c;
        cola(c).add(new JsonArrayRequest(Request.Method.GET, BASE + ruta, null, ok, sesionCaducada(c, error))
        {
            @Override
            public Map<String, String> getHeaders()
            {
                return cabeceras(contexto);
            }
        });
    }

    // Texto "mensaje" que devuelve la API en un error (409 sin stock, 403 contraseña...), o el de por defecto
    public static String mensaje(VolleyError error, String porDefecto)
    {
        if (error.networkResponse != null && error.networkResponse.data != null)
        {
            try
            {
                String m = new JSONObject(new String(error.networkResponse.data, StandardCharsets.UTF_8))
                        .optString("mensaje");
                if (!m.isEmpty())
                {
                    return m;
                }
            }
            catch (Exception e)
            {
                // el cuerpo no era JSON: se usa el texto por defecto
            }
        }
        return porDefecto;
    }

    public static int post()
    {
        return Request.Method.POST;
    }

    public static int put()
    {
        return Request.Method.PUT;
    }
}
