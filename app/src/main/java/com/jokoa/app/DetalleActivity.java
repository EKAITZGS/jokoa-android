package com.jokoa.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
        if (producto.esMovil)
        {
            // La foto del móvil es cuadrada y se ve entera, sin recortar
            ViewGroup.LayoutParams lp = ivImagen.getLayoutParams();
            lp.width = dp(220);
            lp.height = dp(220);
            ivImagen.setLayoutParams(lp);
            ivImagen.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        ivImagen.setTransitionName("imagen_" + producto.id);
        Glide.with(this).load(producto.imagen)
                .placeholder(R.drawable.foto_placeholder)
                .error(R.drawable.foto_placeholder)
                .into(ivImagen);

        ((TextView) findViewById(R.id.tvNombre)).setText(producto.nombre);
        LinearLayout chips = findViewById(R.id.chips);
        chips.addView(crearChip(producto.etiqueta1, R.drawable.bg_chip_azul));
        chips.addView(crearChip(producto.etiqueta2, R.drawable.bg_chip_azul));
        // PEGI va con contorno; en los móviles las tres píldoras son iguales
        chips.addView(crearChip(producto.etiqueta3, producto.esMovil ? R.drawable.bg_chip_azul : R.drawable.bg_chip_contorno));
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
        if (stock > 0)
        {
            darEstiloChip(tvStock, "Quedan " + stock + " unidades", R.drawable.bg_chip_coral, R.color.jokoa_coral_oscuro);
        }
        else
        {
            darEstiloChip(tvStock, "Sin stock", R.drawable.bg_chip_gris, R.color.jokoa_texto);
        }
        btnComprar.setEnabled(stock > 0);
        btnAlquilar.setEnabled(stock > 0);
    }

    private int dp(int valor)
    {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    // Píldora de datos del producto (plataforma, género, PEGI, marca, memoria...)
    private TextView crearChip(String texto, int fondo)
    {
        TextView chip = new TextView(this);
        darEstiloChip(chip, texto, fondo, R.color.jokoa_azul_noche);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(8));
        chip.setLayoutParams(lp);
        return chip;
    }

    private void darEstiloChip(TextView chip, String texto, int fondo, int colorTexto)
    {
        chip.setText(texto);
        chip.setBackgroundResource(fondo);
        chip.setTextColor(ContextCompat.getColor(this, colorTexto));
        chip.setTextSize(12);
        chip.setTypeface(chip.getTypeface(), android.graphics.Typeface.BOLD);
        chip.setPadding(dp(12), dp(6), dp(12), dp(6));
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
        new MaterialAlertDialogBuilder(this)
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
