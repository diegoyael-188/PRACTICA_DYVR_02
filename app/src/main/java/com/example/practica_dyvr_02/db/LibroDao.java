package com.example.practica_dyvr_02.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.practica_dyvr_02.models.LibroEntity;

import java.util.List;

@Dao
public interface LibroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(LibroEntity libro);

    @Query("SELECT * FROM favoritos ORDER BY titulo")
    List<LibroEntity> todos();

    @Query("SELECT * FROM favoritos WHERE id = :id")
    LibroEntity porId(long id);

    @Update
    void actualizar(LibroEntity libro);

    @Delete
    void eliminar(LibroEntity libro);
}
