package com.example.facedetectionapp.presentation.view

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.facedetectionapp.util.BaseFragment
import com.example.facedetectionapp.databinding.FragmentCameraBinding
import com.example.facedetectionapp.presentation.viewmodel.CameraViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class CameraFragment : BaseFragment<FragmentCameraBinding>(
    onInflate = FragmentCameraBinding::inflate
) {
    private lateinit var cameraExecutor: ExecutorService
    private var countdownTimer: CountDownTimer? = null
    private val countdownDurationMillis = 8000
    private val cameraViewModel by viewModels<CameraViewModel>()
    private var cameraProvider: ProcessCameraProvider? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCamera(binding.preview)
        observeTest()
    }

    private fun startCountdown() {
        countdownTimer = object : CountDownTimer(countdownDurationMillis.toLong(), 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.textTimer.text = "Timer: $secondsRemaining"
                Log.d("Countdown", "Timer: $secondsRemaining second")
            }

            override fun onFinish() {
                cameraViewModel.setNextTest()
            }
        }
        (countdownTimer as CountDownTimer).start()
    }


    private fun startCamera(preview: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(preview.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        lifecycleScope.launch {
                            imageProxy.proxyProcess().collectLatest { face ->
                                Log.e("TAG", "startCamera: $face")
                                cameraViewModel.identifyEmotion(face)
                            }
                        }
                    }
                }


            cameraProvider?.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )

        }, ContextCompat.getMainExecutor(requireContext()))

        cameraExecutor = Executors.newSingleThreadExecutor()

    }


    private fun observeTest() {
        lifecycleScope.launch {
            cameraViewModel.faceTestType.collectLatest {
                binding.textViewEmotion.text = it.testName
                countdownTimer?.cancel()
                startCountdown()
            }

        }
        lifecycleScope.launch {
            cameraViewModel.isEndTest.collectLatest {
                if (it) {
                    findNavController().popBackStack()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
//        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
        cameraProvider = null
        cameraExecutor?.shutdown()

        countdownTimer?.cancel()
        countdownTimer = null

//        cameraExecutor = null
    }


    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    fun ImageProxy.proxyProcess() = callbackFlow<Face> {
        val mediaImage = this@proxyProcess.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                this@proxyProcess.imageInfo.rotationDegrees
            )

            val options = FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

            val faceDetector = FaceDetection.getClient(options)

            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.size > 0) {
                        trySend(faces[0])
                    }
                    Log.e("face", "Yüz ${faces.size} yüz bulundu.")
                }
                .addOnFailureListener { e ->
                    Log.e("FaceDetection", "Yüz tespiti başarısız", e)
                }
                .addOnCompleteListener {
                    this@proxyProcess.close()
                }
            awaitClose {
                this@proxyProcess.close()
            }
        }


    }
}









