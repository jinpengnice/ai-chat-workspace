package dev.smartconvert.aichat.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://192.168.1.103:8080/"

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    val chatApi: ChatApi by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApi::class.java)
    }
}