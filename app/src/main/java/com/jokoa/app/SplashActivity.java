package com.jokoa.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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
                    .setStartDelay(150L * i).setDuration(450)
                    .setInterpolator(new OvershootInterpolator(2f)).start();
        }
        findViewById(R.id.tvMarca).animate().alpha(1f).setStartDelay(750).setDuration(400).start();
        findViewById(R.id.tvLema).animate().alpha(1f).setStartDelay(1000).setDuration(400).start();

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
