package org.informatika.if5250rajinapps.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.informatika.if5250rajinapps.model.Presence
import org.informatika.if5250rajinapps.model.Staff
import org.informatika.if5250rajinapps.model.UnitKerja
import org.informatika.if5250rajinapps.util.FirebaseService

class TakeAttendanceViewModel : ViewModel() {

    private val _presensi = MutableLiveData<Presence?>(null)
    val presensi: LiveData<Presence?> = _presensi

    private val _divisi = MutableLiveData<UnitKerja?>(null)
    val divisi: LiveData<UnitKerja?> = _divisi

    private val _staff = MutableLiveData<Staff?>(null)
    val staff: LiveData<Staff?> = _staff

    private val _unitKerja = MutableLiveData<UnitKerja?>(null)
    val unitKerja: LiveData<UnitKerja?> = _unitKerja

    init {
        viewModelScope.launch {
            _presensi.value = FirebaseService.getPresence()
        }

        viewModelScope.launch {
            FirebaseService.getUser().collect{ staff ->

                _staff.value = staff

                _divisi.value = FirebaseService.getUserUnitKerja(staff!!.unitKerja!!)

                if (divisi.value!=null && divisi.value?.parent!=null){
                    _unitKerja.value = FirebaseService.getUserUnitKerja(
                        divisi.value!!.parent!!)
                }
            }
        }
    }

    fun presensiMasuk(presence: Presence){
        FirebaseService.addPresence(presence).addOnFailureListener {
            Log.e("TakeAttendanceViewModel", "Error presensiMasuk",it)
        }
    }

    fun presensiPulang(presence: Presence){
        FirebaseService.addPresence(presence).addOnFailureListener {
            Log.e("TakeAttendanceViewModel", "Error presensiPulang",it)
        }
    }
}