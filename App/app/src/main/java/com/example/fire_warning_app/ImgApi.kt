import retrofit2.Call
import retrofit2.http.GET

interface ImgApi {
    @GET("all")
    fun getFireImages(): Call<List<FireImg>>

    data class FireImg(
        val img: String,
        val timestamp: String
    )
}
