package com.squadup.squadup.data

import com.google.android.gms.maps.model.LatLng

class Constants {

    companion object {

        val METERS_PER_DEGREE = 111319.9

        val MEETING_LOCATIONS = mapOf(
                Pair("MacDonald Commons", LatLng(40.95231, -76.880407)),
                Pair("Academic West", LatLng(40.953678, -76.881673)),
                Pair("Bertrand Library", LatLng(40.954404, -76.882761)),
                Pair("Dana Engineering", LatLng(40.955285, -76.88189)),
                Pair("Rooke Science Building", LatLng(40.95601, -76.88319)),
                Pair("Elaine Langone Center", LatLng(40.956436, -76.884541)),
                Pair("7th Street Cafe", LatLng(40.957906, -76.884733)),
                Pair("Bucknell Bookstore", LatLng(40.963851, -76.886296))
        )

    }

}