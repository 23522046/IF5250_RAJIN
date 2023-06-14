package org.informatika.if5250rajinapps.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import org.informatika.if5250rajinapps.model.Pengajuan
import org.informatika.if5250rajinapps.util.FirebaseService
import org.informatika.if5250rajinapps.util.dokPendukungPath
import org.informatika.if5250rajinapps.util.formattedYMD

class RequestViewModel : ViewModel() {
    private val _pengajuan = MutableLiveData<Pengajuan?>(null)
    val pengajuan: LiveData<Pengajuan?> = _pengajuan

    fun simpanPengajuan(imageUri: Uri, pengajuan: Pengajuan){

        // upload foto
        FirebaseService.uploadFotoPengajuan(imageUri, pengajuan).addOnSuccessListener {

            val storageRef = Firebase.storage.reference;
            // jika berhasil, simpan record ke firestore
            val sd = dokPendukungPath(pengajuan)

            storageRef.child(sd).downloadUrl.addOnSuccessListener {
                Log.i("Firebase", "download passed : $it")
                pengajuan.dokPendukung = ArrayList()
                pengajuan.dokPendukung!!.add(it.toString())

                FirebaseService.addPengajuan(pengajuan).addOnFailureListener {
                    Log.e("RequestViewModel", "Error addPengajuan ", it)
                }
            }.addOnFailureListener {
                Log.e("Firebase", "Failed in downloading")
            }

        }.addOnFailureListener {
            Log.e("Firebase", "Error uploadFotoPengajuan", it)
        }


    }
}