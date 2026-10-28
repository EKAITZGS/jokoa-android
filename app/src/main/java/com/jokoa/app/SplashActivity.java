package com.jokoa.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.jokoa.app.red.Sesion;

// Pantalla de entrada: los cuatro cuadrados del logo aparecen uno a uno y luego se decide a dónde ir
public class SplashActivity extends AppCompatActivity
{
    private final Handler manejador = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int[] ids = {R.id.c1, R.id.c2, R.id.c3, R.id.c4};
        for (int i = 0; i < ids.length; i++)
        {
            View cuadro = findViewById(ids[i]);
            cuadro.setScaleX(0f);
            cuadro.setScaleY(0f);
            cuadro.animate().scaleX(1f).scaleY(1f)
                    .setStartDelay(120L * i).setDuration(450)
                    .setInterpolator(new OvershootInterpolator(1.8f)).start();
        }
        // El nombre y el lema suben mientras aparecen (300 ms, desde los 600 y 800 ms)
        float subida = 16 * getResources().getDisplayMetrics().density;
        for (int[] t : new int[][]{{R.id.tvMarca, 600}, {R.id.tvLema, 800}})
        {
            View texto = findViewById(t[0]);
            texto.setTranslationY(subida);
            texto.animate().alpha(1f).translationY(0f).setStartDelay(t[1]).setDuration(300)
                    .setInterpolator(new DecelerateInterpolator()).start();
        }

        manejador.postDelayed(this::seguir, 1700);
    }

    private void seguir()
    {
        Class<?> destino = new Sesion(this).iniciada() ? MainActivity.class : LoginActivity.class;
        startActivity(new Intent(this, destino));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy()
    {
        manejador.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
