package com.example.pruebaslocalizacion


import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest

import com.android.volley.toolbox.Volley

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject


class MapsActivity2 : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps2)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val sydney = LatLng(-34.0, 151.0)
        val opera = LatLng(-33.9320447,151.1597271)

        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))

        mMap!!.addMarker(MarkerOptions().position(opera).title("Opera House"))


        //Empieza

        val origen = "origin=" + sydney?.latitude + "," + sydney?.longitude + "&"
        val destino = "destination=" + opera.latitude + "," + opera.longitude + "&"
        val key = "key=AIzaSyAeB28AE1Xw3Ert5DOBYsO_EO_oQz1PFSw&"

        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?" + origen + destino + key + "&sensor=false&mode=driving"
        val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener<String> {
                response ->
            val jsonResponse = JSONObject(response)
            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")
            for (i in 0 until steps.length()) {
                val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                path.add(PolyUtil.decode(points))
            }
            for (i in 0 until path.size) {
                this.mMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            }
        }, Response.ErrorListener {
                _ ->
        }){}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
    }
}