package com.jokoa.app;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jokoa.app.red.Api;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistroActivity extends AppCompatActivity
{
    private EditText etNombre, etApellidos, etTelefono, etEmail, etPassword;
    private Button btnCrear;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raiz), (v, insets) ->
        {
            Insets b = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(b.left, b.top, b.right, b.bottom);
            return insets;
        });
        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etTelefono = findViewById(R.id.etTelefono);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnCrear = findViewById(R.id.btnCrear);
        btnCrear.setOnClickListener(v -> crear());
        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());
        findViewById(R.id.tvYaTengo).setOnClickListener(v -> finish());
    }

    private void crear()
    {
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        if (nombre.isEmpty()) { etNombre.setError("Obligatorio"); return; }
        if (apellidos.isEmpty()) { etApellidos.setError("Obligatorio"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Email no válido"); return; }
        if (password.length() < 6) { etPassword.setError("Mínimo 6 caracteres"); return; }
        try
        {
            JSONObject cuerpo = new JSONObject();
            cuerpo.put("nombre", nombre);
            cuerpo.put("apellidos", apellidos);
            cuerpo.put("telefono", etTelefono.getText().toString().trim());
            cuerpo.put("email", email);
            cuerpo.put("password", password);
            btnCrear.setEnabled(false);
            Api.enviar(this, Api.post(), "/registro", cuerpo,
                    respuesta ->
                    {
                        Toast.makeText(this, "Cuenta creada, ya puedes entrar", Toast.LENGTH_LONG).show();
                        finish();
                    },
                    error ->
                    {
                        btnCrear.setEnabled(true);
                        int codigo = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                        Toast.makeText(this, codigo == 409 ? "Ya existe un usuario con ese email"
                                : "No se pudo crear la cuenta", Toast.LENGTH_LONG).show();
                    });
        }
        catch (JSONException e)
        {
            btnCrear.setEnabled(true);
        }
    }
}
