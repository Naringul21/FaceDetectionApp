package com.example.facedetectionapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facedetectionapp.data.local.entities.EmotionResult
import com.example.facedetectionapp.data.repository.Repository
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(private val repo: Repository) : ViewModel() {

    private val _currentEmotionStateIndex = MutableStateFlow(0)
    val currentEmotionStateIndex = _currentEmotionStateIndex.asStateFlow()
    private var isTestInProgress = false

    private val _faceTestType =
        MutableStateFlow(EmotionTest("Head to Left", EmotionType.NEUTRAL))
    val faceTestType = _faceTestType.asStateFlow()

    private val _faceDetectionResult = MutableSharedFlow<Face>()
    val faceDetectionResult: SharedFlow<Face> get() = _faceDetectionResult

    var result = EmotionResult(0)

    //
    private val _isEndTest =
        MutableStateFlow(false)
    val isEndTest = _isEndTest.asStateFlow()
//
//    fun updateEmotionTest(emotionTestState: EmotionTest) {
//        _faceTestType.value = emotionTestState
//    }

    val emotionTestList = listOf(
        EmotionTest("Left", EmotionType.TURN_LEFT),
        EmotionTest("Right", EmotionType.TURN_RIGHT),
        EmotionTest("Smile", EmotionType.SMILE),
        EmotionTest("Neutral", EmotionType.NEUTRAL)
    )


    fun setNextTest() {
        if (_currentEmotionStateIndex.value < emotionTestList.size) {
            _currentEmotionStateIndex.value = _currentEmotionStateIndex.value + 1
            Log.e("index", "updateCurrentTestIndex:${_currentEmotionStateIndex.value} ")

        } else {
            insertTestResult()
            _currentEmotionStateIndex.value = 0
            _isEndTest.value = true
        }

    }


    private val emotionResult = EmotionResult(0)

    fun identifyEmotion(faces: Face) {
        val rotY = faces.headEulerAngleY
        val smilingProbability = faces.smilingProbability ?: 0f
        if (_currentEmotionStateIndex.value < emotionTestList.size) {
            _faceTestType.value = emotionTestList[_currentEmotionStateIndex.value]
        }
        when (faceTestType.value.emotionType) {
            EmotionType.TURN_LEFT -> {
                if (rotY.toInt() > 20) {
                    emotionResult.left = true
                    setNextTest()
                }
                Log.d("FaceDetectLeft", "$rotY")
            }

            EmotionType.TURN_RIGHT -> {
                if (rotY.toInt() < -20) {
                    setNextTest()
                    emotionResult.right = true
                }
                Log.d("FaceDetectRight", "$rotY")
            }

            EmotionType.SMILE -> {
                if (smilingProbability > 0.7) {
                    setNextTest()
                    emotionResult.smile = true
                }
                Log.d("FaceDetectSmile", "$smilingProbability")
            }

            EmotionType.NEUTRAL -> {
                if (smilingProbability.toInt() < 0.7) {
                    setNextTest()
                    emotionResult.neutral = true
                }


            }
        }
    }

//        if (rotY > 20) return EmotionType.TURN_LEFT
//        if (rotY < -20) return EmotionType.TURN_RIGHT
//        if (smilingProbability > 0.7) return EmotionType.SMILE
//        if (smilingProbability < 0.7) return EmotionType.NEUTRAL
//        return EmotionType.NEUTRAL
//
//    }

    fun insertTestResult() {
        viewModelScope.launch {
            repo.insertEmotion(
                result
            )
        }
    }
}

