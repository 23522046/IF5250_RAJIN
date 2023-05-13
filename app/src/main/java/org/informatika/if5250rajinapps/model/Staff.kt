package org.informatika.if5250rajinapps.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.PropertyName

data class Staff(
    @get:PropertyName("is_aktif") @set:PropertyName("is_aktif") @PropertyName("is_aktif")
    var isAktif: Boolean? = null,

    @get:PropertyName("nama") @set:PropertyName("nama") @PropertyName("nama")
    var nama: String? = null,

    @get:PropertyName("no_induk") @set:PropertyName("no_induk") @PropertyName("no_induk")
    var noInduk: String? = null,

    @get:PropertyName("player_id") @set:PropertyName("player_id") @PropertyName("player_id")
    var playerId: String? = null,

    @get:PropertyName("time_create") @set:PropertyName("time_create") @PropertyName("time_create")
    var timeCreate: Timestamp? = null,

    @get:PropertyName("unit_kerja") @set:PropertyName("unit_kerja") @PropertyName("unit_kerja")
    var unitKerja: DocumentReference? = null,

    @get:PropertyName("UID") @set:PropertyName("UID") @PropertyName("UID")
    var UID: String? = null,

    var unitKerjaData: UnitKerja? = null
) {

    override fun toString(): String {
        return "UID : $UID, noInduk : $noInduk, nama : $nama, unitKerja : ${unitKerja?.path}, isAktif : $isAktif, playerId : $playerId, timeCreate : $timeCreate"
    }

    companion object {
        const val COLLECTION_NAME = "staff"
        const val UID = "UID"
    }
}