package org.informatika.if5250rajinapps.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.informatika.if5250rajinapps.R
import org.informatika.if5250rajinapps.databinding.ActivitySignUpBinding
import org.informatika.if5250rajinapps.model.Staff
import org.informatika.if5250rajinapps.model.UnitKerja
import org.informatika.if5250rajinapps.model.User
import org.informatika.if5250rajinapps.viewmodel.SignUpViewModel


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var viewModel: SignUpViewModel
    private lateinit var selectedUnitKerja: UnitKerja
    private var listUnitKerja: List<UnitKerja> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]

        val etKodeBergabung = binding.etKodeBergabung
        etKodeBergabung.addTextChangedListener(afterTextChanged = {
            loadUnitKerja(it)
        })

        viewModel.unitKerja.observe(this){
            val msg = if (it==null) "Kode tidak valid" else "Instansi ditemukan : ${it.nama}"
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }

        viewModel.unitKerjaChild.observe(this){
            if (it!=null){
                listUnitKerja = it.map { it!! }
                val arrayAdapter =
                    ArrayAdapter(this, R.layout.dropdown_item, listUnitKerja.map { it.nama })
                binding.autoCompleteInstansi.setAdapter(arrayAdapter)
            }
        }

        binding.autoCompleteInstansi.setOnItemClickListener { adapterView, view, i, l -> selectedUnitKerja = listUnitKerja[i] }

        binding.buttonSubmit.setOnClickListener {
            try {
                val username = binding.etUsename.text.toString()
                if (TextUtils.isEmpty(username)) throw Exception("Email tidak boleh kosong")

                val password = binding.etPassword.text.toString()
                if (TextUtils.isEmpty(password)) throw Exception("Password tidak boleh kosong")

                val passwordConfirm = binding.etPasswordConfirm.text.toString()
                if (TextUtils.isEmpty(passwordConfirm)) throw Exception("Password konfirmasi tidak boleh kosong")

                val namaLengkap = binding.etNamaLengkap.text.toString()
                if (TextUtils.isEmpty(namaLengkap)) throw Exception("Nama lengkap tidak boleh kosong")

                val noInduk = binding.etNoInduk.text.toString()
                if (TextUtils.isEmpty(noInduk)) throw Exception("No induk tidak boleh kosong")

                val kodeBergabung = binding.etKodeBergabung.text.toString()
                if (TextUtils.isEmpty(kodeBergabung)) throw Exception("Kode bergabung tidak boleh kosong")

                val idUnitKerja = selectedUnitKerja.idDoc
                if (idUnitKerja == null || TextUtils.isEmpty(idUnitKerja)) throw Exception("Unit kerja wajib dipilih")

                val user = User(username, password, passwordConfirm);
                if (user.password!=user.passwordConfirm) throw Exception("Kombinasi password tidak cocok")

                val staff = Staff(isAktif = true, nama = namaLengkap, noInduk = noInduk, timeCreate = Timestamp.now(), unitKerja = FirebaseFirestore.getInstance().collection(UnitKerja.COLLECTION_NAME).document(idUnitKerja))

                viewModel.registerStaff(user, staff)
            } catch (e: Exception) {
                Snackbar.make(
                    findViewById(R.id.activity_main),
                    "${e.localizedMessage}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            moveToLoginActivity()
        }

        viewModel.staff.observe(this){
            if (it!=null){
                Snackbar.make(
                    findViewById(R.id.activity_main),
                    "Registrasi akun berhasil, silahkan login",
                    Snackbar.LENGTH_SHORT
                ).show()
                moveToMenuActivity()
            }
        }
    }

    private fun moveToLoginActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun moveToMenuActivity() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }

    private fun loadUnitKerja(it: Editable?) {
        viewModel.getUnitKerja(it.toString());
    }
}