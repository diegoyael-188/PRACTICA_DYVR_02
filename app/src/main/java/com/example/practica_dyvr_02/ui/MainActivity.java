package com.example.practica_dyvr_02.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.practica_dyvr_02.R;
import com.example.practica_dyvr_02.db.AppDatabase;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.materialswitch.MaterialSwitch;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS = "prefs";
    /** Captura de la pantalla anterior al cambio de tema; la nueva instancia la desvanece encima. */
    private static Bitmap transicion;
    private SharedPreferences prefs;
    private BottomSheetBehavior<View> hoja;
    private final OnBackPressedCallback cerrarHoja = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            hoja.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    };

    @Override
    protected void onCreate(Bundle guardado) {
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        // Al recrearse por la vista previa se conserva el tema del borrador, no el guardado.
        aplicarTema(guardado != null ? guardado.getBoolean("tema") : prefs.getBoolean("oscuro", false));
        super.onCreate(guardado);
        getOnBackPressedDispatcher().addCallback(this, cerrarHoja);
        setContentView(R.layout.activity_main);
        montar(guardado != null && guardado.getBoolean("hojaAbierta"),
                guardado != null ? guardado.getString("nombre") : prefs.getString("nombre", ""),
                guardado != null ? guardado.getBoolean("tema") : prefs.getBoolean("oscuro", false));
        desvanecerTransicion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mostrarPreferencias();
        contarFavoritos();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("hojaAbierta", hoja.getState() != BottomSheetBehavior.STATE_HIDDEN);
        out.putString("nombre", ((EditText) findViewById(R.id.nombre)).getText().toString());
        out.putBoolean("tema", esOscuro());
    }

    private void montar(boolean hojaAbierta, String nombre, boolean oscuro) {
        findViewById(R.id.ajustes).setOnClickListener(v -> abrirHoja());
        findViewById(R.id.catalogo).setOnClickListener(v -> abrir(false));
        findViewById(R.id.favoritos).setOnClickListener(v -> abrir(true));
        findViewById(R.id.continuar).setOnClickListener(v ->
                abrir("favoritos".equals(prefs.getString("ultima", "catalogo"))));

        EditText campo = findViewById(R.id.nombre);
        MaterialSwitch tema = findViewById(R.id.tema);
        // Su estado se restaura a mano; el automático dispararía el listener antes del layout.
        campo.setSaveEnabled(false);
        tema.setSaveEnabled(false);
        campo.setText(nombre);
        tema.setChecked(oscuro);
        // El interruptor solo previsualiza; "Guardar" es lo único que persiste.
        tema.setOnCheckedChangeListener((b, o) -> cambiarTema(o));
        findViewById(R.id.guardar).setOnClickListener(v -> {
            prefs.edit()
                    .putString("nombre", campo.getText().toString().trim())
                    .putBoolean("oscuro", tema.isChecked())
                    .apply();
            hoja.setState(BottomSheetBehavior.STATE_HIDDEN);
            mostrarPreferencias();
        });

        // El panel sube por encima del teclado y de la barra de navegación.
        View contenido = findViewById(R.id.hojaContenido);
        int base = contenido.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(contenido, (v, in) -> {
            int abajo = in.getInsets(WindowInsetsCompat.Type.ime() | WindowInsetsCompat.Type.navigationBars()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), base + abajo);
            return in;
        });

        View velo = findViewById(R.id.velo);
        velo.setOnClickListener(v -> hoja.setState(BottomSheetBehavior.STATE_HIDDEN));
        velo.setVisibility(hojaAbierta ? View.VISIBLE : View.GONE);
        hoja = BottomSheetBehavior.from(findViewById(R.id.hoja));
        // Fijado antes del primer layout: aparece ya abierto, sin animación de entrada.
        hoja.setState(hojaAbierta ? BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_HIDDEN);
        cerrarHoja.setEnabled(hojaAbierta);
        hoja.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View v, int estado) {
                boolean oculta = estado == BottomSheetBehavior.STATE_HIDDEN;
                cerrarHoja.setEnabled(!oculta);
                if (oculta) {
                    velo.setVisibility(View.GONE);
                    controlador().hide(WindowInsetsCompat.Type.ime());
                    cambiarTema(prefs.getBoolean("oscuro", false)); // descarta la vista previa si no se guardó
                }
            }

            @Override
            public void onSlide(@NonNull View v, float desplazamiento) {
                velo.setVisibility(View.VISIBLE);
                velo.setAlpha(Math.max(0f, Math.min(1f, 1f + desplazamiento)));
            }
        });

        controlador().setAppearanceLightStatusBars(!esOscuro());
        controlador().setAppearanceLightNavigationBars(!esOscuro());
    }

    /** Cambia el tema tapando la recreación de la actividad con un fundido desde la pantalla actual. */
    private void cambiarTema(boolean oscuro) {
        if (oscuro == esOscuro()) return;
        View decor = getWindow().getDecorView();
        if (decor.getWidth() > 0 && decor.getHeight() > 0) {
            transicion = Bitmap.createBitmap(decor.getWidth(), decor.getHeight(), Bitmap.Config.ARGB_8888);
            decor.draw(new Canvas(transicion));
        }
        aplicarTema(oscuro);
    }

    private void desvanecerTransicion() {
        if (transicion == null) return;
        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        ImageView capa = new ImageView(this);
        capa.setImageBitmap(transicion);
        capa.setElevation(100f);
        transicion = null;
        decor.addView(capa, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        capa.animate().alpha(0f).setStartDelay(50).setDuration(250)
                .withEndAction(() -> decor.removeView(capa));
    }

    private void abrirHoja() {
        ((EditText) findViewById(R.id.nombre)).setText(prefs.getString("nombre", ""));
        ((MaterialSwitch) findViewById(R.id.tema)).setChecked(prefs.getBoolean("oscuro", false));
        hoja.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void mostrarPreferencias() {
        String nombre = prefs.getString("nombre", "");
        ((TextView) findViewById(R.id.saludo)).setText(
                "¡Hola" + (nombre.isEmpty() ? "" : ", " + nombre) + "!");
        ((TextView) findViewById(R.id.ultima)).setText(
                "favoritos".equals(prefs.getString("ultima", "catalogo")) ? "Favoritos" : "Catálogo");
    }

    private void contarFavoritos() {
        AppDatabase.IO.execute(() -> {
            int total = AppDatabase.dao(this).todos().size();
            runOnUiThread(() -> ((TextView) findViewById(R.id.totalFavoritos)).setText(
                    total == 0 ? "Disponibles sin conexión"
                            : total + (total == 1 ? " guardado" : " guardados") + " · sin conexión"));
        });
    }

    /** Tema pedido (global en AppCompat); ya vale el nuevo mientras la actividad se recrea. */
    private static boolean esOscuro() {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
    }

    private WindowInsetsControllerCompat controlador() {
        return WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
    }

    private static void aplicarTema(boolean oscuro) {
        AppCompatDelegate.setDefaultNightMode(oscuro
                ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void abrir(boolean favoritos) {
        startActivity(new Intent(this, ListaActivity.class).putExtra("favoritos", favoritos));
    }
}
