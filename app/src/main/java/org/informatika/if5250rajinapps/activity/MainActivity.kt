package org.informatika.if5250rajinapps.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import org.informatika.if5250rajinapps.R
import org.informatika.if5250rajinapps.databinding.ActivityMainBinding

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    private var query: Query? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        Log.d(TAG, "onCreate: ${Firebase.auth.currentUser?.uid}")
        if (mAuth.currentUser!=null){
            moveToMenuActivity()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val btLogin = binding.buttonLogin
        btLogin.setOnClickListener {
            try {
                val username = binding.etUsename.text.toString()
                if (TextUtils.isEmpty(username)) throw Exception("Email tidak boleh kosong")
                val password = binding.etPassword.text.toString()
                if (TextUtils.isEmpty(password)) throw Exception("Password tidak boleh kosong")

                Log.d(TAG, "username: ${binding.etUsename.text.toString()}, password : ${binding.etPassword.text.toString()}")

                loginUserAccount(username, password)
            } catch (e: Exception) {
                Snackbar.make(
                    findViewById(R.id.activity_main),
                    "${e.localizedMessage}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        binding.tvSignUp.setOnClickListener { moveToSignUpActivity() }
    }

    private fun moveToSignUpActivity() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun loginUserAccount(username: String, password: String) {
        binding.buttonLogin.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        mAuth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener {

                if (it.isSuccessful) {
                    Toast.makeText(applicationContext, "Login successful", Toast.LENGTH_SHORT).show()
                    moveToMenuActivity()
                } else {
                    Snackbar.make(
                        findViewById(R.id.activity_main),
                        "Login failed!!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "loginUserAccount: ${it.exception?.localizedMessage}")
                }

                binding.buttonLogin.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun moveToMenuActivity() {
        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }
}