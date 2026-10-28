package com.jokoa.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.jokoa.app.adaptadores.FilaAdapter;
import com.jokoa.app.red.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// Soporte técnico: lista de tickets del usuario y alta de uno nuevo en un departamento
public class SoporteActivity extends AppCompatActivity
{
    private RecyclerView lista;
    private LinearProgressIndicator progreso;
    private View tvVacio;
    private final List<Integer> idsDepartamento = new ArrayList<>();
    private final List<String> nombresDepartamento = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soporte);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raiz), (v, insets) ->
        {
            androidx.core.graphics.Insets b = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(b.left, b.top, b.right, b.bottom);
            return insets;
        });
        lista = findViewById(R.id.lista);
        progreso = findViewById(R.id.progreso);
        tvVacio = findViewById(R.id.tvVacio);
        lista.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());
        findViewById(R.id.btnNuevo).setOnClickListener(v -> nuevoTicket());

        Api.lista(this, "/departamentos", datos ->
        {
            for (int i = 0; i < datos.length(); i++)
            {
                idsDepartamento.add(datos.optJSONObject(i).optInt("id"));
                nombresDepartamento.add(datos.optJSONObject(i).optString("nombre"));
            }
        }, error -> { });
        cargar();
    }

    private void cargar()
    {
        progreso.setVisibility(View.VISIBLE);
        Api.lista(this, "/tickets",
                datos ->
                {
                    progreso.setVisibility(View.GONE);
                    pintar(datos);
                },
                error ->
                {
                    progreso.setVisibility(View.GONE);
                    Toast.makeText(this, Api.mensaje(error, "No se pudieron cargar los tickets"), Toast.LENGTH_LONG).show();
                });
    }

    private void pintar(JSONArray datos)
    {
        List<FilaAdapter.Fila> filas = new ArrayList<>();
        for (int i = 0; i < datos.length(); i++)
        {
            JSONObject t = datos.optJSONObject(i);
            String estado = t.optString("estado");
            int codigo = estado.equals("cerrado") ? FilaAdapter.CERRADO : estado.equals("en_proceso") ? FilaAdapter.EN_CURSO : FilaAdapter.ABIERTO;
            filas.add(new FilaAdapter.Fila(t.optInt("id"), t.optString("asunto"),
                    t.optString("departamento") + " · " + t.optString("dispositivo"), t.optString("descripcion"), null,
                    "Abierto el " + t.optString("fecha_apertura").substring(0, 10), null, codigo));
        }
        tvVacio.setVisibility(filas.isEmpty() ? View.VISIBLE : View.GONE);
        lista.setAdapter(new FilaAdapter(filas, fila -> { }));
    }

    private void nuevoTicket()
    {
        if (idsDepartamento.isEmpty())
        {
            Toast.makeText(this, "Cargando departamentos, inténtalo de nuevo", Toast.LENGTH_SHORT).show();
            return;
        }
        View form = LayoutInflater.from(this).inflate(R.layout.dialogo_ticket, (ViewGroup) null);
        MaterialAutoCompleteTextView sp = form.findViewById(R.id.spDepartamento);
        sp.setSimpleItems(nombresDepartamento.toArray(new String[0]));
        sp.setText(nombresDepartamento.get(0), false);
        final int[] elegido = {0};
        sp.setOnItemClickListener((padre, vista, posicion, id) -> elegido[0] = posicion);
        EditText etAsunto = form.findViewById(R.id.etAsunto);
        EditText etDispositivo = form.findViewById(R.id.etDispositivo);
        EditText etDescripcion = form.findViewById(R.id.etDescripcion);

        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle("Nuevo ticket")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Enviar", null)
                .create();
        dialogo.show();
        // El botón se engancha después de show() para que un error de validación no cierre el diálogo
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v ->
        {
            String asunto = etAsunto.getText().toString().trim();
            String dispositivo = etDispositivo.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            if (asunto.isEmpty() || dispositivo.isEmpty() || descripcion.isEmpty())
            {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            try
            {
                JSONObject cuerpo = new JSONObject();
                cuerpo.put("departamento_id", idsDepartamento.get(elegido[0]));
                cuerpo.put("asunto", asunto);
                cuerpo.put("dispositivo", dispositivo);
                cuerpo.put("descripcion", descripcion);
                v.setEnabled(false);
                Api.enviar(this, Api.post(), "/tickets/add", cuerpo,
                        respuesta ->
                        {
                            dialogo.dismiss();
                            Toast.makeText(this, "Ticket enviado", Toast.LENGTH_SHORT).show();
                            cargar();
                        },
                        error ->
                        {
                            v.setEnabled(true);
                            Toast.makeText(this, Api.mensaje(error, "No se pudo enviar el ticket"), Toast.LENGTH_LONG).show();
                        });
            }
            catch (JSONException e)
            {
                v.setEnabled(true);
            }
        });
    }
}
