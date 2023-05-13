package org.informatika.if5250rajinapps.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

data class Cek(
    var waktu: Timestamp? = null,

    @get:PropertyName("is_mock_location") @set:PropertyName("is_mock_location") @PropertyName("is_mock_location")
    var isMockLocation: Boolean? = null,
    var location: GeoPoint? = null
) {

}