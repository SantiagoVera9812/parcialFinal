package com.example.parcial.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.parcial.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.PolylineOptions
import java.util.Locale

class MapsFragment : Fragment() {

    private var userMarker: Marker? = null
    private var distanceString: String? = null
    private lateinit var gmap: GoogleMap
    private var polylineOptions: PolylineOptions? = null
    private val polylineList: MutableList<LatLng> = mutableListOf()
    private var inicialMarker: Marker? = null
    private var distanceListener: DistanceListener? = null


    var listener: ((category: String) -> String)? = null

    interface DistanceListener {
        fun onDistanceUpdated(distance: String)
    }

    fun sendCategoryAndDistanceToListener(category: String) {
        listener?.invoke(category)
        distanceListener?.onDistanceUpdated(distanceString ?: "")
    }

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        gmap = googleMap
        gmap.uiSettings.isZoomControlsEnabled = false
        gmap.uiSettings.isCompassEnabled = true


        val sydney = LatLng(4.627835831777713, -74.06409737200865)

        polylineList.add(sydney)
        val markerOptions = MarkerOptions()
            .position(sydney)
            .icon(context?.let { bitmapDescriptorFromVector(it, R.drawable.baseline_add_location_24) })

        userMarker = gmap.addMarker(markerOptions)

        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15f))

        gmap.setOnMapLongClickListener { latLng ->

            polylineList.add(latLng)

            val (start, end) = getLastAndSecondToLastLatLng()

            val locationA = Location("punto a")

            locationA.latitude = latLng.latitude
            locationA.longitude = latLng.longitude

            val locationB = Location("punto b")
            locationB.latitude = latLng.latitude
            locationB.latitude = latLng.longitude

            getDistancePointAPointB(locationA, locationB)

            val pattern: List<PatternItem> = listOf(
                Dot(), Gap(10f), Dash(30f), Gap(10f)
            )

            val polylineOptionsInt = PolylineOptions()
                .add(start)
                .add(end)
                .color(Color.GRAY) // Set the color of the polyline
                .width(20f)
                .pattern(pattern)

            gmap.addPolyline(polylineOptionsInt)

            val markerOptions = MarkerOptions()
                .position(latLng)
                .icon(context?.let { bitmapDescriptorFromVector(it, R.drawable.baseline_add_location_alt_24) })

            inicialMarker?.remove()
            inicialMarker = gmap.addMarker(markerOptions)
        }

    }

    fun getDistancePointAPointB(locationA: Location, locationB: Location){

        val distance = locationA.distanceTo(locationB)

        distanceString = "Distance: $distance"

    }

    fun getDistance(): String?{

        return distanceString
    }

    fun deleteMap(){
        gmap.clear()
        polylineList.clear()
        val sydney = LatLng(4.627835831777713, -74.06409737200865)

        val markerOptions = MarkerOptions()
            .position(sydney)
            .icon(context?.let { bitmapDescriptorFromVector(it, R.drawable.baseline_add_location_24) })

        polylineList.add(sydney)

        userMarker = gmap.addMarker(markerOptions)
    }

    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    fun getLastAndSecondToLastLatLng(): Pair<LatLng?, LatLng?> {
        return if (polylineList.size >= 2) {
            val lastLatLng = polylineList[polylineList.size - 1]
            val secondToLastLatLng = polylineList[polylineList.size - 2]
            Pair(secondToLastLatLng, lastLatLng)
        } else {
            // Return null if there are not enough elements
            Pair(null, null)
        }
    }


}