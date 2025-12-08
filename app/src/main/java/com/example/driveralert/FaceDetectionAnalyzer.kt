package com.example.driveralert

import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

/**
 * Analyseur d'image pour la détection de visage avec ML Kit
 * Intègre CameraX → MLKit → Landmarks
 */
class FaceDetectionAnalyzer(
    private val onFaceDetected: (FaceDetectionResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val faceDetector: FaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.1f)
            .enableTracking()
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces[0] // Prendre le premier visage détecté
                        val result = FaceDetectionResult(
                            face = face,
                            leftEye = face.getLandmark(FaceLandmark.LEFT_EYE),
                            rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE),
                            leftCheek = face.getLandmark(FaceLandmark.LEFT_CHEEK),
                            rightCheek = face.getLandmark(FaceLandmark.RIGHT_CHEEK),
                            noseBase = face.getLandmark(FaceLandmark.NOSE_BASE),
                            leftMouth = face.getLandmark(FaceLandmark.MOUTH_LEFT),
                            rightMouth = face.getLandmark(FaceLandmark.MOUTH_RIGHT),
                            bottomMouth = face.getLandmark(FaceLandmark.MOUTH_BOTTOM),
                            headEulerX = face.headEulerAngleX,
                            headEulerY = face.headEulerAngleY,
                            headEulerZ = face.headEulerAngleZ,
                            leftEyeOpenProbability = face.leftEyeOpenProbability,
                            rightEyeOpenProbability = face.rightEyeOpenProbability,
                            smilingProbability = face.smilingProbability
                        )
                        onFaceDetected(result)
                    } else {
                        onFaceDetected(FaceDetectionResult.empty())
                    }
                }
                .addOnFailureListener {
                    onFaceDetected(FaceDetectionResult.empty())
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

/**
 * Résultat de la détection de visage avec tous les landmarks nécessaires
 */
data class FaceDetectionResult(
    val face: com.google.mlkit.vision.face.Face? = null,
    val leftEye: FaceLandmark? = null,
    val rightEye: FaceLandmark? = null,
    val leftCheek: FaceLandmark? = null,
    val rightCheek: FaceLandmark? = null,
    val noseBase: FaceLandmark? = null,
    val leftMouth: FaceLandmark? = null,
    val rightMouth: FaceLandmark? = null,
    val bottomMouth: FaceLandmark? = null,
    val headEulerX: Float? = null,
    val headEulerY: Float? = null,
    val headEulerZ: Float? = null,
    val leftEyeOpenProbability: Float? = null,
    val rightEyeOpenProbability: Float? = null,
    val smilingProbability: Float? = null
) {
    fun hasValidLandmarks(): Boolean {
        return leftEye != null && rightEye != null && 
               noseBase != null && leftMouth != null && 
               rightMouth != null && bottomMouth != null
    }

    companion object {
        fun empty() = FaceDetectionResult()
    }
}

