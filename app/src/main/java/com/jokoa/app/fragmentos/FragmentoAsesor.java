package com.jokoa.app.fragmentos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.jokoa.app.DetalleActivity;
import com.jokoa.app.R;
import com.jokoa.app.adaptadores.AsesorAdapter;
import com.jokoa.app.datos.AsesorModelo;
import com.jokoa.app.datos.Producto;
import com.jokoa.app.datos.Turno;
import com.jokoa.app.red.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Chat con el asesor de compra: el servidor consulta el catálogo real y devuelve texto y productos recomendados
public class FragmentoAsesor extends Fragment
{
    private AsesorModelo modelo;
    private AsesorAdapter adaptador;
    private RecyclerView hilo;
    private EditText etMensaje;
    private MaterialButton btnEnviar;
    private TextView tvError;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup contenedor, @Nullable Bundle estado)
    {
        View vista = inflater.inflate(R.layout.fragment_asesor, contenedor, false);
        modelo = new ViewModelProvider(requireActivity()).get(AsesorModelo.class);
        hilo = vista.findViewById(R.id.hilo);
        etMensaje = vista.findViewById(R.id.etMensaje);
        btnEnviar = vista.findViewById(R.id.btnEnviar);
        tvError = vista.findViewById(R.id.tvError);

        adaptador = new AsesorAdapter(modelo.turnos, this::abrirFicha);
        LinearLayoutManager disposicion = new LinearLayoutManager(getContext());
        disposicion.setStackFromEnd(true);
        hilo.setLayoutManager(disposicion);
        hilo.setAdapter(adaptador);
        adaptador.setPensando(modelo.pensando);

        etMensaje.addTextChangedListener(new android.text.TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) { }

            @Override
            public void afterTextChanged(android.text.Editable s)
            {
                pintarBoton();
            }
        });
        etMensaje.setOnEditorActionListener((v, accion, evento) ->
        {
            if (accion == EditorInfo.IME_ACTION_SEND)
            {
                enviar();
                return true;
            }
            return false;
        });
        btnEnviar.setOnClickListener(v -> enviar());
        pintarBoton();
        bajar();
        return vista;
    }

    // Enviar solo si hay texto y no se está esperando respuesta
    private void pintarBoton()
    {
        btnEnviar.setEnabled(!modelo.pensando && !etMensaje.getText().toString().trim().isEmpty());
    }

    private void bajar()
    {
        hilo.scrollToPosition(adaptador.getItemCount() - 1);
    }

    private void enviar()
    {
        String texto = etMensaje.getText().toString().trim();
        if (texto.isEmpty() || modelo.pensando)
        {
            return;
        }
        modelo.turnos.add(new Turno(true, texto));
        etMensaje.setText("");
        // Se cierra el teclado al enviar: así vuelve la barra inferior y se puede seguir navegando mientras responde
        etMensaje.clearFocus();
        ((android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(etMensaje.getWindowToken(), 0);
        tvError.setVisibility(View.GONE);
        modelo.pensando = true;
        adaptador.setPensando(true);
        pintarBoton();
        bajar();

        try
        {
            // La bienvenida no se manda: el servidor exige que la conversación empiece por el cliente
            JSONArray mensajes = new JSONArray();
            for (int i = 1; i < modelo.turnos.size(); i++)
            {
                Turno t = modelo.turnos.get(i);
                mensajes.put(new JSONObject().put("role", t.delUsuario ? "user" : "assistant").put("content", t.texto));
            }
            Api.enviar(requireContext(), Api.post(), "/asistente", new JSONObject().put("mensajes", mensajes),
                    this::alResponder,
                    error -> alFallar(Api.mensaje(error, "No se pudo consultar al asesor")));
        }
        catch (JSONException e)
        {
            alFallar("No se pudo consultar al asesor");
        }
    }

    private void alResponder(JSONObject r)
    {
        Turno respuesta = new Turno(false, r.optString("respuesta"));
        JSONArray productos = r.optJSONArray("productos");
        for (int i = 0; productos != null && i < productos.length(); i++)
        {
            JSONObject p = productos.optJSONObject(i);
            if (p != null)
            {
                respuesta.productos.add(new Turno.Tarjeta(p.optInt("id"), p.optString("nombre"), p.optString("tipo"),
                        p.optDouble("precio"), p.optString("imagen", "")));
            }
        }
        modelo.turnos.add(respuesta);
        modelo.pensando = false;
        if (isAdded())
        {
            adaptador.setPensando(false);
            pintarBoton();
            bajar();
        }
    }

    private void alFallar(String mensaje)
    {
        modelo.pensando = false;
        if (isAdded())
        {
            adaptador.setPensando(false);
            tvError.setText(mensaje);
            tvError.setVisibility(View.VISIBLE);
            pintarBoton();
        }
    }

    // La tarjeta trae pocos datos: se pide la ficha completa y se abre como desde la tienda
    private void abrirFicha(Turno.Tarjeta t)
    {
        Api.enviar(requireContext(), Api.get(), Producto.ruta(t.tipo) + "/" + t.id, null,
                json -> startActivity(new Intent(requireContext(), DetalleActivity.class).putExtra("producto", new Producto(json, t.tipo))),
                error -> Toast.makeText(getContext(), Api.mensaje(error, "No se pudo abrir el producto"), Toast.LENGTH_LONG).show());
    }
}
