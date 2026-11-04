package com.example.practica_dyvr_02.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practica_dyvr_02.R;
import com.example.practica_dyvr_02.adapters.LibroAdapter;
import com.example.practica_dyvr_02.api.LibroApi;
import com.example.practica_dyvr_02.db.AppDatabase;
import com.example.practica_dyvr_02.db.LibroDao;
import com.example.practica_dyvr_02.models.LibroEntity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleActivity extends AppCompatActivity {
    private LibroDao dao;
    private LibroEntity libro;
    private boolean esFavorito;
    private MaterialButton boton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);
        dao = AppDatabase.dao(this);
        MaterialToolbar barra = findViewById(R.id.barra);
        barra.setTitle("Detalle");
        barra.setNavigationOnClickListener(v -> finish());
        boton = findViewById(R.id.favorito);
        boton.setOnClickListener(v -> alternarFavorito());

        long id = getIntent().getLongExtra("id", -1);
        LibroApi.INSTANCE.detalle(id).enqueue(new Callback<LibroEntity>() {
            @Override
            public void onResponse(@NonNull Call<LibroEntity> call, @NonNull Response<LibroEntity> r) {
                if (r.isSuccessful() && r.body() != null) {
                    LibroEntity remoto = r.body();
                    AppDatabase.IO.execute(() -> {
                        boolean fav = dao.porId(id) != null;
                        if (fav) dao.actualizar(remoto); // mantiene la copia offline al día
                        runOnUiThread(() -> mostrar(remoto, fav));
                    });
                } else {
                    toast(r.code() == 404 ? "Libro no encontrado" : "Error del servidor (" + r.code() + ")");
                    cargarLocal(id);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LibroEntity> call, @NonNull Throwable t) {
                toast("Sin conexión: mostrando la copia guardada");
                cargarLocal(id);
            }
        });
    }

    private void cargarLocal(long id) {
        AppDatabase.IO.execute(() -> {
            LibroEntity local = dao.porId(id);
            runOnUiThread(() -> {
                if (local != null) {
                    mostrar(local, true);
                } else {
                    findViewById(R.id.progreso).setVisibility(View.GONE);
                    TextView estado = findViewById(R.id.estado);
                    estado.setText("No se pudo cargar el libro. Revisa tu conexión e inténtalo de nuevo.");
                    estado.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void mostrar(LibroEntity l, boolean fav) {
        libro = l;
        esFavorito = fav;
        findViewById(R.id.progreso).setVisibility(View.GONE);
        findViewById(R.id.contenido).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.titulo)).setText(l.titulo);
        ((TextView) findViewById(R.id.autor)).setText(l.autor);
        LibroAdapter.pintarDisponible(findViewById(R.id.disponible), l.disponible);
        ((TextView) findViewById(R.id.datos)).setText("ID: " + l.id
                + "\nEditorial: " + l.editorial
                + "\nPáginas: " + l.paginas
                + "\nFavorito: " + (fav ? "Sí" : "No"));
        boton.setText(fav ? "Quitar de favoritos" : "Agregar a favoritos");
        boton.setIconResource(fav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
        boton.setEnabled(true);
    }

    private void alternarFavorito() {
        boolean nuevo = !esFavorito;
        AppDatabase.IO.execute(() -> {
            if (nuevo) dao.insertar(libro);
            else dao.eliminar(libro);
            runOnUiThread(() -> mostrar(libro, nuevo));
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
