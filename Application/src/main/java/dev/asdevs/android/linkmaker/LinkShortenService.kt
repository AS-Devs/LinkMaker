package dev.asdevs.android.linkmaker

import retrofit2.Call
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class ShortUrlPost(var url: String) {
}

class ShortLinkResponse() {
    lateinit var hashid: String
    lateinit var url: String
    lateinit var created_at: String
}

class RetroFitClient {

    companion object {
        lateinit var retrofit: Retrofit
        var BASE_URL: String = "https://rel.ink/"

        fun getRetrofitInstance(): Retrofit {
            retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            return retrofit
        }
    }

    interface ShortLink {
        @POST("api/links/")
        fun getShortLink(@Body shortLink: ShortUrlPost): Call<ShortLinkResponse>
    }
}