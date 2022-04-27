package com.finalproject.objectdetection

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.finalproject.objectdetection.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var cameraPermissionAllowed = false
    private var readStoragePermissionAllowed = false
    private var writeStoragePermissionAllowed = false
    private val requestMultiplePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            cameraPermissionAllowed = permissions[android.Manifest.permission.CAMERA] ?: false
            readStoragePermissionAllowed = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
            writeStoragePermissionAllowed = permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
            if (!cameraPermissionAllowed && !readStoragePermissionAllowed && !writeStoragePermissionAllowed) {
                Snackbar.make(binding.root,"Tidak bisa menggunakan fitur ini karena dibutuhkan permission",Snackbar.LENGTH_SHORT).show()
            }
        }
    private var outputUri: Uri? = null
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            val bundle = bundleOf(DetailsFragment.EXTRA_URI to outputUri.toString())
            findNavController().navigate(R.id.action_homeFragment_to_detailsFragment, bundle)
        }
    }
    private val takePictureFromGalleryLauncer = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bundle = bundleOf(DetailsFragment.EXTRA_URI to it.toString())
            findNavController().navigate(R.id.action_homeFragment_to_detailsFragment, bundle)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        _binding = FragmentHomeBinding.bind(view)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCamera.setOnClickListener {
            setOnClickListenerWithPermissionCheck {
                createTempFile()
                takePictureLauncher.launch(outputUri)
            }
        }
        binding.btnGallery.setOnClickListener {
            setOnClickListenerWithPermissionCheck {
                takePictureFromGalleryLauncer.launch("image/*")
            }
        }
        binding.btnScales.setOnClickListener {
            setOnClickListenerWithPermissionCheck {
                Toast.makeText(requireContext(),"btnCameraClicked",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setOnClickListenerWithPermissionCheck(block: () -> Unit) {
        if (cameraPermissionAllowed && readStoragePermissionAllowed && writeStoragePermissionAllowed) {
            block.invoke()
        }else {
            requestMultiplePermissionLauncher.launch(REQUESTED_PERMISSION)
        }
    }

    private fun createTempFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = createContentValuesImage()
            requireContext().contentResolver?.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
                .let {
                    outputUri = it
                }
        } else {
            val tempFile = createTempFileImage()
            outputUri = FileProvider.getUriForFile(
                requireContext(),
                BuildConfig.APPLICATION_ID.plus(".provider"),
                tempFile
            )
        }
    }

    private fun createTempFileImage(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val imageFileName = timeStamp.toString() + "_"
        val fileDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,
            ".jpg",
            fileDirectory
        )
    }

    private fun createContentValuesImage(): ContentValues {
        val timeStamp = System.currentTimeMillis()
        val currentDateTime = Date(timeStamp)
        val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val imageFileName = format.format(currentDateTime) + "_"
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val REQUESTED_PERMISSION = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}