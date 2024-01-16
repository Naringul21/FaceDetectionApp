package com.example.facedetectionapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facedetectionapp.data.local.entities.EmotionResult
import com.example.facedetectionapp.data.repository.Repository
import com.example.facedetectionapp.util.EmotionTest
import com.example.facedetectionapp.util.EmotionTestState
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(val repo: Repository) :ViewModel(){

    private val _emotionState = MutableStateFlow(EmotionTestState("Head to Left", EmotionTest.TURN_LEFT))
    val emotionState = _emotionState.asStateFlow()

    companion object {
        var emotionResult = EmotionResult()
    }

    fun updateEmotionTest(emotionTestState: EmotionTestState) {
        _emotionState.value = emotionTestState
    }


    fun checkEmotion(faces: Face) {
            val rotY = faces.headEulerAngleY
            val smilingProbability = faces.smilingProbability
            when (emotionState.value.emotionTest) {
                EmotionTest.TURN_LEFT -> {
            if (rotY > 20) {
                Log.d("FaceDetectionLeft", "Face rotated to the left. Yaw: $rotY")
                emotionResult.testText="Right"
                _emotionState.value = EmotionTestState("Head to Right", EmotionTest.TURN_RIGHT)

            } }
                EmotionTest.TURN_RIGHT -> {
                    if (rotY < -20) {
                Log.d("FaceDetectionRight", "Face rotated to the right. Yaw: $rotY")
                        emotionResult.testText="Left"
                        _emotionState.value = EmotionTestState("Smile", EmotionTest.SMILE)

            } }
                EmotionTest.SMILE -> {
                    if (smilingProbability!! > 0.7) {
                Log.d("FaceDetectionSmile", "Smile. Yaw: $smilingProbability")
                        emotionResult.testText="Smile"
                        _emotionState.value = EmotionTestState("Neutral", EmotionTest.NEUTRAL)

            } }
                EmotionTest.NEUTRAL -> {
                    if (smilingProbability!! < 0.7) {
                Log.d("FaceDetectionNeutral", "Neutral. Yaw: $smilingProbability")
                        emotionResult.testText="Neutral"
                        _emotionState.value = EmotionTestState("Head to Left", EmotionTest.TURN_LEFT)

            }
                    insertEmotion()
                    emotionResult=EmotionResult()


        }}}

    fun insertEmotion() {
        viewModelScope.launch {
            repo.insertEmotion(emotionResult)
        }
    }}
