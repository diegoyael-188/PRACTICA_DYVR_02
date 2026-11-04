package com.example.practica_dyvr_02.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica_dyvr_02.R;
import com.example.practica_dyvr_02.adapters.LibroAdapter;
import com.example.practica_dyvr_02.api.LibroApi;
import com.example.practica_dyvr_02.db.AppDatabase;
import com.example.practica_dyvr_02.db.LibroDao;
import com.example.practica_dyvr_02.models.LibroEntity;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Pantalla 2 (Catálogo, desde la API) y Pantalla 4 (Favoritos, desde Room) según el extra "favoritos". */
public class ListaActivity extends AppCompatActivity {
    private boolean favoritos;
    private LibroAdapter adapter;
    private LibroDao dao;
    private TextView estado;
    private View progreso;
    private Set<Long> marcados = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        favoritos = getIntent().getBooleanExtra("favoritos", false);
        dao = AppDatabase.dao(this);
        estado = findViewById(R.id.estado);
        progreso = findViewById(R.id.progreso);
        getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).edit()
                .putString("ultima", favoritos ? "favoritos" : "catalogo").apply();

        MaterialToolbar barra = findViewById(R.id.barra);
        barra.setTitle(favoritos ? "Mis favoritos" : "Catálogo");
        barra.setNavigationOnClickListener(v -> finish());

        adapter = favoritos
                ? new LibroAdapter(R.drawable.ic_delete, "Eliminar de favoritos", this::verDetalle, this::eliminar)
                : new LibroAdapter(R.drawable.ic_favorite, "Agregar a favoritos", this::verDetalle, this::alternarFavorito);
        RecyclerView lista = findViewById(R.id.lista);
        lista.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.columnas)));
        lista.setAdapter(adapter);

        View actualizar = findViewById(R.id.actualizar);
        actualizar.setVisibility(favoritos ? View.GONE : View.VISIBLE);
        actualizar.setOnClickListener(v -> cargarCatalogo());
        if (!favoritos) cargarCatalogo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresca lo que pudo cambiar desde el detalle
        if (favoritos) cargarFavoritos();
        else cargarMarcados();
    }

    private void cargarMarcados() {
        AppDatabase.IO.execute(() -> {
            Set<Long> ids = new HashSet<>(dao.ids());
            runOnUiThread(() -> {
                marcados = ids;
                adapter.setMarcados(ids);
            });
        });
    }

    private void cargarCatalogo() {
        estado.setText("Cargando catálogo…");
        progreso.setVisibility(View.VISIBLE);
        LibroApi.INSTANCE.catalogo().enqueue(new Callback<LibroApi.Catalogo>() {
            @Override
            public void onResponse(@NonNull Call<LibroApi.Catalogo> call, @NonNull Response<LibroApi.Catalogo> r) {
                progreso.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null && r.body().embedded != null) {
                    mostrar(r.body().embedded.libroDtoList);
                } else {
                    estado.setText("El servidor no respondió como esperábamos.");
                    toast("Error del servidor (" + r.code() + "). Intenta de nuevo más tarde.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LibroApi.Catalogo> call, @NonNull Throwable t) {
                progreso.setVisibility(View.GONE);
                estado.setText("Sin conexión. Toca actualizar para reintentar o revisa tus favoritos.");
                toast("No se pudo conectar al servidor. Revisa tu conexión a Internet.");
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
        estado.setText(libros.isEmpty()
                ? (favoritos ? "Aún no tienes favoritos. Agrégalos desde el catálogo." : "No hay libros.")
                : libros.size() + (libros.size() == 1 ? " libro" : " libros"));
    }

    private void alternarFavorito(LibroEntity libro) {
        boolean quitar = marcados.contains(libro.id);
        AppDatabase.IO.execute(() -> {
            if (quitar) dao.eliminar(libro);
            else dao.insertar(libro);
            runOnUiThread(() -> toast(quitar ? "Eliminado de favoritos" : "Agregado a favoritos: " + libro.titulo));
            cargarMarcados();
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
