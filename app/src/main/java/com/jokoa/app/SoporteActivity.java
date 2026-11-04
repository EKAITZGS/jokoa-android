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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
            boolean reparacion = t.optString("tipo").equals("reparacion");
            int codigo = estado.equals("cerrado") || estado.equals("listo") ? FilaAdapter.CERRADO
                    : estado.equals("en_proceso") || estado.equals("en_reparacion") ? FilaAdapter.EN_CURSO : FilaAdapter.ABIERTO;
            boolean pendiente = estado.equals("presupuestado") && !t.isNull("presupuesto");
            StringBuilder detalle = new StringBuilder(t.optString("descripcion"));
            if (reparacion && !t.isNull("presupuesto"))
            {
                detalle.append(String.format(Locale.getDefault(), "\nPresupuesto: %.2f €", t.optDouble("presupuesto")));
            }
            if (estado.equals("listo"))
            {
                detalle.append("\nTu equipo está listo: puedes recogerlo en tu tienda Jokoa.");
            }
            if (estado.equals("cerrado") && !t.isNull("presupuesto_aceptado") && !t.optBoolean("presupuesto_aceptado"))
            {
                detalle.append("\nRechazaste el presupuesto.");
            }
            filas.add(new FilaAdapter.Fila(t.optInt("id"), t.optString("asunto"),
                    (reparacion ? "Reparación · " : "") + t.optString("departamento") + " · " + t.optString("dispositivo"), detalle.toString(), null,
                    "Abierto el " + t.optString("fecha_apertura").substring(0, 10), pendiente ? "Responder al presupuesto" : null, codigo,
                    reparacion ? ETIQUETAS.get(estado) : null));
        }
        tvVacio.setVisibility(filas.isEmpty() ? View.VISIBLE : View.GONE);
        lista.setAdapter(new FilaAdapter(filas, fila -> responderPresupuesto(fila.id, datos)));
    }

    // Etiquetas de estado de las reparaciones
    private static final Map<String, String> ETIQUETAS = new HashMap<>();
    static
    {
        ETIQUETAS.put("recibido", "Recibido");
        ETIQUETAS.put("presupuestado", "Presupuesto listo");
        ETIQUETAS.put("en_reparacion", "En reparación");
        ETIQUETAS.put("listo", "Listo para recoger");
        ETIQUETAS.put("cerrado", "Cerrado");
    }

    // El cliente acepta o rechaza el presupuesto: aceptado empieza la reparación, rechazado cierra el ticket
    private void responderPresupuesto(int id, JSONArray datos)
    {
        double importe = 0;
        for (int i = 0; i < datos.length(); i++)
        {
            if (datos.optJSONObject(i).optInt("id") == id)
            {
                importe = datos.optJSONObject(i).optDouble("presupuesto");
            }
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Presupuesto de la reparación")
                .setMessage(String.format(Locale.getDefault(), "El arreglo cuesta %.2f €. Si lo aceptas, empezamos a repararlo.", importe))
                .setPositiveButton("Aceptar", (d, c) -> enviarRespuesta(id, true))
                .setNegativeButton("Rechazar", (d, c) -> enviarRespuesta(id, false))
                .setNeutralButton("Más tarde", null)
                .show();
    }

    private void enviarRespuesta(int id, boolean aceptado)
    {
        try
        {
            Api.enviar(this, Api.put(), "/tickets/presupuesto/" + id, new JSONObject().put("aceptado", aceptado),
                    respuesta ->
                    {
                        Toast.makeText(this, aceptado ? "Presupuesto aceptado: empezamos la reparación" : "Presupuesto rechazado", Toast.LENGTH_LONG).show();
                        cargar();
                    },
                    error -> Toast.makeText(this, Api.mensaje(error, "No se pudo responder al presupuesto"), Toast.LENGTH_LONG).show());
        }
        catch (JSONException e)
        {
            Toast.makeText(this, "No se pudo responder al presupuesto", Toast.LENGTH_LONG).show();
        }
    }

    private void nuevoTicket()
    {
        if (idsDepartamento.isEmpty())
        {
            Toast.makeText(this, "Cargando departamentos, inténtalo de nuevo", Toast.LENGTH_SHORT).show();
            return;
        }
        View form = LayoutInflater.from(this).inflate(R.layout.dialogo_ticket, (ViewGroup) null);
        MaterialAutoCompleteTextView spTipo = form.findViewById(R.id.spTipo);
        spTipo.setSimpleItems(new String[]{"Una consulta o incidencia", "Reparar un equipo"});
        spTipo.setText("Una consulta o incidencia", false);
        final boolean[] reparacion = {false};
        spTipo.setOnItemClickListener((padre, vista, posicion, id) -> reparacion[0] = posicion == 1);
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
                cuerpo.put("tipo", reparacion[0] ? "reparacion" : "soporte");
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
