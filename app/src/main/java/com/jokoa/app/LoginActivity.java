package com.jokoa.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jokoa.app.red.Api;
import com.jokoa.app.red.Sesion;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity
{
    private EditText etEmail, etPassword;
    private Button btnEntrar;
    private TextView tvRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Si ya hay sesión se salta el login
        if (new Sesion(this).iniciada())
        {
            irAPrincipal();
            return;
        }
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raiz), (v, insets) ->
        {
            Insets b = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int m = (int) (24 * getResources().getDisplayMetrics().density);
            v.setPadding(b.left + m, b.top + m, b.right + m, b.bottom + m);
            return insets;
        });
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnEntrar = findViewById(R.id.btnEntrar);
        tvRegistro = findViewById(R.id.tvRegistro);

        btnEntrar.setOnClickListener(v -> entrar());
        tvRegistro.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void entrar()
    {
        final String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            etEmail.setError("Email no válido");
            return;
        }
        if (password.isEmpty())
        {
            etPassword.setError("Escribe la contraseña");
            return;
        }
        try
        {
            JSONObject cuerpo = new JSONObject();
            cuerpo.put("email", email);
            cuerpo.put("password", password);
            btnEntrar.setEnabled(false);
            Api.enviar(this, Api.post(), "/login", cuerpo,
                    respuesta ->
                    {
                        new Sesion(this).guardar(respuesta.optString("token"), email);
                        irAPrincipal();
                    },
                    error ->
                    {
                        btnEntrar.setEnabled(true);
                        int codigo = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                        Toast.makeText(this, codigo == 401 ? "Email o contraseña incorrectos"
                                : "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show();
                    });
        }
        catch (JSONException e)
        {
            btnEntrar.setEnabled(true);
        }
    }

    private void irAPrincipal()
    {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
