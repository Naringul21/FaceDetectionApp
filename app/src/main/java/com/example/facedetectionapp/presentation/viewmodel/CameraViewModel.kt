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


    private val _faceTestType =
        MutableStateFlow(EmotionTest("", EmotionType.NEUTRAL))
    val faceTestType = _faceTestType.asStateFlow()



    private val _isEndTest =
        MutableStateFlow(false)
    val isEndTest = _isEndTest.asStateFlow()

    val emotionTestList = listOf(
        EmotionTest("Turn to Left", EmotionType.TURN_LEFT),
        EmotionTest("Turn to Right", EmotionType.TURN_RIGHT),
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
            Log.e("room_test", "${emotionResult.left} ")
        }

    }


    private var emotionResult = EmotionResult(0)

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
                Log.d("FaceDetectLeft", "${emotionResult.left}")
            }

            EmotionType.TURN_RIGHT -> {
                if (rotY.toInt() < -20) {
                    emotionResult.right = true
                    setNextTest()

                }
                Log.d("FaceDetectRight", "${emotionResult.right}")
            }

            EmotionType.SMILE -> {
                if (smilingProbability > 0.7) {
                    emotionResult.smile = true
                    setNextTest()

                }
                Log.d("FaceDetectSmile", "${emotionResult.smile}")
            }

            EmotionType.NEUTRAL -> {
                if (smilingProbability < 0.7) {
                    emotionResult.neutral = true
                    setNextTest()

                }
                Log.d("FaceDetectNeutral", "${emotionResult.neutral}")


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
            repo.insertEmotion(emotionResult)
            Log.e("RoomTEST", "$emotionResult")
        }
    }
}

