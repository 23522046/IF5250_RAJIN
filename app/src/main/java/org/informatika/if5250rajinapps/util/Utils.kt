package org.informatika.if5250rajinapps.util

import android.content.Context
import android.location.Location
import androidx.core.content.edit
import com.google.firebase.Timestamp
import org.informatika.if5250rajinapps.R
import org.informatika.if5250rajinapps.model.Pengajuan
import java.text.SimpleDateFormat
import java.util.*

val Date?.formattedYMD: String
    get() {
        val sfd = SimpleDateFormat("yyyy-MM-dd")
        return sfd.format(this)
    }

val Timestamp?.formattedDateOnly: String
    get() {
        val sfd = SimpleDateFormat("EEEE, dd MMMM yyyy")
        return sfd.format(this?.toDate()?.time?.let { Date(it) })
    }

val Date?.formattedDateOnly: String
    get() {
        val sfd = SimpleDateFormat("EEEE, dd MMMM yyyy")
        return sfd.format(this)
    }

val Timestamp?.formattedTimeOnly: String
    get() {
        val sfd = SimpleDateFormat("HH:mm:ss")
        return sfd.format(this?.toDate()?.time?.let { Date(it) })
    }

val String?.toDateOnly: Date
    get() {
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.parse(this)
    }

internal object SharedPreferenceUtil {
    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The [Context].
     */
    fun getLocationTrackingPref(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            .getBoolean(KEY_FOREGROUND_ENABLED, false)

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE).edit {
            putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
        }
}

fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}

fun dokPendukungPath(pengajuan: Pengajuan) =
    "pengajuan/${pengajuan.sampaiTgl!!.toDate().formattedYMD}_${pengajuan.mulaiTgl!!.toDate().formattedYMD}_${pengajuan.uid}.jpg"