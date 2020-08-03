package com.example.pruebaslocalizacion

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCourseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
    //private val permisoCamara = android.Manifest.permission.CAMERA
    private val CODIGO_SOLICITUD_PERMISO = 100
    var fusedLocationClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    var callback: LocationCallback? = null
    val API_KEY = R.string.google_maps_key

    //marcadores del mapa
    private var marcadorGolder: Marker? = null
    private var marcadorPiramides: Marker? = null
    private var marcadorTorre: Marker? = null
    private var listaMarcadores: ArrayList<Marker>? = null
    private var miposicion: LatLng? = null

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = FusedLocationProviderClient(this)
        inicializarLocationRequest()

        callback = object: LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)

                if(mMap != null){
                    //mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    for (ubicacion in p0?.locations!!){
                        Toast.makeText(applicationContext,ubicacion.latitude.toString() + ", " + ubicacion.longitude.toString(),
                            Toast.LENGTH_LONG).show()
                        miposicion = LatLng(ubicacion.latitude, ubicacion.longitude)
                        mMap.addMarker(MarkerOptions().position(miposicion!!).title("Aqui estoy"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(miposicion))
                    }
                }
            }
        }
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
        //muestra las calles y el mapa satelital
        //mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        marcadoresEstaticos()
        crearListeners()
        prepararMarcadores()
        dibujarLineas()
    }

    override fun onMarkerClick(marcador: Marker?): Boolean {
        var numeroClicks = marcador?.tag as? Int

        if(numeroClicks != null){
            numeroClicks++
            marcador?.tag = numeroClicks
            Toast.makeText(this, "Se han daddo " + numeroClicks.toString() + " clicks",Toast.LENGTH_LONG).show()
        }

        return false
    }

    //-------------------------------------------------------------------------------
    private fun dibujarLineas(){
        //val coordenadas = PolylineOptions().add(LatLng())
    }

    private fun crearListeners(){
        mMap.setOnMarkerClickListener(this)
    }

    private fun marcadoresEstaticos(){
        val GOLDEN_GATE = LatLng(37.8199286,-122.4782551)
        val PIRAMIDES = LatLng(29.9772962,31.1324955)
        val TORRE_PISA = LatLng(43.722952,10.396597)

        marcadorGolder = mMap.addMarker(MarkerOptions()
            .position(GOLDEN_GATE)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ubicacion_32))
            .snippet("Metro de San Francisco")
            .alpha(1f)
            .title("Golden Gate"))
        marcadorGolder?.tag = 0
        marcadorPiramides = mMap.addMarker(MarkerOptions()
            .position(PIRAMIDES)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ubicacion_32))
            .alpha(0.6f)
            .title("PIRAMIDES DE GUIZA"))
        marcadorPiramides?.tag = 0
        marcadorTorre = mMap.addMarker(MarkerOptions()
            .position(TORRE_PISA)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ubicacion_32))
            .alpha(0.9f)
            .title("TORRE DE PISA"))
        marcadorTorre?.tag = 0
    }

    private fun prepararMarcadores(){
        listaMarcadores = ArrayList()
        mMap.setOnMapLongClickListener {
            location: LatLng? ->

            listaMarcadores?.add(mMap.addMarker(MarkerOptions()
                                .position(location!!)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ubicacion_32))
                                .alpha(0.9f)
                                .title("TORRE DE PISA"))
            )
            listaMarcadores?.last()!!.isDraggable = true
            val coordenadas = LatLng(listaMarcadores?.last()!!.position.latitude,listaMarcadores?.last()!!.position.longitude)

            val origen = "origin=" + miposicion?.latitude + "," + miposicion?.longitude + "&"
            val destino = "destination=" + coordenadas.latitude + "," + coordenadas.longitude + "&"
            val key = "key=AIzaSyAeB28AE1Xw3Ert5DOBYsO_EO_oQz1PFSw&"

            val parametros = origen + destino + key + "&sensor=false&mode=driving"

            cargarURL("http://maps.googleapis.com/maps/api/directions/json?" + parametros)
        }
    }

    private fun inicializarLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest?.interval = 10000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun validarPermisosUbicacion() : Boolean{
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(this,permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(this,permisoCourseLocation) == PackageManager.PERMISSION_GRANTED
        //val haycamara = ActivityCompat.checkSelfPermission(this,permisoCamara) == PackageManager.PERMISSION_GRANTED

        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion(){
        /*fusedLocationClient?.lastLocation?.addOnSuccessListener(this,object:OnSuccessListener<Location>{
            override fun onSuccess(location: Location?) {
                //TODO("Not yet implemented")
                if(location != null){
                    Toast.makeText(applicationContext, location?.latitude.toString() + " - " + location?.longitude.toString(),Toast.LENGTH_LONG).show()
                }
            }
        })*/
        fusedLocationClient?.requestLocationUpdates(locationRequest, callback, null)
    }

    private fun pedirPermisos(){
        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this, permisoFineLocation)

        if(deboProveerContexto){
            //mandar un mensaje con explicacion adicional
            solicitudPermiso()
        } else {
            solicitudPermiso()
        }
    }

    @SuppressLint("NewApi")
    private fun solicitudPermiso(){
        requestPermissions(arrayOf(permisoFineLocation,permisoCourseLocation), CODIGO_SOLICITUD_PERMISO)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CODIGO_SOLICITUD_PERMISO ->{
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //obtener ubicacion
                    obtenerUbicacion()
                } else {
                    Toast.makeText(this,"No diste los permisos", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun detenerActualizacionUbicacion(){
        fusedLocationClient?.removeLocationUpdates(callback)
    }

    private fun cargarURL(url:String){
        val queue = Volley.newRequestQueue(this)

        val solicitud = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                Log.d("HTTP", response)

                val coordenadas = obtenerCoordenadas(response)

                mMap.addPolyline(coordenadas)

            }, Response.ErrorListener {  })
        queue.add(solicitud)
    }

    private fun obtenerCoordenadas(json: String) : PolylineOptions{
        val gson = Gson()
        /*
        val objeto = gson.fromJson(json, com.example.pruebaslocalizacion.Response::class.java)

        val puntos = objeto.routes?.get(0)!!.legs?.get(0)!!.steps!!
*/
        var coordenadas = PolylineOptions()
/*
        for(punto in puntos){
            coordenadas.add(punto.start_location?.toLatLng())
            coordenadas.add(punto.end_location?.toLatLng())
        }

        coordenadas.color(Color.CYAN).width(15f)
*/
        return coordenadas
    }

    /*private fun decodePoly(encoded:String): List<GeoPoint>{

    }*/

    override fun onStart() {
        super.onStart()

        if(validarPermisosUbicacion()){
            obtenerUbicacion()
        } else {
            pedirPermisos()
        }
    }

    override fun onPause() {
        super.onPause()
        detenerActualizacionUbicacion()
    }


}