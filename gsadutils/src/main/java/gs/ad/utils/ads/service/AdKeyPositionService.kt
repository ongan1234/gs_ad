package gs.ad.utils.ads.service

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url


interface AdKeyPositionService {
    @GET("{endpoint}")
    fun listPosition(@Path(value = "endpoint", encoded = true) endpoint: String): Call<List<AdKeyPositionData>>
}