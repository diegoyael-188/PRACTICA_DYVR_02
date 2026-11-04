package com.example.practica_dyvr_02.api;

import com.example.practica_dyvr_02.models.LibroEntity;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface LibroApi {

    LibroApi INSTANCE = new Retrofit.Builder()
            .baseUrl("https://ejercicio-08-documentacion-y-endpoints.onrender.com/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LibroApi.class);

    @GET("libros")
    Call<Catalogo> catalogo();

    @GET("libros/{id}")
    Call<LibroEntity> detalle(@Path("id") long id);

    // HATEOAS wrapper: {"_embedded":{"libroDtoList":[...]}}
    class Catalogo {
        @SerializedName("_embedded")
        public Embedded embedded;

        public static class Embedded {
            public List<LibroEntity> libroDtoList;
        }
    }
}
