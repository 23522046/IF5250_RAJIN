package org.informatika.if5250rajinapps.util

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.informatika.if5250rajinapps.model.Pengajuan
import org.informatika.if5250rajinapps.model.Presence
import org.informatika.if5250rajinapps.model.Staff
import org.informatika.if5250rajinapps.model.UnitKerja
import java.util.*

object FirebaseService {
    private const val TAG = "FirebaseService"
    suspend fun getStaffLoggedIn(): Staff?{
        val db = FirebaseFirestore.getInstance()
        return try {
            var mAuth = FirebaseAuth.getInstance()

            if (mAuth.currentUser==null) throw Exception("Anda belum login")

            db.collection(Staff.COLLECTION_NAME).document(mAuth.currentUser!!.uid).get().await().toObject(Staff::class.java)
        } catch (e: Exception){
            Log.e(TAG, "Error getting staff", e)
            null
        }
    }

    fun getUser(): Flow<Staff?> = callbackFlow {
        val firestore = FirebaseFirestore.getInstance()

        val listener = object : EventListener<DocumentSnapshot> {
            override fun onEvent(snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException?) {
                if (exception != null) {
                    // An error occurred
                    cancel()
                    return
                }

                if (snapshot != null && snapshot.exists()) {
                    // The user document has data
                    val user = snapshot.toObject(Staff::class.java)
                    trySend(user)
                } else {
                    // The user document does not exist or has no data
                }
            }
        }


        var mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser==null) throw Exception("Anda belum login")
        val registration = firestore.collection(Staff.COLLECTION_NAME).document(mAuth.currentUser!!.uid).addSnapshotListener(listener)
        awaitClose { registration.remove() }
    }

    suspend fun getUserUnitKerja(reference: DocumentReference): UnitKerja? {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.document(reference.path).get().await().toObject(UnitKerja::class.java)
        } catch (e: Exception){
            Log.e(TAG, "Error getting unit_kerja", e)
            null
        }
    }

    suspend fun getPresence(): Presence? {
        val db = FirebaseFirestore.getInstance()
        val date = Date()
        return try {
            var mAuth = FirebaseAuth.getInstance()

            if (mAuth.currentUser==null) throw Exception("Anda belum login")

            db.collection(Presence.COLLECTION_NAME).document("${mAuth.currentUser!!.uid}_${date.formattedYMD}").get().await().toObject(Presence::class.java)
        } catch (e: Exception){
            Log.e(TAG, "Error getting staff", e)
            null
        }
    }

    fun addPresence(p: Presence): Task<Void> {
        val firestore = FirebaseFirestore.getInstance()
        var mAuth = FirebaseAuth.getInstance()
        val date = Date()

        if (mAuth.currentUser==null) throw Exception("Anda belum login")

        var documentReference =  firestore.collection(Presence.COLLECTION_NAME)
            .document("${mAuth.currentUser!!.uid}_${date.formattedYMD}")

        return documentReference.set(p)
    }

    fun addPengajuan(p: Pengajuan): Task<Void> {
        Log.d(TAG, "p: ${p.toString()}")
        val firestore = FirebaseFirestore.getInstance()
        var mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser==null) throw Exception("Anda belum login")

        var documentReference = firestore.collection(Pengajuan.COLLECTION_NAME)
            .document("${mAuth.currentUser!!.uid}_${p.sampaiTgl!!.toDate().formattedYMD}_${p.mulaiTgl!!.toDate().formattedYMD}_${p.uid}")

        return documentReference.set(p)
    }

    fun uploadFotoPengajuan(imageUri: Uri, pengajuan: Pengajuan) : UploadTask{
        // creating a storage reference
        val storageRef = Firebase.storage.reference;

        // val fileName = imageUri?.pathSegments?.last()

        // extract the file name with extension
//        val sd = getFileName(applicationContext, imageUri)
        val sd = dokPendukungPath(pengajuan)

        // Upload Task with upload to directory 'file'
        // and name of the file remains same
        val uploadTask = storageRef.child(sd).putFile(imageUri)

        return uploadTask
    }

}
