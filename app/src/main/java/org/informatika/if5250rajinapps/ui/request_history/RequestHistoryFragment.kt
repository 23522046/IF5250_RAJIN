package org.informatika.if5250rajinapps.ui.request_history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.informatika.if5250rajinapps.adapter.RequestAdapter
import org.informatika.if5250rajinapps.databinding.FragmentHistoryRequestBinding
import org.informatika.if5250rajinapps.model.Pengajuan
import java.util.Date

class RequestHistoryFragment : Fragment(), RequestAdapter.OnRequestSelectedListener {

    lateinit var firestore: FirebaseFirestore
    private var query: Query? = null
    private var adapter: RequestAdapter? = null
    private var _binding: FragmentHistoryRequestBinding? = null

    private lateinit var viewModel: RequestHistoryViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(RequestHistoryViewModel::class.java)

        _binding = FragmentHistoryRequestBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[RequestHistoryViewModel::class.java]

        renderData()
    }

    private fun renderData() {
        firestore = Firebase.firestore
        val mAuth = Firebase.auth

        val date = Date()
        query = firestore.collection(Pengajuan.COLLECTION_NAME)
            .whereEqualTo(Pengajuan.FIELD_UID, mAuth.uid)

        query!!.get().addOnCompleteListener { task ->
            if (task.isSuccessful){
                val result = task.result
                result?.let {
                    result.documents.mapNotNull { snapshot ->
                        Log.d("RequestHistoryFragment", "snapshot = ${snapshot["uid"]}")
                        val p = snapshot.toObject(Pengajuan::class.java)
                        Log.d("RequestHistoryFragment", "Pengajuan : $p")
                    }

                    if (result.documents.isEmpty()) showRecyclerView() else hideRecyclerView()

                }
            } else {
                task.exception?.let { Log.e("RequestHistoryFragment", it.stackTraceToString()) }
            }
        }

        query?.let {
            adapter = RequestAdapter(it, this@RequestHistoryFragment)
            binding.recyclerView.adapter = adapter
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
    }

    override fun onStart() {
        super.onStart()
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

    override fun onRequestSelected(pengajuan: Pengajuan) {
        Snackbar.make(binding.root, "Request selected", Snackbar.LENGTH_LONG).show()
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


}