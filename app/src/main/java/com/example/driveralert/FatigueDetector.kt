package com.example.driveralert

import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

/**
 * Détecteur de fatigue basé sur :
 * - EAR (Eye Aspect Ratio) < 0.20 pendant > 1.5 seconde
 * - Head Pose (inclinaison de la tête)
 * - Clignements lents
 * - Bâillements
 */
class FatigueDetector(
    private val context: Context,
    private val onFatigueDetected: (FatigueLevel) -> Unit
) {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    
    // Seuils de détection
    private val EAR_THRESHOLD = 0.20
    private val EYES_CLOSED_DURATION_THRESHOLD_MS = 1500L // 1.5 seconde
    private val BLINK_DURATION_THRESHOLD_MS = 500L // Clignement lent
    
    // État de détection
    private var eyesClosedStartTime: AtomicLong = AtomicLong(0)
    private var lastEAR: Double = 0.3
    private var consecutiveLowEARCount = 0
    private var lastBlinkStartTime: AtomicLong = AtomicLong(0)
    
    /**
     * Traite un résultat de détection de visage et détecte la fatigue
     */
    fun processFaceDetection(result: FaceDetectionResult) {
        if (!result.hasValidLandmarks()) {
            resetDetection()
            return
        }
        
        // Calculer le EAR
        val ear = EyeAspectRatio.calculateEAR(
            result.leftEye,
            result.rightEye,
            result.leftEyeOpenProbability,
            result.rightEyeOpenProbability
        )
        
        lastEAR = ear
        
        // Vérifier si les yeux sont fermés
        val eyesClosed = EyeAspectRatio.areEyesClosed(ear, EAR_THRESHOLD)
        
        // Vérifier la pose de la tête
        val headTilted = HeadPoseEstimator.isHeadTilted(
            result.headEulerX,
            result.headEulerY,
            result.headEulerZ
        )
        
        // Détecter les bâillements (bouche ouverte + yeux fermés)
        val yawning = detectYawning(result)
        
        // Détecter les clignements lents
        val slowBlink = detectSlowBlink(ear)
        
        // Détecter la fatigue
        val fatigueLevel = determineFatigueLevel(
            eyesClosed,
            headTilted,
            yawning,
            slowBlink,
            ear
        )
        
        if (fatigueLevel != FatigueLevel.NONE) {
            onFatigueDetected(fatigueLevel)
            triggerAlert(fatigueLevel)
        }
    }
    
    private fun detectYawning(result: FaceDetectionResult): Boolean {
        // Un bâillement est détecté si :
        // - La bouche est ouverte (probabilité de sourire faible ou landmarks de bouche éloignés)
        // - Les yeux sont fermés ou presque fermés
        val eyesClosed = EyeAspectRatio.areEyesClosed(
            EyeAspectRatio.calculateEAR(
                result.leftEye,
                result.rightEye,
                result.leftEyeOpenProbability,
                result.rightEyeOpenProbability
            )
        )
        
        // Vérifier si la bouche est ouverte en comparant les landmarks
        val mouthOpen = result.leftMouth != null && result.rightMouth != null && 
                       result.bottomMouth != null && result.noseBase != null
        
        if (mouthOpen) {
            val leftMouth = result.leftMouth!!.position
            val rightMouth = result.rightMouth!!.position
            val bottomMouth = result.bottomMouth!!.position
            val noseBase = result.noseBase!!.position
            
            // Distance verticale entre le nez et le bas de la bouche
            val mouthHeight = kotlin.math.abs(bottomMouth.y - noseBase.y)
            // Distance horizontale entre les coins de la bouche
            val mouthWidth = kotlin.math.abs(rightMouth.x - leftMouth.x)
            
            // Si la bouche est grande (hauteur > largeur * 0.5), c'est probablement un bâillement
            val mouthOpenRatio = mouthHeight / mouthWidth
            return mouthOpenRatio > 0.3 && eyesClosed
        }
        
        return false
    }
    
    private fun detectSlowBlink(ear: Double): Boolean {
        val currentTime = System.currentTimeMillis()
        
        if (EyeAspectRatio.areEyesClosed(ear, EAR_THRESHOLD)) {
            if (lastBlinkStartTime.get() == 0L) {
                lastBlinkStartTime.set(currentTime)
            }
            
            val blinkDuration = currentTime - lastBlinkStartTime.get()
            if (blinkDuration > BLINK_DURATION_THRESHOLD_MS) {
                lastBlinkStartTime.set(0L)
                return true
            }
        } else {
            lastBlinkStartTime.set(0L)
        }
        
        return false
    }
    
    private fun determineFatigueLevel(
        eyesClosed: Boolean,
        headTilted: Boolean,
        yawning: Boolean,
        slowBlink: Boolean,
        ear: Double
    ): FatigueLevel {
        val currentTime = System.currentTimeMillis()
        
        // Gérer le temps pendant lequel les yeux sont fermés
        if (eyesClosed) {
            if (eyesClosedStartTime.get() == 0L) {
                eyesClosedStartTime.set(currentTime)
            }
            
            val eyesClosedDuration = currentTime - eyesClosedStartTime.get()
            
            // Fatigue critique : yeux fermés > 1.5 seconde
            if (eyesClosedDuration > EYES_CLOSED_DURATION_THRESHOLD_MS) {
                return FatigueLevel.CRITICAL
            }
            
            // Fatigue élevée : yeux fermés > 1 seconde
            if (eyesClosedDuration > 1000L) {
                return FatigueLevel.HIGH
            }
        } else {
            eyesClosedStartTime.set(0L)
        }
        
        // Bâillement = fatigue modérée à élevée
        if (yawning) {
            return FatigueLevel.HIGH
        }
        
        // Clignement lent = fatigue modérée
        if (slowBlink) {
            return FatigueLevel.MODERATE
        }
        
        // Tête inclinée = fatigue modérée
        if (headTilted) {
            return FatigueLevel.MODERATE
        }
        
        // EAR faible mais yeux pas complètement fermés = fatigue légère
        if (ear < 0.25 && ear >= EAR_THRESHOLD) {
            consecutiveLowEARCount++
            if (consecutiveLowEARCount > 10) {
                return FatigueLevel.LOW
            }
        } else {
            consecutiveLowEARCount = 0
        }
        
        return FatigueLevel.NONE
    }
    
    private fun triggerAlert(level: FatigueLevel) {
        when (level) {
            FatigueLevel.CRITICAL -> {
                vibrate(500)
                // Ici on pourrait aussi déclencher une alerte sonore
            }
            FatigueLevel.HIGH -> {
                vibrate(300)
            }
            FatigueLevel.MODERATE -> {
                vibrate(200)
            }
            FatigueLevel.LOW -> {
                vibrate(100)
            }
            FatigueLevel.NONE -> {}
        }
    }
    
    private fun vibrate(durationMs: Long) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(durationMs)
        }
    }
    
    private fun resetDetection() {
        eyesClosedStartTime.set(0L)
        lastBlinkStartTime.set(0L)
        consecutiveLowEARCount = 0
    }
}

/**
 * Niveaux de fatigue détectés
 */
enum class FatigueLevel {
    NONE,      // Aucune fatigue détectée
    LOW,       // Fatigue légère
    MODERATE,  // Fatigue modérée
    HIGH,      // Fatigue élevée
    CRITICAL   // Fatigue critique (yeux fermés > 1.5s)
}

