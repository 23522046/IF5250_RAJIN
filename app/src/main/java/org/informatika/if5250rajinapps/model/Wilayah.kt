package org.informatika.if5250rajinapps.model

import com.google.firebase.firestore.GeoPoint

data class Wilayah (
    var nama: String? = null,
    var polygons: List<GeoPoint>? = null,
) {

    override fun toString(): String {
        return "nama : $nama, polygons_length : ${polygons?.size}"
    }
}