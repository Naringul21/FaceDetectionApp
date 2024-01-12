package com.example.facedetectionapp.ui

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
import androidx.room.Room
import com.example.facedetectionapp.AppDatabase
import com.example.facedetectionapp.data.EmotionResult
import com.example.facedetectionapp.util.BaseFragment
import com.example.facedetectionapp.databinding.FragmentCameraBinding
import com.example.facedetectionapp.util.EmotionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : BaseFragment<FragmentCameraBinding>(
    onInflate = FragmentCameraBinding::inflate
) {
    private lateinit var cameraExecutor: ExecutorService
    private var isPromptDisplayed = false
    private var emotionState: EmotionState = EmotionState.IDLE
    private var countdownTimer: CountDownTimer? = null
    private val countdownDurationMillis = 10000

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionHandler()
        startCamera(binding.preview)
        emotionState = EmotionState.IDLE
    }

    private fun startCountdown() {
        countdownTimer = object : CountDownTimer(countdownDurationMillis.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                Log.d("Countdown", "Timer: $secondsRemaining second")
            }

            override fun onFinish() {
                Toast.makeText(requireActivity(), "Fail! Time is up!", Toast.LENGTH_SHORT).show()
                logEmotionResult("Fail",  false)
            }
        }.start()
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
                    if (faces.isNotEmpty() && emotionState == EmotionState.COUNTDOWN) {
                            checkEmotion(faces)
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

        private fun checkEmotion(faces: List<Face>) {
        for (face in faces) {
            val rotY = face.headEulerAngleY
            val smilingProbability = face.smilingProbability

            if (rotY > 20) {
                Log.d("FaceDetectionRight", "Face rotated to the right. Yaw: $rotY")
                emotionState = EmotionState.TURN_RIGHT
                logEmotionResult("right", true)
            } else if (rotY < -20) {
                Log.d("FaceDetectionLeft", "Face rotated to the left. Yaw: $rotY")
                emotionState = EmotionState.TURN_LEFT
                logEmotionResult("right", false)
            } else if (smilingProbability!! > 0.7) {
                Log.d("FaceDetectionSmile", "Smile. Yaw: $smilingProbability")
                emotionState = EmotionState.LAUGH
                logEmotionResult("smile", true)
            }  else if (smilingProbability!! < 0.7) {
                Log.d("FaceDetectionNeutral", "Neutral. Yaw: $smilingProbability")
                emotionState = EmotionState.NOT_LAUGH
                logEmotionResult("smile", false)
            }else {
                Log.d("FaceDetection", "No faces detected.")
            }

            isPromptDisplayed = false
        }
    }

    private fun logEmotionResult(testText: String, isCorrect: Boolean) {
        val result = EmotionResult(
            testText = testText,
            isCorrect = isCorrect
        )

        insertResultIntoDatabase(result)
    }

    private fun insertResultIntoDatabase(result: EmotionResult) {
        val database = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "emotion_database"
        ).build()

        CoroutineScope(Dispatchers.IO).launch {
            database.headTurnResultDao().insert(result)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}








