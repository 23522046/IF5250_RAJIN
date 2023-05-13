package org.informatika.if5250rajinapps.util

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.informatika.if5250rajinapps.BuildConfig

class FirestoreInitializer : Initializer<FirebaseFirestore> {

    // The host '10.0.2.2' is a special IP address to let the
    // Android emulator connect to 'localhost'.
    private val FIRESTORE_EMULATOR_HOST = "192.168.18.100"
    private val FIRESTORE_EMULATOR_PORT = 8080

    override fun create(context: Context): FirebaseFirestore {
        val firestore = Firebase.firestore
        // Use emulators only in debug builds
        if (BuildConfig.DEBUG) {
//            firestore.useEmulator(FIRESTORE_EMULATOR_HOST, FIRESTORE_EMULATOR_PORT)
        }
        return firestore
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()

}