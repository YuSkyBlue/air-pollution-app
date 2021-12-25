package org.techtown.air.pollution.app.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.techtown.air.pollution.app.BuildConfig
import org.techtown.air.pollution.app.data.models.airquality.MeasuredValue
import org.techtown.air.pollution.app.data.models.monitoringstation.MonitroingStation
import org.techtown.air.pollution.app.data.services.AirKoreaApiService
import org.techtown.air.pollution.app.data.services.KakaoLocalApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object Repository {

    suspend fun getNearbyMonitoringStation(latitude: Double, longitude : Double): MonitroingStation?{
        val tmCoordintaes = kakaoLocalApiService
            .getTmCoordinates(longitude, latitude)
            .body()
            ?.documents
            ?.firstOrNull()

        val tmX = tmCoordintaes?.x
        val tmY = tmCoordintaes?.y

        return airKoreaApiService // 가장 가까운 측정소 가져오는 로직
            .getNearbyMonitoringService(tmX!!,tmY!!)
            .body()
            ?.response
            ?.body
            ?.monitroingStations
            ?.minByOrNull { it.tm ?: Double.MAX_VALUE }
    }
    suspend fun getLatestAirQualityData(stationName: String): MeasuredValue? =
        airKoreaApiService
            .getRealtimeAirQualties(stationName)
            .body()
            ?.response
            ?.body
            ?.measuredValues
            ?.firstOrNull()
    private val kakaoLocalApiService: KakaoLocalApiService by lazy{
        Retrofit.Builder()
            .baseUrl(Url.KAKAO_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient())
            .build()
            .create()
    }

    private val airKoreaApiService: AirKoreaApiService by lazy{
        Retrofit.Builder()
            .baseUrl(Url.AIR_KOREA_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient())
            .build()
            .create()
    }

    private  fun buildHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if(BuildConfig.DEBUG){
                        HttpLoggingInterceptor.Level.BODY
                    }else{
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()
}