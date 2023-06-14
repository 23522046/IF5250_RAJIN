package org.informatika.if5250rajinapps.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import org.informatika.if5250rajinapps.R
import org.informatika.if5250rajinapps.databinding.ActivityMenuBinding
import org.informatika.if5250rajinapps.ui.home.HomeViewModel

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private var isAllFabsVisible: Boolean? = null
    private var alreadyCheckout: Boolean = false

    var homeViewModel : HomeViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_menu)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_presence, R.id.navigation_submission
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // observe presence here

        // invisible
        isAllFabsVisible = false

        // Now set all the FABs and all the action name
        // texts as GONE
        binding.fabAddRequest.visibility = View.GONE
        binding.fabTakeAttendance.visibility = View.GONE



        // Set the Extended floating action button to
        // shrinked state initially
        binding.fabClose.shrink()
        binding.fabClose.setOnClickListener {
            updateAllFabVisible()
        }

        binding.fabTakeAttendance.setOnClickListener {
            updateAllFabVisible()
            val intent = Intent(this, TakeAttendanceActivity::class.java)
            startActivity(intent)
        }

        binding.fabAddRequest.setOnClickListener {
            updateAllFabVisible()
            val intent = Intent(this, RequestActivity::class.java)
            startActivity(intent)
        }

        homeViewModel!!.presensi.observe(this) {
            Log.d("MenuActivity", "onCreateView: presensi : ${it}")
            if (it?.checkOut != null){
                alreadyCheckout = true
            }
        }
    }

    private fun updateAllFabVisible() {
        isAllFabsVisible = if (!isAllFabsVisible!!) {

            binding.fabAddRequest.show()


            if (!alreadyCheckout) {
                binding.fabTakeAttendance.show()
            }

            binding.fabClose.extend()
            true
        } else {

            binding.fabTakeAttendance.hide()
            binding.fabAddRequest.hide()
            binding.fabClose.shrink()
            false
        }
    }

    override fun onResume() {
        homeViewModel.let {
            // refresh presence here
            it?.fetchPresence()
        }
        super.onResume()
    }
}