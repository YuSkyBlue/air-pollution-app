package org.techtown.air.pollution.app.data.services

import org.techtown.air.pollution.app.BuildConfig
import org.techtown.air.pollution.app.data.models.tmcoordinates.TmCoordinateResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface KakaoLocalApiService {

        @Headers("Authorization: KakaoAK ${BuildConfig.KAKAO_API_KEY}")
        @GET("v2/local/geo/transcoord.json?output_coord=TM")
        suspend fun getTmCoordinates(
           @Query("x")  longitude : Double,
           @Query("y") latitude: Double
        ) : Response<TmCoordinateResponse>


}