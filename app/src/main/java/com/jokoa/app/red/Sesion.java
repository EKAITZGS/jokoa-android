package com.jokoa.app.red;

import android.content.Context;
import android.content.SharedPreferences;

// Guarda el token JWT en SharedPreferences (sesión del usuario)
public class Sesion
{
    private final SharedPreferences prefs;

    public Sesion(Context contexto)
    {
        prefs = contexto.getSharedPreferences("sesion", Context.MODE_PRIVATE);
    }

    public void guardar(String token, String email)
    {
        prefs.edit().putString("token", token).putString("email", email).apply();
    }

    public String getToken()
    {
        return prefs.getString("token", null);
    }

    public String getEmail()
    {
        return prefs.getString("email", "");
    }

    public boolean iniciada()
    {
        return getToken() != null;
    }

    public void cerrar()
    {
        prefs.edit().clear().apply();
    }
}
