package com.jokoa.app.fragmentos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jokoa.app.LoginActivity;
import com.jokoa.app.R;
import com.jokoa.app.red.Api;
import com.jokoa.app.red.Sesion;

import org.json.JSONException;
import org.json.JSONObject;

// Perfil: ver y cambiar los datos y la contraseña (GET /perfil y PUT /perfil/update)
public class FragmentoPerfil extends Fragment
{
    private EditText etNombre, etApellidos, etTelefono, etActual, etNueva;
    private Button btnGuardar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup contenedor, @Nullable Bundle estado)
    {
        View vista = inflater.inflate(R.layout.fragment_perfil, contenedor, false);
        final Sesion sesion = new Sesion(requireContext());
        ((TextView) vista.findViewById(R.id.tvEmail)).setText(sesion.getEmail());
        etNombre = vista.findViewById(R.id.etNombre);
        etApellidos = vista.findViewById(R.id.etApellidos);
        etTelefono = vista.findViewById(R.id.etTelefono);
        etActual = vista.findViewById(R.id.etActual);
        etNueva = vista.findViewById(R.id.etNueva);
        btnGuardar = vista.findViewById(R.id.btnGuardar);

        btnGuardar.setOnClickListener(v -> guardar());
        vista.findViewById(R.id.btnSoporte).setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(), com.jokoa.app.SoporteActivity.class)));
        vista.findViewById(R.id.btnSalir).setOnClickListener(v ->
        {
            sesion.cerrar();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
        cargar();
        return vista;
    }

    private void cargar()
    {
        Api.enviar(requireContext(), com.android.volley.Request.Method.GET, "/perfil", null,
                datos ->
                {
                    etNombre.setText(datos.optString("nombre"));
                    etApellidos.setText(datos.optString("apellidos"));
                    String telefono = datos.optString("telefono");
                    etTelefono.setText("null".equals(telefono) ? "" : telefono);
                },
                error -> Toast.makeText(getContext(), "No se pudo cargar el perfil", Toast.LENGTH_LONG).show());
    }

    private void guardar()
    {
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String actual = etActual.getText().toString();
        String nueva = etNueva.getText().toString();
        if (nombre.isEmpty()) { etNombre.setError("Obligatorio"); return; }
        if (apellidos.isEmpty()) { etApellidos.setError("Obligatorio"); return; }
        if (!nueva.isEmpty() && nueva.length() < 6) { etNueva.setError("Mínimo 6 caracteres"); return; }
        if (!nueva.isEmpty() && actual.isEmpty()) { etActual.setError("Escribe la contraseña actual"); return; }
        try
        {
            JSONObject cuerpo = new JSONObject();
            cuerpo.put("nombre", nombre);
            cuerpo.put("apellidos", apellidos);
            cuerpo.put("telefono", etTelefono.getText().toString().trim());
            if (!nueva.isEmpty())
            {
                cuerpo.put("password_actual", actual);
                cuerpo.put("password_nueva", nueva);
            }
            btnGuardar.setEnabled(false);
            Api.enviar(requireContext(), Api.put(), "/perfil/update", cuerpo,
                    respuesta ->
                    {
                        btnGuardar.setEnabled(true);
                        etActual.setText("");
                        etNueva.setText("");
                        Toast.makeText(getContext(), "Perfil guardado", Toast.LENGTH_LONG).show();
                    },
                    error ->
                    {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(getContext(), Api.mensaje(error, "No se pudo guardar"), Toast.LENGTH_LONG).show();
                    });
        }
        catch (JSONException e)
        {
            btnGuardar.setEnabled(true);
        }
    }
}
