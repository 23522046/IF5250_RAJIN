package org.informatika.if5250rajinapps.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.informatika.if5250rajinapps.model.Staff
import org.informatika.if5250rajinapps.model.UnitKerja
import org.informatika.if5250rajinapps.model.User
import org.informatika.if5250rajinapps.util.FirebaseService

class SignUpViewModel : ViewModel() {
    private val _unitKerja = MutableLiveData<UnitKerja?>(null)
    val unitKerja: LiveData<UnitKerja?> = _unitKerja

    private val _unitKerjaChild = MutableLiveData<List<UnitKerja?>>(null)
    val unitKerjaChild: LiveData<List<UnitKerja?>> = _unitKerjaChild

    private val _staff = MutableLiveData<Staff?>(null)
    val staff: LiveData<Staff?> = _staff

    fun getUnitKerja(idDoc: String){
        viewModelScope.launch {
            _unitKerja.value = FirebaseService.getUnitKerja(idDoc)
            _unitKerjaChild.value = FirebaseService.getListUnitKerja(idDoc)
        }
    }

    fun registerStaff(u: User, s: Staff){
        viewModelScope.launch {
            _staff.value = FirebaseService.registerStaff(u, s)
        }
    }
}