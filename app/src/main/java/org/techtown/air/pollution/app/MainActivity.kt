package org.techtown.air.pollution.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.techtown.air.pollution.app.data.Repository
import org.techtown.air.pollution.app.data.models.airquality.Gradle
import org.techtown.air.pollution.app.data.models.airquality.MeasuredValue
import org.techtown.air.pollution.app.data.models.monitoringstation.MonitroingStation
import org.techtown.air.pollution.app.databinding.ActivityMainBinding
import kotlin.Exception

class MainActivity : AppCompatActivity() {
    private  lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater)}

    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bindViews()
        initVariables()
        requestLocationPermissions() // 앱을 실행하자마자 onRequest 실행 요청하고 permission 부여받았으면 다음진행단계이동
    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource?.cancel()
        scope.cancel()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val locationPermissonGranted =
            requestCode == REQUEST_ACCESS_LOCATION_PERMISSIONS &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED

        val backgroundLocationPermissionGranted =
            requestCode == REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!backgroundLocationPermissionGranted){
                requestBackgroundLocationPermissions()
            }else{
                fetchAirQuailtyData()
            }

        } else{
            if(!locationPermissonGranted){
            finish()
        } else{
            fetchAirQuailtyData() //권한을 요청해서 권한이 있을때 이함수를 실행
         }
        }
    }
    private fun bindViews(){
        binding.refresh.setOnRefreshListener {
            fetchAirQuailtyData()
        }
    }

    private  fun initVariables(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    }
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_ACCESS_LOCATION_PERMISSIONS
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS
        )
    }
    @SuppressLint("MissingPermission")
    private fun fetchAirQuailtyData(){

        // fetchData
        cancellationTokenSource = CancellationTokenSource()

        fusedLocationProviderClient
            .getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource!!.token
            ).addOnSuccessListener {location ->
                scope.launch {
                    binding.errorDescriptionTextView.visibility = View.GONE // 재시작 처리
                    try{
                        val monitroingStation =
                        Repository.getNearbyMonitoringStation(location.latitude,location.longitude)

                    val measuredValue =
                        Repository.getLatestAirQualityData(monitroingStation!!.stationName!!)

                    displayAirQualityData(monitroingStation, measuredValue!!)
                    } catch (exception : Exception){
                        binding.errorDescriptionTextView.visibility = View.VISIBLE
                        binding.contentsLayout.alpha= 0F

                    }finally {
                        binding.progresBar.visibility = View.GONE
                        binding.refresh.isRefreshing = false
                    }
                }
            }

    }
    @SuppressLint("SetTextI18n")
    fun displayAirQualityData(monitoringStation: MonitroingStation, measuredValue: MeasuredValue){
        binding.contentsLayout.animate()
            .alpha(1F)
            .start()

        binding.measuringStationNameTextView.text =monitoringStation.stationName
        binding.measuringStationAddressTextView.text = monitoringStation.addr

        (measuredValue.khaiGrade?: Gradle.UNKNOWN).let { gradle ->
            binding.root.setBackgroundResource(gradle.colorResId)
            binding.totalGradeLabelTextView.text= gradle.label
            binding.totalGradleEmojiTextView.text=gradle.emoji

        }
        with(measuredValue){
            binding.fineDustInformationTextView.text =
                "미세먼지: $pm10Value ㎍/㎥ ${(pm10Grade ?: Gradle.UNKNOWN).emoji}"
            binding.ultraFineDustInformationTextView.text =
            "초미세먼지: $pm25Value ㎍/㎥ ${(pm25Grade ?: Gradle.UNKNOWN).emoji} "

            with(binding.so2Item){
                labelTextView.text = "아황산가스"
                gradeTextView.text = (so2Grade ?: Gradle.UNKNOWN).toString()
                valueTextView.text = "$so2Value ppm"
            }

            with(binding.coItem){
                labelTextView.text = "일산화탄소"
                gradeTextView.text = (coGrade ?: Gradle.UNKNOWN).toString()
                valueTextView.text = "$coValue ppm"
            }
            with(binding.o3Item){
                labelTextView.text = "오존"
                gradeTextView.text = (o3Grade ?: Gradle.UNKNOWN).toString()
                valueTextView.text = "$o3Value ppm"
            }
            with(binding.no2Item){
                labelTextView.text = "이산화질소"
                gradeTextView.text = (no2Grade ?: Gradle.UNKNOWN).toString()
                valueTextView.text = "$no2Value ppm"
            }
        }

    }
    companion object {
        private const val  REQUEST_ACCESS_LOCATION_PERMISSIONS = 100
        private const val  REQUEST_BACKGROUND_ACCESS_LOCATION_PERMISSIONS = 101

    }
}