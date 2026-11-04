package com.example.practica_dyvr_02.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.practica_dyvr_02.models.LibroEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {LibroEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static volatile AppDatabase instance;

    public abstract LibroDao libroDao();

    public static LibroDao dao(Context ctx) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(ctx.getApplicationContext(), AppDatabase.class, "libros.db").build();
                }
            }
        }
        return instance.libroDao();
    }
}
