package org.informatika.if5250rajinapps.ui.home

import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.informatika.if5250rajinapps.model.Presence
import org.informatika.if5250rajinapps.model.Staff
import org.informatika.if5250rajinapps.model.UnitKerja
import org.informatika.if5250rajinapps.util.FirebaseService

private const val TAG = "HomeViewModel"
class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _staff = MutableLiveData<Staff?>(null)
    val staff: LiveData<Staff?> = _staff

    private val _unitKerja = MutableLiveData<UnitKerja>(null)
    val unitkerja: LiveData<UnitKerja?> = _unitKerja

    private val _presensi = MutableLiveData<Presence?>(null)
    val presensi: LiveData<Presence?> = _presensi

    init {
        viewModelScope.launch {
            FirebaseService.getUser().collect{ staff ->
                _staff.value = staff
                _unitKerja.value = FirebaseService.getUserUnitKerja(staff!!.unitKerja!!)
            }
        }

        fetchPresence()
    }

    fun fetchPresence(){
        viewModelScope.launch {
            _presensi.value = FirebaseService.getPresence()
        }
    }
}