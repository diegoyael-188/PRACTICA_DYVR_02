package com.example.practica_dyvr_02.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practica_dyvr_02.R;
import com.example.practica_dyvr_02.api.LibroApi;
import com.example.practica_dyvr_02.db.AppDatabase;
import com.example.practica_dyvr_02.db.LibroDao;
import com.example.practica_dyvr_02.models.LibroEntity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleActivity extends AppCompatActivity {
    private LibroDao dao;
    private LibroEntity libro;
    private boolean esFavorito;
    private TextView info;
    private Button boton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);
        dao = AppDatabase.dao(this);
        info = findViewById(R.id.info);
        boton = findViewById(R.id.favorito);
        boton.setEnabled(false);
        boton.setOnClickListener(v -> alternarFavorito());

        long id = getIntent().getLongExtra("id", -1);
        info.setText("Cargando...");
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
                toast("Sin conexión: mostrando copia local si existe");
                cargarLocal(id);
            }
        });
    }

    private void cargarLocal(long id) {
        AppDatabase.IO.execute(() -> {
            LibroEntity local = dao.porId(id);
            runOnUiThread(() -> {
                if (local != null) mostrar(local, true);
                else info.setText("No se pudo cargar el libro.");
            });
        });
    }

    private void mostrar(LibroEntity l, boolean fav) {
        libro = l;
        esFavorito = fav;
        info.setText("ID: " + l.id
                + "\n\nTítulo: " + l.titulo
                + "\n\nAutor: " + l.autor
                + "\n\nEditorial: " + l.editorial
                + "\n\nPáginas: " + l.paginas
                + "\n\nDisponibilidad: " + (l.disponible ? "Disponible" : "No disponible")
                + "\n\nFavorito: " + (fav ? "Sí" : "No"));
        boton.setText(fav ? "Quitar de favoritos" : "Agregar a favoritos");
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
