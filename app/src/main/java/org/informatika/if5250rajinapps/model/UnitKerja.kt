package org.informatika.if5250rajinapps.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.PropertyName

data class UnitKerja (
    var idDoc: String? = null,
    var level: String? = null,
    var nama: String? = null,
    var parent: DocumentReference? = null,

    @get:PropertyName("batas_wilayah") @set:PropertyName("batas_wilayah") @PropertyName("batas_wilayah")
    var batasWilayah: List<Wilayah>? = null,
) {

    override fun toString(): String {
        return "$nama"
    }

    companion object {
        const val COLLECTION_NAME = "unit_kerja"
    }
}