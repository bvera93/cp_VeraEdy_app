package com.samuwings.app.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    val BASE_URL="https://samusystems.com/app/api/"
    val PrivicyPolicy="https://samusystems.com/app/privacy-policy/"
    val termscondition="https://samusystems.com/app/terms-condition"
    val MapKey="AIzaSyBwef9hj7a1EABnp8UjaCfys8MaORAUgRY"
    val Stripe="pk_test_51J7vMgJpua5GL5dmQiyUYhX7ZJDl9cv9JvEmmdmpBLPCzlj5stU7m9fFOkRZYqRwfIY2iv1IfgESeXi4lRNumF6i005Ftf7sRg"

    var TIMEOUT: Long = 60 * 2 * 1.toLong()
    val getClient: ApiInterface get() {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClient=OkHttpClient.Builder().connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        httpClient.addInterceptor(logging)
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofit.create(ApiInterface::class.java)
    }

}