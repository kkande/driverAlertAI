package com.example.driveralert

import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicBoolean

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
    private var alarmMediaPlayer: MediaPlayer? = null
    private val isAlarmPlaying = AtomicBoolean(false)
    
    // Seuils de détection
    private val EAR_THRESHOLD = 0.20 // Seuil pour yeux fermés
    private val EAR_NORMAL_THRESHOLD = 0.25 // Seuil pour yeux ouverts normalement
    private val EYES_CLOSED_DURATION_THRESHOLD_MS = 3000L // 3 secondes pour l'endormissement
    private val BLINK_DURATION_THRESHOLD_MS = 500L // Clignement lent
    
    // État de détection
    private var eyesClosedStartTime: AtomicLong = AtomicLong(0)
    private var lastEAR: Double = 0.3
    private var consecutiveLowEARCount = 0
    private var lastBlinkStartTime: AtomicLong = AtomicLong(0)
    private var lastAlarmTriggerTime: AtomicLong = AtomicLong(0)
    
    /**
     * Traite un résultat de détection de visage et détecte la fatigue
     */
    fun processFaceDetection(result: FaceDetectionResult) {
        if (!result.hasValidLandmarks()) {
            resetDetection()
            onFatigueDetected(FatigueLevel.NONE)
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
        
        // Toujours notifier le niveau de fatigue (même si NONE)
        onFatigueDetected(fatigueLevel)
        
        if (fatigueLevel != FatigueLevel.NONE) {
            triggerAlert(fatigueLevel)
        } else {
            // Arrêter l'alarme si les yeux sont ouverts et pas de fatigue
            stopEmergencyAlarm()
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
        
        // VÉRIFICATION PRÉLIMINAIRE : Si les yeux sont bien ouverts (EAR > 0.25), pas de fatigue
        if (!eyesClosed && ear > EAR_NORMAL_THRESHOLD) {
            // Réinitialiser tous les compteurs
            eyesClosedStartTime.set(0L)
            lastBlinkStartTime.set(0L)
            consecutiveLowEARCount = 0
            return FatigueLevel.NONE
        }
        
        // PRIORITÉ 1 : Gérer le temps pendant lequel les yeux sont fermés
        // C'est le signe le plus fiable de fatigue
        if (eyesClosed) {
            if (eyesClosedStartTime.get() == 0L) {
                eyesClosedStartTime.set(currentTime)
            }
            
            val eyesClosedDuration = currentTime - eyesClosedStartTime.get()
            
            // Fatigue critique : yeux fermés > 3 secondes (endormissement)
            if (eyesClosedDuration > EYES_CLOSED_DURATION_THRESHOLD_MS) {
                // Déclencher l'alerte sonore d'urgence si elle n'est pas déjà en cours
                if (!isAlarmPlaying.get() && (currentTime - lastAlarmTriggerTime.get() > 5000L)) {
                    triggerEmergencyAlarm()
                    lastAlarmTriggerTime.set(currentTime)
                }
                return FatigueLevel.CRITICAL
            }
            
            // Fatigue élevée : yeux fermés > 2 secondes
            if (eyesClosedDuration > 2000L) {
                return FatigueLevel.HIGH
            }
            
            // Fatigue modérée : yeux fermés > 1 seconde
            if (eyesClosedDuration > 1000L) {
                return FatigueLevel.MODERATE
            }
        } else {
            // Si les yeux sont ouverts, réinitialiser le compteur
            eyesClosedStartTime.set(0L)
        }
        
        // PRIORITÉ 2 : Bâillement (seulement si les yeux sont aussi fermés)
        if (yawning && eyesClosed) {
            return FatigueLevel.HIGH
        }
        
        // PRIORITÉ 3 : Clignement lent (seulement si les yeux sont fermés)
        if (slowBlink && eyesClosed) {
            return FatigueLevel.MODERATE
        }
        
        // PRIORITÉ 4 : Tête inclinée (seulement si combinée avec yeux fermés)
        // Ne déclencher la fatigue basée sur la pose de la tête QUE si :
        // - Les yeux sont fermés ET l'EAR est vraiment faible
        if (headTilted && eyesClosed && ear < EAR_THRESHOLD) {
            return FatigueLevel.MODERATE
        }
        
        // PRIORITÉ 5 : EAR faible mais yeux pas complètement fermés = fatigue légère
        // Seulement si cela persiste pendant plusieurs frames ET que l'EAR est vraiment bas
        // ET que les yeux ne sont pas complètement ouverts
        if (ear < 0.22 && ear >= EAR_THRESHOLD && !eyesClosed && ear < EAR_NORMAL_THRESHOLD) {
            consecutiveLowEARCount++
            if (consecutiveLowEARCount > 40) { // Augmenté encore plus pour être moins sensible
                return FatigueLevel.LOW
            }
        } else {
            consecutiveLowEARCount = 0
        }
        
        // Par défaut, pas de fatigue si les yeux sont ouverts normalement
        return FatigueLevel.NONE
    }
    
    private fun triggerAlert(level: FatigueLevel) {
        when (level) {
            FatigueLevel.CRITICAL -> {
                vibrate(500)
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
            FatigueLevel.NONE -> {
                // Arrêter l'alarme si les yeux sont ouverts
                stopEmergencyAlarm()
            }
        }
    }
    
    private fun triggerEmergencyAlarm() {
        if (isAlarmPlaying.get()) return
        
        try {
            // Utiliser l'alarme système d'urgence
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            alarmMediaPlayer = MediaPlayer.create(context, alarmUri).apply {
                isLooping = true
                setVolume(1.0f, 1.0f)
                start()
            }
            
            isAlarmPlaying.set(true)
            
            // Arrêter automatiquement après 10 secondes pour éviter de surcharger
            CoroutineScope(Dispatchers.Main).launch {
                delay(10000)
                stopEmergencyAlarm()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isAlarmPlaying.set(false)
        }
    }
    
    private fun stopEmergencyAlarm() {
        if (!isAlarmPlaying.get()) return
        
        try {
            alarmMediaPlayer?.stop()
            alarmMediaPlayer?.release()
            alarmMediaPlayer = null
            isAlarmPlaying.set(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun release() {
        stopEmergencyAlarm()
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
        stopEmergencyAlarm()
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

