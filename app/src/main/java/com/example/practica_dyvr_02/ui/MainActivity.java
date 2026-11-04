package com.example.practica_dyvr_02.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.practica_dyvr_02.R;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS = "prefs";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(prefs.getBoolean("oscuro", false)
                ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText nombre = findViewById(R.id.nombre);
        SwitchCompat tema = findViewById(R.id.tema);
        nombre.setText(prefs.getString("nombre", ""));
        tema.setChecked(prefs.getBoolean("oscuro", false));

        findViewById(R.id.guardar).setOnClickListener(v -> {
            prefs.edit().putString("nombre", nombre.getText().toString().trim()).apply();
            mostrarPreferencias();
        });
        tema.setOnCheckedChangeListener((b, oscuro) -> {
            prefs.edit().putBoolean("oscuro", oscuro).apply();
            AppCompatDelegate.setDefaultNightMode(oscuro
                    ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
        findViewById(R.id.catalogo).setOnClickListener(v -> abrir(false));
        findViewById(R.id.favoritos).setOnClickListener(v -> abrir(true));
        findViewById(R.id.continuar).setOnClickListener(v ->
                abrir("favoritos".equals(prefs.getString("ultima", "catalogo"))));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mostrarPreferencias();
    }

    private void mostrarPreferencias() {
        String nombre = prefs.getString("nombre", "");
        String ultima = prefs.getString("ultima", "catalogo");
        ((TextView) findViewById(R.id.bienvenida)).setText(
                "¡Bienvenido" + (nombre.isEmpty() ? "" : ", " + nombre) + "!\n"
                        + "Tema: " + (prefs.getBoolean("oscuro", false) ? "Oscuro" : "Claro") + "\n"
                        + "Última sección: " + ("favoritos".equals(ultima) ? "Favoritos" : "Catálogo"));
        ((Button) findViewById(R.id.continuar)).setText(
                "Continuar en " + ("favoritos".equals(ultima) ? "Favoritos" : "Catálogo"));
    }

    private void abrir(boolean favoritos) {
        startActivity(new Intent(this, ListaActivity.class).putExtra("favoritos", favoritos));
    }
}
