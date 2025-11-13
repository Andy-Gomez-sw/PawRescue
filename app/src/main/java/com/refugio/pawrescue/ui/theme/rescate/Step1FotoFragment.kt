package com.refugio.pawrescue.ui.theme.rescate

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore // <<< IMPORTADO
import android.util.Log // <<< IMPORTADO
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider // <<< IMPORTADO
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.refugio.pawrescue.databinding.Step1FotoBinding
import java.io.File
import java.io.IOException // <<< IMPORTADO
import java.text.SimpleDateFormat
import java.util.*

// <<< SE ELIMINARON LOS IMPORTS DE CAMERAX (ImageCapture, Preview, CameraProvider, etc.) >>>

class Step1FotoFragment : Fragment() {

    private var _binding: Step1FotoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NuevoRescateViewModel by activityViewModels()

    // <<< Se eliminó imageCapture y outputDirectory >>>
    private var latestTmpUri: Uri? = null // <<< Variable para guardar la URI de la foto temporal

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si el usuario da permiso, lanzamos la cámara
            launchCameraIntent()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara requerido", Toast.LENGTH_SHORT).show()
        }
    }

    // --- LAUNCHER PARA ABRIR LA CÁMARA EXTERNA ---
    // Este reemplaza la lógica de takePhoto() con CameraX
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            // La foto se guardó exitosamente en la 'latestTmpUri'
            latestTmpUri?.let { uri ->
                viewModel.setFotoPrincipal(uri)
                showCapturedImage(uri)
            }
        } else {
            // El usuario canceló o hubo un error
            Log.e("Step1FotoFragment", "Captura de foto fallida o cancelada")
        }
    }

    // --- LAUNCHER PARA LA GALERÍA (Este se queda igual) ---
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setFotoPrincipal(it)
            showCapturedImage(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Step1FotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // <<< Ya no necesitamos 'outputDirectory' >>>
        setupUI()

        // <<< Ya no se inicia la cámara aquí, así que eliminamos checkCameraPermission() >>>
        // Ocultamos el 'previewView' de CameraX, ya no se usará.
        binding.previewView.visibility = View.GONE
    }

    private fun setupUI() {
        binding.btnCapture.setOnClickListener {
            // <<< Lógica actualizada >>>
            // Ahora, al capturar, revisamos permiso y lanzamos el INTENT
            checkPermissionAndLaunchCamera()
        }

        binding.btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnRetake.setOnClickListener {
            binding.ivCapturedImage.visibility = View.GONE
            binding.btnRetake.visibility = View.GONE
            binding.llPlaceholder.visibility = View.VISIBLE
            // <<< Ya no hay previewView que mostrar >>>
            latestTmpUri = null // Limpiamos la URI temporal
        }
    }

    // --- NUEVA FUNCIÓN ---
    // Revisa el permiso y lanza el launcher de permiso o la cámara directamente
    private fun checkPermissionAndLaunchCamera() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permiso ya concedido, lanzar la cámara
                launchCameraIntent()
            }
            else -> {
                // Pedir permiso
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // --- NUEVA FUNCIÓN ---
    // Esta es la versión Kotlin de 'abrirCamara()'
    private fun launchCameraIntent() {
        try {
            val photoFile = createImageFile()

            // Obtenemos la URI usando FileProvider (¡IMPORTANTE!)
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider", // Debe coincidir con tu Manifest
                photoFile
            )

            // Guardamos la URI para usarla en el callback de takePictureLauncher
            latestTmpUri = photoURI

            // Lanzamos el contrato moderno, pasándole la URI donde guardar
            takePictureLauncher.launch(photoURI)

        } catch (ex: Exception) {
            Log.e("Step1FotoFragment", "Error al preparar la cámara", ex)
            Toast.makeText(requireContext(), "Error al preparar la cámara: ${ex.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- NUEVA FUNCIÓN ---
    // Esta es la versión Kotlin de 'createImageFile()'
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Creamos un nombre de archivo único
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        // Usamos el directorio de caché externo, que es el lugar correcto para esto
        val storageDir = requireContext().externalCacheDir ?: requireContext().cacheDir

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefijo */
            ".jpg", /* sufijo */
            storageDir /* directorio */
        )
    }

    private fun showCapturedImage(uri: Uri) {
        binding.ivCapturedImage.setImageURI(uri)
        binding.ivCapturedImage.visibility = View.VISIBLE
        binding.btnRetake.visibility = View.VISIBLE
        // <<< 'previewView' ya no se usa, así que no se toca >>>
        binding.llPlaceholder.visibility = View.GONE
    }

    // <<< SE ELIMINARON LAS FUNCIONES: >>>
    // - checkCameraPermission()
    // - startCamera()
    // - takePhoto()
    // - getOutputDirectory()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}