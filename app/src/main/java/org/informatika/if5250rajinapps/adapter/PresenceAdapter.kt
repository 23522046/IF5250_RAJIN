package org.informatika.if5250rajinapps.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import org.informatika.if5250rajinapps.databinding.PresenceListItemBinding
import org.informatika.if5250rajinapps.model.Presence
import org.informatika.if5250rajinapps.util.formattedDateOnly
import org.informatika.if5250rajinapps.util.formattedTimeOnly
import java.util.*

open class PresenceAdapter(query: Query, private val listener: OnPresenceSelectedListener) : FirestoreAdapter<PresenceAdapter.ViewHolder>(query) {

    interface OnPresenceSelectedListener {
        fun onPresenceSelected(presence: Presence)
    }

    class ViewHolder(val binding: PresenceListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnPresenceSelectedListener?,
        ){
            val presence = snapshot.toObject<Presence>()

            binding.tvJenis.text = presence?.jenis.toString().uppercase()
            binding.tvTimeCreate.text = presence?.checkIn?.waktu?.formattedDateOnly
            binding.tvCheckIn.text = "Masuk : ${presence?.checkIn?.waktu?.formattedTimeOnly}"
            binding.tvCheckOut.text = "Pulang : ${if (presence?.checkOut==null) "-" else presence?.checkOut?.waktu?.formattedTimeOnly}"
            binding.tvDurasi.text = "Durasi Kerja : -"

            presence?.checkOut?.let {
                val startDate: Date = presence?.checkIn?.waktu!!.toDate()
                val endDate: Date = it?.waktu!!.toDate()

                val durationInMillis: Long = endDate.time - startDate.time
                val hours: Long = durationInMillis / (1000 * 60 * 60)
                val minutes: Long = durationInMillis / (1000 * 60) % 60
                val seconds: Long = durationInMillis / 1000 % 60

                binding.tvDurasi.text = "Durasi Kerja : $hours Jam, $minutes Menit, $seconds Detik"
            }
            // TODO: lanjutkan binding
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("PresenceAdapter", "onCreateViewHolder invoked")
        return ViewHolder(
            PresenceListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("PresenceAdapter", "onBindViewHolder item ke $position")
        holder.bind(getSnapshot(position), listener)
    }
}