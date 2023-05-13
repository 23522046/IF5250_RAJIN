package org.informatika.if5250rajinapps.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.informatika.if5250rajinapps.R
import org.informatika.if5250rajinapps.databinding.ActivityRequestBinding

class RequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}