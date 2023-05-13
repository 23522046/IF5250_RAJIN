package org.informatika.if5250rajinapps.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import org.informatika.if5250rajinapps.databinding.FragmentHomeBinding
import org.informatika.if5250rajinapps.util.formattedDateOnly
import org.informatika.if5250rajinapps.util.formattedTimeOnly
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var navController: NavController

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var viewModel : HomeViewModel? = null;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel!!.staff.observe(viewLifecycleOwner,  {
            it.let {
                binding.tvNama.text = it?.nama
                binding.tvNoInduk.text = it?.noInduk

            }
        })

        viewModel!!.unitkerja.observe(viewLifecycleOwner, {
            it.let {
                binding.tvOrganisasi.text = it?.nama
            }
        })

        viewModel!!.presensi.observe(viewLifecycleOwner,  {
//            binding.btIsiKehadiran.visibility = View.VISIBLE
            Log.d("HomeFragment", "onCreateView: presensi : ${it}")
            it.let {
                binding.tvJenis.text =  it?.jenis?.uppercase()
                binding.tvTimeCreate.text = it?.timeCreate?.formattedDateOnly
                if (it?.timeCreate==null){
                    val date = Date()
                    binding.tvTimeCreate.text = date.formattedDateOnly
                }

                binding.tvCheckIn.text = "Masuk : ${if (it?.checkIn!=null) it?.checkIn?.waktu?.formattedTimeOnly else '-'}"
                binding.tvCheckOut.text = "Pulang : -"
                binding.tvDurasi.text = "Durasi Kerja : -"
//
                it?.checkOut?.let {checkOut ->
                    binding.tvCheckOut.text = "Pulang : ${checkOut?.waktu?.formattedTimeOnly}"
                    val startDate: Date = it.checkIn?.waktu!!.toDate()
                    val endDate: Date = checkOut.waktu!!.toDate()

                    val durationInMillis: Long = endDate.time - startDate.time
                    val hours: Long = durationInMillis / (1000 * 60 * 60)
                    val minutes: Long = durationInMillis / (1000 * 60) % 60
                    val seconds: Long = durationInMillis / 1000 % 60

                    binding.tvDurasi.text = "Durasi Kerja : $hours Jam, $minutes Menit"
//                    binding.btIsiKehadiran.visibility = View.GONE
                }
            }
        })


//        val btIsiKehadiran: Button = binding.btIsiKehadiran
//        btIsiKehadiran.setOnClickListener {
//            val intent = Intent(activity, TakeAttendanceActivity::class.java)
//            startActivity(intent)
//        }
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
}