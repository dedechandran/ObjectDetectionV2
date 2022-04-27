package com.finalproject.objectdetection

import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.finalproject.objectdetection.Retrofit
import com.finalproject.objectdetection.databinding.FragmentDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var canvas: Canvas
    private val boxPaint = Paint()
    private val textPaint = Paint()
    private var color: Int? = null

    private val retrofit by lazy {
        Retrofit.getInstance().create(ApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_details, container, false)
        _binding = FragmentDetailsBinding.bind(view)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString(EXTRA_URI)?.let {
            onAnalyzeImage(Uri.parse(it))
        }
        setupColor()
    }

    private fun getCapturedImage(bitmap: Bitmap): Bitmap {
        // Crop image to match imageView's aspect ratio
        val scaleFactor = Math.min(
            bitmap.width / binding.ivResult.width.toFloat(),
            bitmap.height / binding.ivResult.height.toFloat()
        )
//
        val deltaWidth = (bitmap.width - binding.ivResult.width * scaleFactor).toInt()
        val deltaHeight = (bitmap.height - binding.ivResult.height * scaleFactor).toInt()

        val scaledImage = Bitmap.createBitmap(
            bitmap,
            deltaWidth / 2,
            deltaHeight / 2,
            bitmap.width - deltaWidth,
            bitmap.height - deltaHeight
        )
        bitmap.recycle()
        return scaledImage
    }

    private fun setupColor() {
        color = Color.GREEN
        color?.let {
            boxPaint.color = it
            boxPaint.style = Paint.Style.STROKE; // stroke or fill or ...
            boxPaint.strokeWidth = 5.toFloat()
            textPaint.color = it
            textPaint.style = Paint.Style.FILL
            textPaint.textSize = 56.toFloat()
        }
    }

    private fun onAnalyzeImage(uri: Uri?) {
        if (uri == null) return
        val bitmap = getBitmap(uri)
        val scaledImage = bitmap.copy(Bitmap.Config.ARGB_8888,true)
        val encodedBitmap = Utils.encodeBitmap(scaledImage)
        val params = DetectionRequest(encodedImage = encodedBitmap)
//        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val response = retrofit.detectObject(params)
            launch(Dispatchers.Main) {
//                binding.progressBar.visibility = View.GONE
                binding.ivResult.setImageBitmap(scaledImage)
                canvas = Canvas(scaledImage)
                response.detectedObject.forEach {
                    canvas.drawRect(
                        Rect(
                            it.foodBoundingbox.foodX,
                            it.foodBoundingbox.foodY,
                            it.foodBoundingbox.foodX + it.foodBoundingbox.foodWidth,
                            it.foodBoundingbox.foodY + it.foodBoundingbox.foodHeight
                        ),
                        boxPaint
                    )
                    canvas.drawText(
                        it.foodLabel,
                        it.foodBoundingbox.foodX.toFloat(),
                        it.foodBoundingbox.foodY.toFloat(),
                        textPaint
                    )
                }
                binding.root.invalidate()
            }
        }
    }

    private fun getBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    requireContext().contentResolver,
                    uri
                )
            )
        } else {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        }
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )

        return resizedBitmap
    }

    companion object {
        const val EXTRA_URI = "extra_uri"
    }

}