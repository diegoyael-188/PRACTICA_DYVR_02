package com.example.practica_dyvr_02.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica_dyvr_02.R;
import com.example.practica_dyvr_02.models.LibroEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LibroAdapter extends RecyclerView.Adapter<LibroAdapter.Holder> {
    private List<LibroEntity> libros = new ArrayList<>();
    private final String textoBoton;
    private final Consumer<LibroEntity> alTocar;
    private final Consumer<LibroEntity> alBoton;

    public LibroAdapter(String textoBoton, Consumer<LibroEntity> alTocar, Consumer<LibroEntity> alBoton) {
        this.textoBoton = textoBoton;
        this.alTocar = alTocar;
        this.alBoton = alBoton;
    }

    public void setLibros(List<LibroEntity> libros) {
        this.libros = libros;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_libro, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        LibroEntity l = libros.get(position);
        h.titulo.setText("#" + l.id + "  " + l.titulo);
        h.info.setText(l.autor + " · " + l.editorial + "\n" + l.paginas + " páginas · "
                + (l.disponible ? "Disponible" : "No disponible"));
        h.boton.setText(textoBoton);
        h.itemView.setOnClickListener(v -> alTocar.accept(l));
        h.boton.setOnClickListener(v -> alBoton.accept(l));
    }

    @Override
    public int getItemCount() {
        return libros.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView titulo, info;
        final Button boton;

        Holder(View v) {
            super(v);
            titulo = v.findViewById(R.id.titulo);
            info = v.findViewById(R.id.info);
            boton = v.findViewById(R.id.boton);
        }
    }
}
