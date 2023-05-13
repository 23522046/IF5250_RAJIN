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
        binding.addAlarmFab.setVisibility(View.GONE)
        binding.addPersonFab.setVisibility(View.GONE)



        // Set the Extended floating action button to
        // shrinked state initially
        binding.addFab.shrink()
        binding.addFab.setOnClickListener {
            updateAllFabVisible()
        }

        binding.addPersonFab.setOnClickListener {
            updateAllFabVisible()
            val intent = Intent(this, TakeAttendanceActivity::class.java)
            startActivity(intent)
        }

        binding.addAlarmFab.setOnClickListener {
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

            // when isAllFabsVisible becomes
            // true make all the action name
            // texts and FABs VISIBLE.
            binding.addAlarmFab.show()


            if (!alreadyCheckout) {
                binding.addPersonFab.show()
            }

            // Now extend the parent FAB, as
            // user clicks on the shrinked
            // parent FAB
            binding.addFab.extend()

            // make the boolean variable true as
            // we have set the sub FABs
            // visibility to GONE
            true
        } else {

            // when isAllFabsVisible becomes
            // true make all the action name
            // texts and FABs GONE.
            binding.addAlarmFab.hide()
            binding.addPersonFab.hide()



            // Set the FAB to shrink after user
            // closes all the sub FABs
            binding.addFab.shrink()

            // make the boolean variable false
            // as we have set the sub FABs
            // visibility to GONE
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