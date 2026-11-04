package com.example.practica_dyvr_02.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// ponytail: same class is the Gson DTO and the Room row; split if the API and DB shapes diverge.
@Entity(tableName = "favoritos")
public class LibroEntity {
    @PrimaryKey
    public long id;
    public String titulo;
    public String autor;
    public String editorial;
    public int paginas;
    public boolean disponible;
}
