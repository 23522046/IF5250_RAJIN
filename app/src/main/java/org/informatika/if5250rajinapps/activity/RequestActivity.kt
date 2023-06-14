package org.informatika.if5250rajinapps.activity

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import org.informatika.if5250rajinapps.R
import org.informatika.if5250rajinapps.databinding.ActivityRequestBinding
import org.informatika.if5250rajinapps.model.Pengajuan
import org.informatika.if5250rajinapps.util.toDateOnly
import org.informatika.if5250rajinapps.viewmodel.RequestViewModel
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class RequestActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRequestBinding
    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var ivDoc: ImageView
    private lateinit var etFirstDate: TextInputEditText
    private lateinit var etLastDate: TextInputEditText
    private var selectedImageUri: Uri? = null
    lateinit var viewModel: RequestViewModel

    var launchSomeActivity = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode
            == RESULT_OK
        ) {
            val data = result.data
            onPickedImage(data)
        }
    }

    private fun onPickedImage(data: Intent?) {
        if (data != null
            && data.data != null
        ) {
            selectedImageUri = data.data
            val selectedImageBitmap: Bitmap?
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                    this.contentResolver,
                    selectedImageUri
                )

                ivDoc.setImageBitmap(
                    selectedImageBitmap
                )


            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[RequestViewModel::class.java]

        binding = ActivityRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ivDoc = binding.ivDoc

        etFirstDate = binding.etFirstDate
        etFirstDate.setOnClickListener {
            showDatePicker(etFirstDate)
        }

        etLastDate = binding.etLastDate
        etLastDate.setOnClickListener {
            showDatePicker(etLastDate)
        }

        val btChooseImage = binding.btChooseImage
        btChooseImage.setOnClickListener { chooseImage() }

        val btSend = binding.btSend
        btSend.setOnClickListener {
            try {
                val selectedRbId = binding.rg.checkedRadioButtonId
                val selectedRb = findViewById<RadioButton>(selectedRbId)
                val mAuth = Firebase.auth

                if (mAuth.uid==null){
                    throw Exception("Anda belum login")
                }
                if (etFirstDate.text.isNullOrEmpty()){
                    throw Exception("Tanggal awal belum dipilih")
                }
                if (etLastDate.text.isNullOrEmpty()){
                    throw Exception("Tanggal akhir belum dipilih")
                }
                if (selectedImageUri==null){
                    throw Exception("Anda belum mengambil foto surat")
                }
                Log.i("RequestActivity", "{tgl_awal:${etFirstDate.text}, tgl_akhir:${etLastDate.text}, status:${selectedRb.text}, image_uri:${selectedImageUri.toString()}}")

                val p = Pengajuan(
                    jenis = selectedRb.text.toString().lowercase(),
                    mulaiTgl = Timestamp(etFirstDate.text.toString().toDateOnly),
                    sampaiTgl = Timestamp(etLastDate.text.toString().toDateOnly),
                    status = Pengajuan.STATUS_TERKIRIM,
                    timeCreate = Timestamp.now(),
                    uid = mAuth.uid
                )
                viewModel.simpanPengajuan(selectedImageUri!!, p)

                onBackPressedDispatcher.onBackPressed()
                Toast.makeText(applicationContext, "Berhasil mengajukan permohonan", Toast.LENGTH_SHORT).show()
            } catch (e: Exception){
                Snackbar.make(findViewById(R.id.activity_request), e.message.toString(), Snackbar.LENGTH_LONG).show()
            }
        }

        val btBatal = binding.btCancel
        btBatal.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

    }

    private fun chooseImage() {
        // Defining implicit intent to mobile gallery
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT

        launchSomeActivity.launch(i)
    }

    private fun showDatePicker(etDate: TextInputEditText) {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(calendar.time)
                etDate.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}