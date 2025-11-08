package com.refugio.pawrescue.ui.theme.rescate

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.refugio.pawrescue.data.model.Animal
import com.refugio.pawrescue.data.model.Rescate
import com.refugio.pawrescue.data.model.UbicacionRescate
import com.refugio.pawrescue.data.model.repository.AnimalRepository
import com.refugio.pawrescue.data.model.repository.RescateRepository
import com.refugio.pawrescue.ui.theme.utils.Constants
import kotlinx.coroutines.launch
import java.util.*

sealed class RescateState {
    object Idle : RescateState()
    object Loading : RescateState()
    data class Success(val animalId: String) : RescateState()
    data class Error(val message: String) : RescateState()
}

class NuevoRescateViewModel : ViewModel() {

    private val animalRepository = AnimalRepository()
    private val rescateRepository = RescateRepository()

    // Step 1: Foto
    private val _fotoPrincipal = MutableLiveData<Uri?>()
    val fotoPrincipal: LiveData<Uri?> = _fotoPrincipal

    private val _fotosAdicionales = MutableLiveData<List<Uri>>(emptyList())
    val fotosAdicionales: LiveData<List<Uri>> = _fotosAdicionales

    // Step 2: Ubicación
    private val _ubicacion = MutableLiveData<UbicacionRescate?>()
    val ubicacion: LiveData<UbicacionRescate?> = _ubicacion

    // Step 3: Datos
    private val _tipoAnimal = MutableLiveData<String>()
    val tipoAnimal: LiveData<String> = _tipoAnimal

    private val _raza = MutableLiveData<String>()
    val raza: LiveData<String> = _raza

    private val _edad = MutableLiveData<String>(Constants.EDAD_CACHORRO)
    val edad: LiveData<String> = _edad

    private val _sexo = MutableLiveData<String>("macho")
    val sexo: LiveData<String> = _sexo

    private val _estadoSalud = MutableLiveData<String>(Constants.SALUD_BUENO)
    val estadoSalud: LiveData<String> = _estadoSalud

    private val _condicionesEspeciales = MutableLiveData<List<String>>(emptyList())
    val condicionesEspeciales: LiveData<List<String>> = _condicionesEspeciales

    // Step 4: Notas
    private val _videoUri = MutableLiveData<Uri?>()
    val videoUri: LiveData<Uri?> = _videoUri

    private val _notas = MutableLiveData<String>()
    val notas: LiveData<String> = _notas

    private val _prioridad = MutableLiveData<String>(Constants.PRIORIDAD_MEDIA)
    val prioridad: LiveData<String> = _prioridad

    private val _necesitaVet = MutableLiveData<Boolean>(false)
    val necesitaVet: LiveData<Boolean> = _necesitaVet

    // State
    private val _saveState = MutableLiveData<RescateState>(RescateState.Idle)
    val saveState: LiveData<RescateState> = _saveState

    // Setters
    fun setFotoPrincipal(uri: Uri) {
        _fotoPrincipal.value = uri
    }

    fun addFotoAdicional(uri: Uri) {
        val current = _fotosAdicionales.value ?: emptyList()
        _fotosAdicionales.value = current + uri
    }

    fun setUbicacion(lat: Double, lng: Double, direccion: String, ciudad: String, estado: String) {
        _ubicacion.value = UbicacionRescate(lat, lng, direccion, ciudad, estado)
    }

    fun setTipoAnimal(tipo: String) {
        _tipoAnimal.value = tipo
    }

    fun setRaza(raza: String) {
        _raza.value = raza
    }

    fun setEdad(edad: String) {
        _edad.value = edad
    }

    fun setSexo(sexo: String) {
        _sexo.value = sexo
    }

    fun setEstadoSalud(salud: String) {
        _estadoSalud.value = salud
    }

    fun setCondicionesEspeciales(condiciones: List<String>) {
        _condicionesEspeciales.value = condiciones
    }

    fun setVideoUri(uri: Uri) {
        _videoUri.value = uri
    }

    fun setNotas(notas: String) {
        _notas.value = notas
    }

    fun setPrioridad(prioridad: String) {
        _prioridad.value = prioridad
    }

    fun setNecesitaVet(necesita: Boolean) {
        _necesitaVet.value = necesita
    }

    fun guardarRescate(voluntarioId: String, refugioId: String) {
        _saveState.value = RescateState.Loading

        viewModelScope.launch {
            try {
                // 1. Crear el animal
                val animal = Animal(
                    nombre = "Animal-${System.currentTimeMillis().toString().takeLast(4)}",
                    especie = _tipoAnimal.value ?: Constants.TIPO_PERRO,
                    raza = _raza.value ?: "Desconocido",
                    edad = _edad.value ?: Constants.EDAD_ADULTO,
                    sexo = _sexo.value ?: "macho",
                    estadoSalud = _estadoSalud.value ?: Constants.SALUD_BUENO,
                    condicionesEspeciales = _condicionesEspeciales.value ?: emptyList(),
                    fechaRescate = Date(),
                    ubicacionRescate = _ubicacion.value,
                    refugioId = refugioId,
                    prioridad = _prioridad.value ?: Constants.PRIORIDAD_MEDIA,
                    estadoAdopcion = Constants.ADOPCION_DISPONIBLE
                )

                // 2. Guardar animal en Firestore
                val animalResult = animalRepository.saveAnimal(animal)

                animalResult.onSuccess { animalId ->
                    // 3. Subir foto principal
                    _fotoPrincipal.value?.let { uri ->
                        val photoResult = animalRepository.uploadAnimalPhoto(animalId, uri)
                        photoResult.onSuccess { photoUrl ->
                            // Actualizar animal con la foto
                            animalRepository.updateAnimal(
                                animalId,
                                mapOf("fotosPrincipales" to listOf(photoUrl))
                            )
                        }
                    }

                    // 4. Subir fotos adicionales
                    _fotosAdicionales.value?.let { fotos ->
                        if (fotos.isNotEmpty()) {
                            animalRepository.uploadAnimalPhotos(animalId, fotos)
                        }
                    }

                    // 5. Crear registro de rescate
                    val rescate = Rescate(
                        animalId = animalId,
                        voluntarioId = voluntarioId,
                        ubicacion = _ubicacion.value ?: UbicacionRescate(),
                        descripcion = _notas.value ?: "",
                        prioridad = _prioridad.value ?: Constants.PRIORIDAD_MEDIA,
                        necesitaVeterinario = _necesitaVet.value ?: false,
                        fechaRescate = Date()
                    )

                    rescateRepository.saveRescate(rescate)

                    // 6. Subir video si existe
                    _videoUri.value?.let { uri ->
                        rescateRepository.uploadRescateVideo(animalId, uri)
                    }

                    _saveState.value = RescateState.Success(animalId)

                }.onFailure { exception ->
                    _saveState.value = RescateState.Error(
                        exception.message ?: "Error al guardar el rescate"
                    )
                }

            } catch (e: Exception) {
                _saveState.value = RescateState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}