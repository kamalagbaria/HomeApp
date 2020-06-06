package com.example.homeapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MainActivity : AppCompatActivity() {

    var locationManager: LocationManager?=null
    lateinit var locationListener:LocationListener
    private val PERMISSION_REQUEST = 100
    private var hasGps = false
    private var locationGps: Location? = null
    var prefs: SharedPreferences? = null

    lateinit var btn_get_location:Button
    lateinit var text_latitude:TextView
    lateinit var text_longtitude:TextView
    lateinit var text_accuracy:TextView
    private lateinit var homeLocation:Button
    private lateinit var saveHome:TextView
    private lateinit var clearLocation:Button

    val LATITUDE:String="Latitude: ";
    val LONGTITUDE:String="Longitude: ";
    val ACCURACY:String="Accuracy: ";

    var homeLongitude:String="0"
    var homeLatitude:String="0"

    var stopTracking:Boolean=false

    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_get_location=findViewById(R.id.button) as Button
        text_longtitude=findViewById(R.id.textView3) as TextView
        text_latitude=findViewById(R.id.textView) as TextView
        text_accuracy=findViewById(R.id.textView2) as TextView
        homeLocation=findViewById(R.id.HomeButton) as Button
        saveHome=findViewById(R.id.homeText) as TextView
        clearLocation=findViewById(R.id.ClearButton) as Button
        init()
    }
    private fun init(){
        prefs = this.getPreferences(Context.MODE_PRIVATE)
        homeLongitude=getLocation("Longitude")
        homeLatitude=getLocation("Latitude")
        if (homeLatitude!="0" && homeLongitude !="0"){
            saveHome.visibility=View.VISIBLE
            saveHome.text=getString(R.string.your_home_location_is_defined_as).plus("<".plus(homeLongitude).plus(",".plus(homeLatitude).plus(">")))
            clearLocation.visibility=View.VISIBLE
        }
    }

    private fun enableView() {
        btn_get_location.text=getString(R.string.Stop_Tracking)
        getLocation()
        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[0])
                    if (requestAgain) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    } else {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Location Permission")
                        builder.setMessage("The app is all about tracking your location, without your permission the app will be useless, now you have to go to settings to give us permission ")

                        builder.setNeutralButton("I'll think about it"){dialog, which ->
                        }
                        builder.setPositiveButton("Go to settings"){dialog, which ->
                            startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                        val dialog: AlertDialog = builder.create()
                        dialog.show()
                    }
            }
            if (allSuccess)
                enableView()
        }
    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        locationListener=object : LocationListener{
            override fun onLocationChanged(location: Location?) {
                if (location != null) {
                    locationGps = location
                    text_latitude.text= LATITUDE.plus(locationGps!!.latitude.toString())
                    text_longtitude.text= LONGTITUDE.plus(locationGps!!.longitude.toString())
                    text_accuracy.text= ACCURACY.plus(locationGps!!.accuracy.toString())
                    if(locationGps!!.accuracy.toString().toDouble()<50.0){
                        homeLatitude=locationGps!!.latitude.toString()
                        homeLongitude=locationGps!!.longitude.toString()
                        homeLocation.visibility=View.VISIBLE
                    }
                    else{
                        homeLocation.visibility=View.INVISIBLE
                    }
                }
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String?) {}
            override fun onProviderDisabled(provider: String?) {}
        }
         if (hasGps) {
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, locationListener)
                val localGpsLocation =
                    locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null)
                    locationGps = localGpsLocation
            }
            if(locationGps!= null){
                    text_latitude.text= LATITUDE.plus(locationGps!!.latitude.toString())
                    text_longtitude.text= LONGTITUDE.plus(locationGps!!.longitude.toString())
                    text_accuracy.text= ACCURACY.plus(locationGps!!.accuracy.toString())
                    if(locationGps!!.accuracy.toString().toDouble()<50.0){
                        homeLatitude=locationGps!!.latitude.toString()
                        homeLongitude=locationGps!!.longitude.toString()
                        homeLocation.visibility=View.VISIBLE
                    }
                    else{
                        homeLocation.visibility=View.INVISIBLE
                    }
            }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun startLocation(view: View) {
        if (btn_get_location.text == getString(R.string.Start_Tracking)){
            if (checkPermission(permissions)) {
                enableView()
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        }
        else{
            if (btn_get_location.text.equals(getString(R.string.Stop_Tracking))){
                btn_get_location.text=getString(R.string.Start_Tracking)
                locationManager?.removeUpdates(locationListener)
                locationManager=null
                homeLocation.visibility=View.INVISIBLE
                text_latitude.text= LATITUDE.plus(" :")
                text_longtitude.text= LONGTITUDE.plus(" :")
                text_accuracy.text= ACCURACY.plus(" :")

            }
        }
        }
    fun saveLocation(view: View) {
            save(homeLongitude,"Longitude")
            save(homeLatitude,"Latitude")
            saveHome.visibility=View.VISIBLE
            saveHome.text=getString(R.string.your_home_location_is_defined_as).plus("<".plus(homeLongitude).plus(",".plus(homeLatitude).plus(">")))
            clearLocation.visibility=View.VISIBLE

        }
    fun save(value:String,key:String) {
        val editor = prefs?.edit()
        val gson = Gson()
        val json = gson.toJson(value)
        if (editor != null) {
            editor.putString(key, json)
        }
        if (editor != null) {
            editor.apply()
        }
    }

    fun getLocation(key: String?): String {
        val gson = Gson()
        val json = prefs?.getString(key, "0")
        val type: Type = object : TypeToken<String?>() {}.type
        return gson.fromJson(json, type)
    }

    fun ClearLocation(view: View) {
        homeLatitude="0"
        homeLongitude="0"
        saveHome.visibility=View.INVISIBLE
        clearLocation.visibility=View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(locationListener)
        locationManager=null
    }

}


