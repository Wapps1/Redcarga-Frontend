package com.wapps1.redcarga.features.requests.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wapps1.redcarga.features.fleet.domain.models.geo.Department
import com.wapps1.redcarga.features.fleet.domain.models.geo.GeoCatalog
import com.wapps1.redcarga.features.fleet.domain.models.geo.Province
import com.wapps1.redcarga.features.fleet.domain.repositories.GeoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateRequestViewModel @Inject constructor(
    private val geoRepository: GeoRepository,
    private val requestsRepository: com.wapps1.redcarga.features.requests.domain.repositories.RequestsRepository
) : ViewModel() {

    // Estados de la solicitud
    sealed class SubmitState {
        object Idle : SubmitState()
        object Loading : SubmitState()
        data class Success(val requestId: Long) : SubmitState()
        data class Error(val message: String) : SubmitState()
    }

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    // Catálogo completo de geo
    val geoCatalog: StateFlow<GeoCatalog?> = geoRepository.observeCatalog()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Estado del formulario
    private val _requestName = MutableStateFlow("")
    val requestName: StateFlow<String> = _requestName.asStateFlow()

    // Origen
    private val _selectedOriginDepartment = MutableStateFlow<Department?>(null)
    val selectedOriginDepartment: StateFlow<Department?> = _selectedOriginDepartment.asStateFlow()

    private val _selectedOriginProvince = MutableStateFlow<Province?>(null)
    val selectedOriginProvince: StateFlow<Province?> = _selectedOriginProvince.asStateFlow()

    private val _originDistrict = MutableStateFlow("")
    val originDistrict: StateFlow<String> = _originDistrict.asStateFlow()

    // Destino
    private val _selectedDestinationDepartment = MutableStateFlow<Department?>(null)
    val selectedDestinationDepartment: StateFlow<Department?> = _selectedDestinationDepartment.asStateFlow()

    private val _selectedDestinationProvince = MutableStateFlow<Province?>(null)
    val selectedDestinationProvince: StateFlow<Province?> = _selectedDestinationProvince.asStateFlow()

    private val _destinationDistrict = MutableStateFlow("")
    val destinationDistrict: StateFlow<String> = _destinationDistrict.asStateFlow()

    // Payment on delivery
    private val _paymentOnDelivery = MutableStateFlow(false)
    val paymentOnDelivery: StateFlow<Boolean> = _paymentOnDelivery.asStateFlow()

    // Items
    data class ItemFormData(
        val id: String = java.util.UUID.randomUUID().toString(),
        val itemName: String = "",
        val heightCm: String = "",
        val widthCm: String = "",
        val lengthCm: String = "",
        val weightKg: String = "",
        val quantity: String = "1",
        val fragile: Boolean = false,
        val notes: String = "",
        val imageUris: List<String> = emptyList()
    )

    private val _items = MutableStateFlow<List<ItemFormData>>(emptyList())
    val items: StateFlow<List<ItemFormData>> = _items.asStateFlow()

    init {
        // Refrescar catálogo al iniciar
        viewModelScope.launch {
            runCatching {
                geoRepository.refreshCatalog()
            }
        }
    }

    // Funciones para actualizar el estado
    fun updateRequestName(name: String) {
        _requestName.value = name
    }

    fun selectOriginDepartment(department: Department?) {
        _selectedOriginDepartment.value = department
        // Reset provincia y distrito cuando cambia el departamento
        _selectedOriginProvince.value = null
        _originDistrict.value = ""
    }

    fun selectOriginProvince(province: Province?) {
        _selectedOriginProvince.value = province
        // Reset distrito cuando cambia la provincia
        _originDistrict.value = ""
    }

    fun updateOriginDistrict(district: String) {
        _originDistrict.value = district
    }

    fun selectDestinationDepartment(department: Department?) {
        _selectedDestinationDepartment.value = department
        // Reset provincia y distrito cuando cambia el departamento
        _selectedDestinationProvince.value = null
        _destinationDistrict.value = ""
    }

    fun selectDestinationProvince(province: Province?) {
        _selectedDestinationProvince.value = province
        // Reset distrito cuando cambia la provincia
        _destinationDistrict.value = ""
    }

    fun updateDestinationDistrict(district: String) {
        _destinationDistrict.value = district
    }

    fun togglePaymentOnDelivery() {
        _paymentOnDelivery.value = !_paymentOnDelivery.value
    }

    fun setPaymentOnDelivery(value: Boolean) {
        _paymentOnDelivery.value = value
    }

    // Funciones para manejar items
    fun addItem(item: ItemFormData) {
        _items.value = _items.value + item
    }

    fun updateItem(item: ItemFormData) {
        _items.value = _items.value.map { if (it.id == item.id) item else it }
    }

    fun removeItem(itemId: String) {
        _items.value = _items.value.filter { it.id != itemId }
    }

    fun getTotalWeight(): Double {
        return _items.value.sumOf { item ->
            val weight = item.weightKg.toDoubleOrNull() ?: 0.0
            val quantity = item.quantity.toIntOrNull() ?: 1
            weight * quantity
        }
    }

    fun getTotalItems(): Int {
        return _items.value.sumOf { it.quantity.toIntOrNull() ?: 1 }
    }

    // Función helper para obtener provincias filtradas por departamento
    fun getProvincesForDepartment(departmentCode: String?): List<Province> {
        if (departmentCode == null) return emptyList()
        return geoCatalog.value?.provinces?.filter { it.departmentCode == departmentCode } ?: emptyList()
    }

    // Validación: Verifica si todos los campos obligatorios están completos
    fun isFormValid(): Boolean {
        return requestName.value.isNotBlank() &&
                selectedOriginDepartment.value != null &&
                selectedOriginProvince.value != null &&
                selectedDestinationDepartment.value != null &&
                selectedDestinationProvince.value != null &&
                _items.value.isNotEmpty()
    }

    // Función para obtener los errores de validación
    fun getValidationErrors(): List<String> {
        val errors = mutableListOf<String>()

        if (requestName.value.isBlank()) {
            errors.add("El nombre de la solicitud es obligatorio")
        }
        if (selectedOriginDepartment.value == null) {
            errors.add("Debe seleccionar el departamento de origen")
        }
        if (selectedOriginProvince.value == null) {
            errors.add("Debe seleccionar la provincia de origen")
        }
        if (selectedDestinationDepartment.value == null) {
            errors.add("Debe seleccionar el departamento de destino")
        }
        if (selectedDestinationProvince.value == null) {
            errors.add("Debe seleccionar la provincia de destino")
        }
        if (_items.value.isEmpty()) {
            errors.add("Debe agregar al menos un artículo")
        }

        return errors
    }

    // Construir el objeto UbigeoSnapshot para origin/destination
    private fun buildUbigeoSnapshot(
        department: Department?,
        province: Province?,
        districtText: String
    ): com.wapps1.redcarga.features.requests.domain.models.UbigeoSnapshot? {
        if (department == null || province == null) return null

        return com.wapps1.redcarga.features.requests.domain.models.UbigeoSnapshot(
            departmentCode = department.code,
            departmentName = department.name,
            provinceCode = province.code,
            provinceName = province.name,
            districtText = districtText
        )
    }

    // Obtener el snapshot de origen
    fun getOriginSnapshot(): com.wapps1.redcarga.features.requests.domain.models.UbigeoSnapshot? {
        return buildUbigeoSnapshot(
            selectedOriginDepartment.value,
            selectedOriginProvince.value,
            originDistrict.value
        )
    }

    // Obtener el snapshot de destino
    fun getDestinationSnapshot(): com.wapps1.redcarga.features.requests.domain.models.UbigeoSnapshot? {
        return buildUbigeoSnapshot(
            selectedDestinationDepartment.value,
            selectedDestinationProvince.value,
            destinationDistrict.value
        )
    }

    // Construir el CreateRequestRequest completo
    fun buildCreateRequest(): com.wapps1.redcarga.features.requests.domain.models.CreateRequestRequest? {
        if (!isFormValid()) return null

        val origin = getOriginSnapshot() ?: return null
        val destination = getDestinationSnapshot() ?: return null

        val requestItems = _items.value.map { item ->
            com.wapps1.redcarga.features.requests.domain.models.CreateRequestItem(
                itemName = item.itemName,
                heightCm = java.math.BigDecimal(item.heightCm.toDoubleOrNull() ?: 0.0),
                widthCm = java.math.BigDecimal(item.widthCm.toDoubleOrNull() ?: 0.0),
                lengthCm = java.math.BigDecimal(item.lengthCm.toDoubleOrNull() ?: 0.0),
                weightKg = java.math.BigDecimal(item.weightKg.toDoubleOrNull() ?: 0.0),
                totalWeightKg = java.math.BigDecimal((item.weightKg.toDoubleOrNull() ?: 0.0) * (item.quantity.toIntOrNull() ?: 1)),
                quantity = item.quantity.toIntOrNull() ?: 1,
                fragile = item.fragile,
                notes = item.notes,
                images = item.imageUris.mapIndexed { index, uri ->
                    com.wapps1.redcarga.features.requests.domain.models.CreateRequestImage(
                        imageUrl = uri,
                        imagePosition = index + 1
                    )
                }
            )
        }

        return com.wapps1.redcarga.features.requests.domain.models.CreateRequestRequest(
            origin = origin,
            destination = destination,
            paymentOnDelivery = _paymentOnDelivery.value,
            requestName = requestName.value,
            items = requestItems
        )
    }

    /* 
     * Estructura del JSON que se construirá:
     * {
     *   "origin": {
     *     "departmentCode": selectedOriginDepartment.code,
     *     "departmentName": selectedOriginDepartment.name,
     *     "provinceCode": selectedOriginProvince.code,
     *     "provinceName": selectedOriginProvince.name,
     *     "districtText": originDistrict
     *   },
     *   "destination": {
     *     "departmentCode": selectedDestinationDepartment.code,
     *     "departmentName": selectedDestinationDepartment.name,
     *     "provinceCode": selectedDestinationProvince.code,
     *     "provinceName": selectedDestinationProvince.name,
     *     "districtText": destinationDistrict
     *   },
     *   "paymentOnDelivery": true/false,
     *   "request_name": requestName,
     *   "items": [...]
     * }
     */

    // Enviar la solicitud al servidor
    fun submitRequest() {
        viewModelScope.launch {
            try {
                _submitState.value = SubmitState.Loading

                val request = buildCreateRequest()
                if (request == null) {
                    _submitState.value = SubmitState.Error("Por favor complete todos los campos obligatorios")
                    return@launch
                }

                val response = requestsRepository.createRequest(request)
                _submitState.value = SubmitState.Success(response.requestId)

                // Limpiar el formulario después del éxito
                clearForm()
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is com.wapps1.redcarga.features.requests.domain.RequestsDomainError.NetworkError ->
                        "No se pudo conectar con el servidor. Verifica tu conexión a internet."
                    is com.wapps1.redcarga.features.requests.domain.RequestsDomainError.InvalidRequestData ->
                        "Los datos ingresados no son válidos. Por favor revisa la información."
                    is com.wapps1.redcarga.features.requests.domain.RequestsDomainError.Unauthorized ->
                        "No tienes autorización para crear solicitudes. Por favor inicia sesión nuevamente."
                    is com.wapps1.redcarga.features.requests.domain.RequestsDomainError.ServerError ->
                        "Error en el servidor. Por favor intenta más tarde."
                    else ->
                        "Error inesperado: ${e.message ?: "Intenta nuevamente"}"
                }
                _submitState.value = SubmitState.Error(errorMessage)
            }
        }
    }

    // Resetear el estado de envío
    fun resetSubmitState() {
        _submitState.value = SubmitState.Idle
    }

    // Limpiar el formulario después de un envío exitoso
    private fun clearForm() {
        _requestName.value = ""
        _selectedOriginDepartment.value = null
        _selectedOriginProvince.value = null
        _originDistrict.value = ""
        _selectedDestinationDepartment.value = null
        _selectedDestinationProvince.value = null
        _destinationDistrict.value = ""
        _paymentOnDelivery.value = false
        _items.value = emptyList()
    }
}

