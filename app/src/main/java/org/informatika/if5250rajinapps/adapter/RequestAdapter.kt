package org.informatika.if5250rajinapps.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import org.informatika.if5250rajinapps.databinding.RequestListItemBinding
import org.informatika.if5250rajinapps.model.Pengajuan
import org.informatika.if5250rajinapps.util.formattedDateOnly
import org.informatika.if5250rajinapps.util.formattedYMD

class RequestAdapter(query: Query, private val listener: OnRequestSelectedListener) : FirestoreAdapter<RequestAdapter.ViewHolder>(query) {

    interface OnRequestSelectedListener {
        fun onRequestSelected(pengajuan: Pengajuan)
    }

    class ViewHolder(val binding: RequestListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnRequestSelectedListener?,
        ){
            val pengajuan = snapshot.toObject<Pengajuan>()

            binding.tvStatus.text = pengajuan?.status.toString().uppercase()
            binding.tvJenis.text = pengajuan?.jenis.toString().uppercase()
            binding.tvDiajukan.text = "Diajukan : ${pengajuan?.timeCreate?.toDate().formattedDateOnly}"
            binding.tvMulai.text = "Mulai : ${pengajuan?.mulaiTgl?.toDate().formattedYMD}"
            binding.tvSelesai.text = "Selesai : ${pengajuan?.sampaiTgl?.toDate().formattedYMD}"
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("RequestAdapter", "onCreateViewHolder invoked")
        return ViewHolder(
            RequestListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("RequestAdapter", "onBindViewHolder item ke $position")
        holder.bind(getSnapshot(position), listener)
    }
}