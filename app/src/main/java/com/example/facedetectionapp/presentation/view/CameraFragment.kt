package com.example.facedetectionapp.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.facedetectionapp.R
import com.example.facedetectionapp.util.BaseFragment
import com.example.facedetectionapp.databinding.FragmentCameraBinding
import com.example.facedetectionapp.presentation.viewmodel.CameraViewModel
import com.example.facedetectionapp.util.EmotionTest
import com.example.facedetectionapp.util.EmotionTestState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class CameraFragment : BaseFragment<FragmentCameraBinding>(
    onInflate = FragmentCameraBinding::inflate
) {
    private lateinit var cameraExecutor: ExecutorService
    private var emotionTest: EmotionTest = EmotionTest.TURN_LEFT
    private var countdownTimer: CountDownTimer? = null
    private val countdownDurationMillis = 10000
    private val cameraViewModel by viewModels<CameraViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionHandler()
        startCamera(binding.preview)
        emotionTest = EmotionTest.TURN_LEFT
        observeTest()
    }

    private fun startCountdown() {
        countdownTimer = object : CountDownTimer(countdownDurationMillis.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.textTimer.text = secondsRemaining.toString()
                Log.d("Countdown", "Timer: $secondsRemaining second")
            }

            override fun onFinish() {
                handlerCountdownFinish()
            }
        }
        countdownTimer?.start()
    }



    private fun handlerCountdownFinish() {
        when (emotionTest) {
            EmotionTest.TURN_LEFT -> {
                cameraViewModel.updateEmotionTest(
                    EmotionTestState(
                        "Head to Right",
                        EmotionTest.TURN_RIGHT
                    )
                )
            }

            EmotionTest.TURN_RIGHT -> {
                cameraViewModel.updateEmotionTest(EmotionTestState("Smile", EmotionTest.SMILE))
            }

            EmotionTest.SMILE -> {
                cameraViewModel.updateEmotionTest(
                    EmotionTestState(
                        "Neutral",
                        EmotionTest.NEUTRAL
                    )
                )
            }

            EmotionTest.NEUTRAL -> {
                cameraViewModel.insertEmotion()
                stopCountdown()
                findNavController().navigate(R.id.action_cameraFragment_to_homeFragment)

            }
        }
    }

    private fun stopCountdown() {
        countdownTimer?.cancel()
    }


    private fun startCamera(preview: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

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
                        proxyProgress(imageProxy)
                    }
                }


            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )

        }, ContextCompat.getMainExecutor(requireContext()))

        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun proxyProgress(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            val options = FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build()

            val faceDetector = FaceDetection.getClient(options)
            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        cameraViewModel.checkEmotion(faces[0])
                        Log.d("FaceDetectionSuccess", "Success")
                    }
                }

                .addOnFailureListener { e ->
                    Log.e("FaceDetectionError", "Face detection failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }


    private fun permissionHandler() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
        } else {
        }
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    fun observeTest() {
        lifecycleScope.launch {
            cameraViewModel.emotionState.collectLatest {
                binding.textViewEmotion.text = it.testName
                countdownTimer?.cancel()
                startCountdown()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}








