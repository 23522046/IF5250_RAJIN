package org.informatika.if5250rajinapps.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import org.informatika.if5250rajinapps.R
import org.informatika.if5250rajinapps.activity.MainActivity
import org.informatika.if5250rajinapps.databinding.FragmentHomeBinding
import org.informatika.if5250rajinapps.util.formattedDateOnly
import org.informatika.if5250rajinapps.util.formattedTimeOnly
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var navController: NavController
    private lateinit var mAuth: FirebaseAuth
    private val binding get() = _binding!!

    var viewModel : HomeViewModel? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        mAuth = FirebaseAuth.getInstance()

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.tvTimeCreate.text = Date().formattedDateOnly
        binding.tvCheckIn.text = "Masuk : -"
        binding.tvCheckOut.text = "Pulang : -"
        binding.tvDurasi.text = "Durasi Kerja : -"

        viewModel!!.staff.observe(viewLifecycleOwner) {
            it.let {
                binding.tvNama.text = it?.nama
                binding.tvNoInduk.text = it?.noInduk

            }
        }

        viewModel!!.unitkerja.observe(viewLifecycleOwner) {
            it.let {
                binding.tvOrganisasi.text = it?.nama
            }
        }

        viewModel!!.presensi.observe(viewLifecycleOwner) {
            Log.d("HomeFragment", "onCreateView: presensi : ${it}")
            if (it!=null){
                binding.tvJenis.text = it?.jenis?.uppercase()
                binding.tvTimeCreate.text = it?.timeCreateTime()
                binding.tvCheckIn.text = "Masuk : ${it?.checkInTime()}"
                binding.tvCheckOut.text = "Pulang : ${it?.checkOutTime()}"
                binding.tvDurasi.text = "Durasi Kerja : ${it?.durasiKerja()}"
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.navController = Navigation.findNavController(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        viewModel.let {
            it?.fetchPresence()
        }
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_action_bar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_logout -> {
                actionSignOut()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun actionSignOut() {
        mAuth.signOut()
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(intent)
        activity?.finish()
    }
}