package com.example.practica_dyvr_02.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica_dyvr_02.R;
import com.example.practica_dyvr_02.models.LibroEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class LibroAdapter extends RecyclerView.Adapter<LibroAdapter.Holder> {
    private List<LibroEntity> libros = new ArrayList<>();
    private Set<Long> marcados = new HashSet<>();
    @DrawableRes
    private final int iconoBoton;
    private final String descripcionBoton;
    private final Consumer<LibroEntity> alTocar;
    private final Consumer<LibroEntity> alBoton;

    public LibroAdapter(@DrawableRes int iconoBoton, String descripcionBoton,
                        Consumer<LibroEntity> alTocar, Consumer<LibroEntity> alBoton) {
        this.iconoBoton = iconoBoton;
        this.descripcionBoton = descripcionBoton;
        this.alTocar = alTocar;
        this.alBoton = alBoton;
    }

    public void setLibros(List<LibroEntity> libros) {
        this.libros = libros;
        notifyDataSetChanged();
    }

    /** Ids que ya son favoritos: se pintan con corazón relleno. */
    public void setMarcados(Set<Long> marcados) {
        this.marcados = marcados;
        notifyDataSetChanged();
    }

    /** Chip verde/rojo de disponibilidad; también lo usa la pantalla de detalle. */
    public static void pintarDisponible(Chip chip, boolean disponible) {
        chip.setText(disponible ? "Disponible" : "No disponible");
        chip.setTextColor(ContextCompat.getColor(chip.getContext(),
                disponible ? R.color.disponible_fg : R.color.no_disponible_fg));
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(chip.getContext(),
                disponible ? R.color.disponible_bg : R.color.no_disponible_bg)));
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_libro, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        LibroEntity l = libros.get(position);
        h.titulo.setText(l.titulo);
        h.autor.setText(l.autor);
        h.info.setText("#" + l.id + " · " + l.editorial + " · " + l.paginas + " págs.");
        pintarDisponible(h.disponible, l.disponible);
        boolean marcado = marcados.contains(l.id);
        h.boton.setIconResource(marcado ? R.drawable.ic_favorite_filled : iconoBoton);
        h.boton.setIconTint(ColorStateList.valueOf(marcado
                ? ContextCompat.getColor(h.boton.getContext(), R.color.favorito)
                : MaterialColors.getColor(h.boton, androidx.appcompat.R.attr.colorPrimary)));
        h.boton.setContentDescription((marcado ? "Quitar de favoritos" : descripcionBoton) + ": " + l.titulo);
        h.itemView.setOnClickListener(v -> alTocar.accept(l));
        h.boton.setOnClickListener(v -> alBoton.accept(l));
    }

    @Override
    public int getItemCount() {
        return libros.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView titulo, autor, info;
        final Chip disponible;
        final MaterialButton boton;

        Holder(View v) {
            super(v);
            titulo = v.findViewById(R.id.titulo);
            autor = v.findViewById(R.id.autor);
            info = v.findViewById(R.id.info);
            disponible = v.findViewById(R.id.disponible);
            boton = v.findViewById(R.id.boton);
        }
    }
}
