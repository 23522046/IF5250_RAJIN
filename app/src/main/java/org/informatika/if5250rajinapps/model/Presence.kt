package org.informatika.if5250rajinapps.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import org.informatika.if5250rajinapps.util.formattedDateOnly
import org.informatika.if5250rajinapps.util.formattedTimeOnly
import java.util.*

data class Presence(

    var UID: String? = null,
    var jenis: String? = null,
    var ket: String? = null,

    @get:PropertyName("check_in") @set:PropertyName("check_in") @PropertyName("check_in")
    var checkIn: Cek? = null,

    @get:PropertyName("check_out") @set:PropertyName("check_out") @PropertyName("check_out")
    var checkOut: Cek? = null,

    @get:PropertyName("is_lembur") @set:PropertyName("is_lembur") @PropertyName("is_lembur")
    var isLembur: Boolean? = null,

    @get:PropertyName("time_create") @set:PropertyName("time_create") @PropertyName("time_create")
    var timeCreate: Timestamp? = null,

    @get:PropertyName("time_update") @set:PropertyName("time_update") @PropertyName("time_update")
    var timeUpdate: Timestamp? = null
) {

    override fun toString(): String {
        return "UID : $UID, jenis : $jenis, isLembur : $isLembur, checkInTime : ${checkIn?.waktu?.toDate()}, checkInLoc : GeoPoint(${checkIn?.location?.latitude}, ${checkIn?.location?.longitude})"
    }

    companion object {
        const val COLLECTION_NAME = "presensi"
        const val FIELD_TIME_CREATE = "time_create"
        const val FIELD_UID = "uid"
    }

    fun checkInTime() : String =  if (checkIn != null){ checkIn?.waktu?.formattedTimeOnly!! } else { "-" }

    fun checkOutTime() : String {
        if (checkOut!=null){
            return checkOut!!.waktu!!.formattedTimeOnly
        }
        return "-"
    }

    fun timeCreateTime() : String {
        if (timeCreate!=null){
            return timeCreate!!.formattedDateOnly
        }
        return Date().formattedDateOnly
    }

    fun durasiKerja() : String {
        if (checkOut!=null){
            val startDate: Date = checkIn?.waktu!!.toDate()
            val endDate: Date = checkOut!!.waktu!!.toDate()

            val durationInMillis: Long = endDate.time - startDate.time
            val hours: Long = durationInMillis / (1000 * 60 * 60)
            val minutes: Long = durationInMillis / (1000 * 60) % 60
            val seconds: Long = durationInMillis / 1000 % 60

            return "$hours Jam, $minutes Menit"
        }
        return "-"
    }
}