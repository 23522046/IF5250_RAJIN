package org.informatika.if5250rajinapps.ui.presence_history

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.informatika.if5250rajinapps.R
import org.informatika.if5250rajinapps.adapter.PresenceAdapter
import org.informatika.if5250rajinapps.databinding.FragmentHistoryPresenceBinding
import org.informatika.if5250rajinapps.model.Pengajuan
import org.informatika.if5250rajinapps.model.Presence
import org.informatika.if5250rajinapps.util.formattedYMD
import java.text.SimpleDateFormat
import java.util.*

class PresenceHistoryFragment : Fragment(), PresenceAdapter.OnPresenceSelectedListener {

    lateinit var firestore: FirebaseFirestore
    private var query: Query? = null
    private var adapter: PresenceAdapter? = null
    private var _binding: FragmentHistoryPresenceBinding? = null
    private lateinit var viewModel:PresenceHistoryViewModel
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_bar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.action_search -> {
                // navigate to setting screen
                menuSearchSelected()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun menuSearchSelected() {
        val datePicker = MaterialDatePicker.Builder.dateRangePicker().build()
        datePicker.show(requireFragmentManager(), "DatePicker")

        // Setting up the event for when ok is clicked
        datePicker.addOnPositiveButtonClickListener {
            val cStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cStart.time = Date(it.first)

            val cEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cEnd.time = Date(it.second)
            cEnd.add(Calendar.DATE, 1)


            Log.d("PresenceFragment", "start : ${cStart.get(Calendar.YEAR)}-${cStart.get(Calendar.MONTH)+1}-${cStart.get(
                Calendar.DATE)}")
            Log.d("PresenceFragment", "end : ${cEnd.get(Calendar.YEAR)}-${cEnd.get(Calendar.MONTH)+1}-${cEnd.get(
                Calendar.DATE)}")

            val start = SimpleDateFormat("yyyy-MM-dd").parse("${cStart.get(Calendar.YEAR)}-${cStart.get(
                Calendar.MONTH)+1}-${cStart.get(Calendar.DATE)}")
            val end = SimpleDateFormat("yyyy-MM-dd").parse("${cEnd.get(Calendar.YEAR)}-${cEnd.get(
                Calendar.MONTH)+1}-${cEnd.get(Calendar.DATE)}")
            renderData(start, end)

//            Toast.makeText(context, "${datePicker.headerText} is selected", Toast.LENGTH_LONG).show()
        }

        // Setting up the event for when cancelled is clicked
        datePicker.addOnNegativeButtonClickListener {
            Toast.makeText(context, "${datePicker.headerText} is cancelled", Toast.LENGTH_LONG).show()
        }

        // Setting up the event for when back button is pressed
        datePicker.addOnCancelListener {
            Toast.makeText(context, "Date Picker Cancelled", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentHistoryPresenceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View model
        viewModel =
            ViewModelProvider(this).get(PresenceHistoryViewModel::class.java)

        renderData(null, null)
    }


    private fun renderData(start: Date?, end: Date?) {
        // Firestore
        firestore = Firebase.firestore
        val mAuth = Firebase.auth

        if (start==null || end ==null){
            val date = Date()
            Log.d("PresenceHistoryFragment", "date : ${date.formattedYMD}")
                // Get the 10 newest presence
                query = firestore.collection(Presence.COLLECTION_NAME)
                    .orderBy(Presence.FIELD_TIME_CREATE, Query.Direction.DESCENDING)
                    .whereEqualTo(Presence.FIELD_UID, mAuth.uid)
                    .whereGreaterThanOrEqualTo(Presence.FIELD_TIME_CREATE, SimpleDateFormat("yyyy-MM-dd").parse(date.formattedYMD))

            } else {
            // Get the 10 newest presence
            query = firestore.collection(Presence.COLLECTION_NAME)
                .orderBy(Presence.FIELD_TIME_CREATE, Query.Direction.DESCENDING)
                .whereEqualTo(Presence.FIELD_UID, mAuth.uid)
                .whereGreaterThanOrEqualTo(Presence.FIELD_TIME_CREATE, start)
                .whereLessThanOrEqualTo(Presence.FIELD_TIME_CREATE, end)
        }

        query!!.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                result?.let {
                    result.documents.mapNotNull { snapshot ->
                        Log.d("PresenceFragment", "snapshot = ${snapshot["staff_id"]}")
                        val p = snapshot.toObject(Presence::class.java)
                        Log.d("PresenceFragment", p.toString())
                    }

                    if (result.documents.isEmpty()) showRecyclerView() else hideRecyclerView()
                }
            } else {
                task.exception?.let { Log.e("PresenceFragment", it.stackTraceToString()) }
            }
        }


        // Recyclerview
        query?.let {
            Log.d("PresenceFragment", "here")

            adapter = PresenceAdapter(it, this@PresenceHistoryFragment)

            Log.d("PresenceFragment", "here 97")
            binding.recyclerView.adapter = adapter
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter?.startListening()
    }

    private fun hideRecyclerView() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.tvTidakAdaData.visibility = View.GONE
        binding.ivNoData.visibility = View.GONE
    }

    private fun showRecyclerView() {
        binding.recyclerView.visibility = View.GONE
        binding.tvTidakAdaData.visibility = View.VISIBLE
        binding.ivNoData.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        // Start listening for Firestore updates
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onPresenceSelected(presence: Presence) {
        Snackbar.make(binding.root, "Presence selected", Snackbar.LENGTH_LONG).show()
    }

}