package org.informatika.if5250rajinapps.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Pengajuan(

    @get:PropertyName("dok_pendukung") @set:PropertyName("dok_pendukung") @PropertyName("dok_pendukung")
    var dokPendukung: ArrayList<String>? = null,

    var jenis: String? = null,

    @get:PropertyName("mulai_tanggal") @set:PropertyName("mulai_tanggal") @PropertyName("mulai_tanggal")
    var mulaiTgl: Timestamp? = null,

    @get:PropertyName("sampai_tanggal") @set:PropertyName("sampai_tanggal") @PropertyName("sampai_tanggal")
    var sampaiTgl: Timestamp? = null,

    var status: String? = null,

    @get:PropertyName("time_create") @set:PropertyName("time_create") @PropertyName("time_create")
    var timeCreate: Timestamp? = null,

    var uid: String? = null
) {

    override fun toString(): String {
        return "uid : $uid, dok_pendukung_size : ${dokPendukung?.size}, jenis : $jenis, mulai_tanggal : ${mulaiTgl.toString()}, sampai_tanggal : ${sampaiTgl.toString()}, status : $status"
    }

    companion object {
        const val COLLECTION_NAME = "pengajuan"
        const val FIELD_TIME_CREATE = "time_create"
        const val FIELD_UID = "uid"
        const val STATUS_TERKIRIM = "terkirim"
    }
}