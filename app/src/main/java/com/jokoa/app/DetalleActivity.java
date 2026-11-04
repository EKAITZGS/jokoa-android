package com.jokoa.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.jokoa.app.datos.Comparar;
import com.jokoa.app.datos.Favoritos;
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
    private Button btnComprar, btnAlquilar, btnGarantia;
    private int garantia = -1; // posición elegida en producto.garantiaMeses, o -1

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
            View fav = findViewById(R.id.btnFavorito);
            ViewGroup.MarginLayoutParams lpf = (ViewGroup.MarginLayoutParams) fav.getLayoutParams();
            lpf.topMargin = lp.topMargin;
            fav.setLayoutParams(lpf);
            return insets;
        });
        producto = (Producto) getIntent().getSerializableExtra("producto");
        stock = producto.stock;

        ImageView ivImagen = findViewById(R.id.ivImagen);
        if (!producto.esJuego)
        {
            // La foto de móviles y artículos es cuadrada y se ve entera, sin recortar
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
        // Las píldoras vacías (artículos con pocos datos) no se pintan; PEGI va con contorno
        for (String etiqueta : new String[]{producto.etiqueta1, producto.etiqueta2})
        {
            if (!etiqueta.isEmpty())
            {
                chips.addView(crearChip(etiqueta, R.drawable.bg_chip_azul));
            }
        }
        if (!producto.etiqueta3.isEmpty())
        {
            chips.addView(crearChip(producto.etiqueta3, producto.esJuego ? R.drawable.bg_chip_contorno : R.drawable.bg_chip_azul));
        }
        pintarFicha();
        if (producto.garantiaMeses.length > 0)
        {
            btnGarantia = findViewById(R.id.btnGarantia);
            btnGarantia.setVisibility(View.VISIBLE);
            btnGarantia.setOnClickListener(v -> elegirGarantia());
        }
        ((TextView) findViewById(R.id.tvPrecio)).setText(String.format(Locale.getDefault(), "%.2f €", producto.precio));
        ((TextView) findViewById(R.id.tvDescripcion)).setText(producto.descripcion);
        tvStock = findViewById(R.id.tvStock);
        btnComprar = findViewById(R.id.btnComprar);
        btnAlquilar = findViewById(R.id.btnAlquilar);
        findViewById(R.id.btnAtras).setOnClickListener(v -> supportFinishAfterTransition());
        ImageButton btnFavorito = findViewById(R.id.btnFavorito);
        Runnable pintarCorazon = () ->
        {
            boolean marcado = Favoritos.es(producto.id);
            btnFavorito.setImageResource(marcado ? R.drawable.ic_corazon_lleno : R.drawable.ic_corazon);
            btnFavorito.setContentDescription(marcado ? "Quitar de favoritos" : "Añadir a favoritos");
        };
        pintarCorazon.run();
        btnFavorito.setOnClickListener(v -> Favoritos.alternar(this, producto.id, pintarCorazon));

        Button btnComparar = findViewById(R.id.btnComparar);
        Button btnVer = findViewById(R.id.btnVerComparacion);
        Runnable pintarComparar = () ->
        {
            btnComparar.setText(Comparar.es(producto.id) ? "Quitar del comparador" : "Añadir al comparador");
            btnVer.setVisibility(Comparar.items().size() >= 2 ? View.VISIBLE : View.GONE);
            btnVer.setText("Ver comparación (" + Comparar.items().size() + ")");
        };
        pintarComparar.run();
        btnComparar.setOnClickListener(v ->
        {
            if (!Comparar.alternar(producto))
            {
                Toast.makeText(this, "Solo puedes comparar " + Comparar.MAX + " productos a la vez", Toast.LENGTH_SHORT).show();
            }
            pintarComparar.run();
        });
        btnVer.setOnClickListener(v -> startActivity(new android.content.Intent(this, ComparadorActivity.class)));

        btnComprar.setOnClickListener(v -> comprar());
        if (!producto.esJuego)
        {
            btnAlquilar.setVisibility(View.GONE);
        }
        else
        {
            btnAlquilar.setText(String.format(Locale.getDefault(), "Alquilar · %.2f €/día", producto.alquilerDia));
            btnAlquilar.setOnClickListener(v -> elegirDias());
        }
        pintarStock();
        pintarGarantia();
    }

    // Ficha técnica de los artículos: una fila "dato · valor" por característica
    private void pintarFicha()
    {
        if (producto.ficha.isEmpty())
        {
            return;
        }
        LinearLayout ficha = findViewById(R.id.fichaTecnica);
        ficha.setVisibility(View.VISIBLE);
        TextView titulo = new TextView(this);
        titulo.setText("Ficha técnica");
        titulo.setTextColor(ContextCompat.getColor(this, R.color.jokoa_texto));
        titulo.setTextSize(18);
        titulo.setTypeface(titulo.getTypeface(), android.graphics.Typeface.BOLD);
        ficha.addView(titulo);
        for (String[] dato : producto.ficha)
        {
            TextView fila = new TextView(this);
            fila.setText(android.text.Html.fromHtml("<b>" + android.text.Html.escapeHtml(dato[0]) + ":</b> " + android.text.Html.escapeHtml(dato[1]), android.text.Html.FROM_HTML_MODE_COMPACT));
            fila.setTextColor(ContextCompat.getColor(this, R.color.jokoa_texto));
            fila.setTextSize(15);
            fila.setPadding(0, dp(6), 0, 0);
            ficha.addView(fila);
        }
    }

    // Garantía ampliada: por unidad, se elige antes de comprar (-1 = sin garantía ampliada)
    private void elegirGarantia()
    {
        String[] opciones = new String[producto.garantiaMeses.length + 1];
        opciones[0] = "Sin garantía ampliada";
        for (int i = 0; i < producto.garantiaMeses.length; i++)
        {
            opciones[i + 1] = String.format(Locale.getDefault(), "+%d meses · %.2f €", producto.garantiaMeses[i], producto.garantiaPrecio[i]);
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Garantía ampliada")
                .setSingleChoiceItems(opciones, garantia + 1, (dialogo, cual) ->
                {
                    garantia = cual - 1;
                    pintarGarantia();
                    dialogo.dismiss();
                })
                .show();
    }

    // El botón de garantía y el de comprar muestran lo elegido y el total
    private void pintarGarantia()
    {
        double total = producto.precio + (garantia >= 0 ? producto.garantiaPrecio[garantia] : 0);
        if (btnGarantia != null)
        {
            btnGarantia.setText(garantia < 0 ? "Garantía ampliada: sin elegir"
                    : String.format(Locale.getDefault(), "Garantía +%d meses · %.2f €", producto.garantiaMeses[garantia], producto.garantiaPrecio[garantia]));
        }
        btnComprar.setText(garantia < 0 ? "Comprar" : String.format(Locale.getDefault(), "Comprar · %.2f €", total));
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

    // La compra se recoge en tienda: primero se elige cuál
    private void comprar()
    {
        btnComprar.setEnabled(false);
        Api.lista(this, "/tiendas",
                tiendas ->
                {
                    String[] nombres = new String[tiendas.length()];
                    for (int i = 0; i < nombres.length; i++)
                    {
                        nombres[i] = tiendas.optJSONObject(i).optString("nombre");
                    }
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("¿Dónde recoges tu pedido?")
                            .setItems(nombres, (dialogo, cual) -> enviarCompra(tiendas.optJSONObject(cual).optInt("id")))
                            .setOnCancelListener(d -> pintarStock())
                            .show();
                },
                error ->
                {
                    pintarStock();
                    Toast.makeText(this, Api.mensaje(error, "No se pudieron cargar las tiendas"), Toast.LENGTH_LONG).show();
                });
    }

    private void enviarCompra(int tiendaId)
    {
        try
        {
            JSONObject linea = new JSONObject();
            linea.put("producto_id", producto.id);
            linea.put("cantidad", 1);
            if (garantia >= 0)
            {
                linea.put("garantia_meses", producto.garantiaMeses[garantia]);
            }
            JSONObject cuerpo = new JSONObject();
            cuerpo.put("lineas", new JSONArray().put(linea));
            cuerpo.put("tienda_id", tiendaId);
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
