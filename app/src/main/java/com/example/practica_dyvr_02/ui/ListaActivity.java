package com.example.practica_dyvr_02.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica_dyvr_02.R;
import com.example.practica_dyvr_02.adapters.LibroAdapter;
import com.example.practica_dyvr_02.api.LibroApi;
import com.example.practica_dyvr_02.db.AppDatabase;
import com.example.practica_dyvr_02.db.LibroDao;
import com.example.practica_dyvr_02.models.LibroEntity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Pantalla 2 (Catálogo, desde la API) y Pantalla 4 (Favoritos, desde Room) según el extra "favoritos". */
public class ListaActivity extends AppCompatActivity {
    private boolean favoritos;
    private LibroAdapter adapter;
    private LibroDao dao;
    private TextView estado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        favoritos = getIntent().getBooleanExtra("favoritos", false);
        dao = AppDatabase.dao(this);
        estado = findViewById(R.id.estado);
        getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).edit()
                .putString("ultima", favoritos ? "favoritos" : "catalogo").apply();

        ((TextView) findViewById(R.id.titulo)).setText(favoritos ? "Favoritos (offline)" : "Catálogo de libros");
        adapter = new LibroAdapter(favoritos ? "Eliminar" : "★ Favorito", this::verDetalle,
                favoritos ? this::eliminar : this::agregar);
        RecyclerView lista = findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        View actualizar = findViewById(R.id.actualizar);
        actualizar.setVisibility(favoritos ? View.GONE : View.VISIBLE);
        actualizar.setOnClickListener(v -> cargarCatalogo());
        if (!favoritos) cargarCatalogo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (favoritos) cargarFavoritos(); // refresca si se quitó un favorito desde el detalle
    }

    private void cargarCatalogo() {
        estado.setText("Cargando...");
        LibroApi.INSTANCE.catalogo().enqueue(new Callback<LibroApi.Catalogo>() {
            @Override
            public void onResponse(@NonNull Call<LibroApi.Catalogo> call, @NonNull Response<LibroApi.Catalogo> r) {
                if (r.isSuccessful() && r.body() != null && r.body().embedded != null) {
                    mostrar(r.body().embedded.libroDtoList);
                } else {
                    estado.setText("");
                    toast("Error del servidor (" + r.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LibroApi.Catalogo> call, @NonNull Throwable t) {
                estado.setText("Sin conexión. Pulsa Actualizar para reintentar.");
                toast("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void cargarFavoritos() {
        AppDatabase.IO.execute(() -> {
            List<LibroEntity> libros = dao.todos();
            runOnUiThread(() -> mostrar(libros));
        });
    }

    private void mostrar(List<LibroEntity> libros) {
        adapter.setLibros(libros);
        estado.setText(libros.isEmpty() ? "No hay libros." : libros.size() + " libros");
    }

    private void agregar(LibroEntity libro) {
        AppDatabase.IO.execute(() -> {
            dao.insertar(libro);
            runOnUiThread(() -> toast("Agregado a favoritos: " + libro.titulo));
        });
    }

    private void eliminar(LibroEntity libro) {
        AppDatabase.IO.execute(() -> {
            dao.eliminar(libro);
            runOnUiThread(() -> toast("Eliminado de favoritos"));
            cargarFavoritos();
        });
    }

    private void verDetalle(LibroEntity libro) {
        startActivity(new Intent(this, DetalleActivity.class).putExtra("id", libro.id));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
