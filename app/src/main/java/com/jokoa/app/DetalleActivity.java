package com.jokoa.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.jokoa.app.datos.Producto;
import com.jokoa.app.red.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

// Ficha grande de un móvil o videojuego, con compra y (en juegos) alquiler
public class DetalleActivity extends AppCompatActivity
{
    private Producto producto;
    private int stock;
    private TextView tvStock;
    private Button btnComprar, btnAlquilar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);
        // Con pantalla completa la barra de estado tapa el borde: la flecha baja lo que mide la barra
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raiz), (v, insets) ->
        {
            View atras = findViewById(R.id.btnAtras);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) atras.getLayoutParams();
            lp.topMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top + (int) (16 * getResources().getDisplayMetrics().density);
            atras.setLayoutParams(lp);
            return insets;
        });
        producto = (Producto) getIntent().getSerializableExtra("producto");
        stock = producto.stock;

        ImageView ivImagen = findViewById(R.id.ivImagen);
        ivImagen.setTransitionName("imagen_" + producto.id);
        Glide.with(this).load(producto.imagen)
                .placeholder(R.drawable.foto_placeholder)
                .error(R.drawable.foto_placeholder)
                .into(ivImagen);

        ((TextView) findViewById(R.id.tvNombre)).setText(producto.nombre);
        ((TextView) findViewById(R.id.tvDetalle)).setText(producto.detalle);
        ((TextView) findViewById(R.id.tvPrecio)).setText(String.format(Locale.getDefault(), "%.2f €", producto.precio));
        ((TextView) findViewById(R.id.tvDescripcion)).setText(producto.descripcion);
        tvStock = findViewById(R.id.tvStock);
        btnComprar = findViewById(R.id.btnComprar);
        btnAlquilar = findViewById(R.id.btnAlquilar);
        findViewById(R.id.btnAtras).setOnClickListener(v -> supportFinishAfterTransition());

        btnComprar.setOnClickListener(v -> comprar());
        if (producto.esMovil)
        {
            btnAlquilar.setVisibility(View.GONE);
        }
        else
        {
            btnAlquilar.setText(String.format(Locale.getDefault(), "Alquilar · %.2f €/día", producto.alquilerDia));
            btnAlquilar.setOnClickListener(v -> elegirDias());
        }
        pintarStock();
    }

    private void pintarStock()
    {
        tvStock.setText(stock > 0 ? "Quedan " + stock + " unidades" : "Sin stock");
        btnComprar.setEnabled(stock > 0);
        btnAlquilar.setEnabled(stock > 0);
    }

    private void comprar()
    {
        try
        {
            JSONObject linea = new JSONObject();
            linea.put("producto_id", producto.id);
            linea.put("cantidad", 1);
            JSONObject cuerpo = new JSONObject();
            cuerpo.put("lineas", new JSONArray().put(linea));
            btnComprar.setEnabled(false);
            Api.enviar(this, Api.post(), "/compras/add", cuerpo,
                    respuesta ->
                    {
                        stock--;
                        pintarStock();
                        Toast.makeText(this, "¡Compra realizada!", Toast.LENGTH_LONG).show();
                    },
                    error ->
                    {
                        pintarStock();
                        Toast.makeText(this, Api.mensaje(error, "No se pudo completar la compra"), Toast.LENGTH_LONG).show();
                    });
        }
        catch (JSONException e)
        {
            pintarStock();
        }
    }

    private void elegirDias()
    {
        final int[] dias = {1, 3, 7};
        String[] textos = {"1 día", "3 días", "7 días"};
        new AlertDialog.Builder(this)
                .setTitle("¿Cuántos días?")
                .setItems(textos, (dialogo, cual) -> alquilar(dias[cual]))
                .show();
    }

    private void alquilar(int dias)
    {
        try
        {
            JSONObject cuerpo = new JSONObject();
            cuerpo.put("videojuego_id", producto.id);
            cuerpo.put("dias", dias);
            btnAlquilar.setEnabled(false);
            Api.enviar(this, Api.post(), "/alquileres/add", cuerpo,
                    respuesta ->
                    {
                        stock--;
                        pintarStock();
                        Toast.makeText(this, "Alquilado " + dias + " día(s)", Toast.LENGTH_LONG).show();
                    },
                    error ->
                    {
                        pintarStock();
                        Toast.makeText(this, Api.mensaje(error, "No se pudo alquilar"), Toast.LENGTH_LONG).show();
                    });
        }
        catch (JSONException e)
        {
            pintarStock();
        }
    }
}
