package dev.asdevs.android.linkmaker

import androidx.annotation.Keep
import retrofit2.Call
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

@Keep
class ShortUrlPost(var url: String) {
}

@Keep
class ShortLinkResponse() {
    lateinit var hashid: String
    lateinit var url: String
    lateinit var created_at: String
}

@Keep
class RetroFitClient {

    companion object {
        private lateinit var retrofit: Retrofit
        private var BASE_URL: String = "https://rel.ink/"

        fun getRetrofitInstance(): Retrofit {
            retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            return retrofit
        }
    }

    @Keep
    interface ShortLink {
        @POST("api/links/")
        fun getShortLink(@Body shortLink: ShortUrlPost): Call<ShortLinkResponse>
    }
}