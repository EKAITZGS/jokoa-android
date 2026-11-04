package com.jokoa.app.datos;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

// Guarda la conversación con el asesor mientras la app está abierta (al cambiar de pestaña no se pierde)
public class AsesorModelo extends ViewModel
{
    public final List<Turno> turnos = new ArrayList<>();
    public boolean pensando;

    public AsesorModelo()
    {
        turnos.add(new Turno(false, "¡Hola! Soy el asesor de Jokoa. Cuéntame qué buscas (un RPG, un móvil con buena cámara, un portátil para estudiar...) y te recomiendo lo que tenemos."));
    }
}
