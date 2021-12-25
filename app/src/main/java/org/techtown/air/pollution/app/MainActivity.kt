package org.techtown.air.pollution.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.techtown.air.pollution.app.data.Repository
import org.techtown.air.pollution.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private  lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater)}

    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
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

        if(!locationPermissonGranted){
            finish()
        } else{
            fetchAirQuailtyData() //권한을 요청해서 권한이 있을때 이함수를 실행
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
                    val monitroingStation =
                        Repository.getNearbyMonitoringStation(location.latitude,location.longitude)

                    val measuredValue =
                        Repository.getLatestAirQualityData(monitroingStation!!.stationName!!)

                    binding.textView.text = measuredValue.toString()
                }
            }

    }

    companion object {
        private const val  REQUEST_ACCESS_LOCATION_PERMISSIONS = 100
    }
}