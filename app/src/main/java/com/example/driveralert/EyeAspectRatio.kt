package com.example.driveralert

import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Calculateur du EAR (Eye Aspect Ratio)
 * Le EAR diminue lorsque l'œil se ferme.
 * Seuil critique : EAR < 0.20 pendant > 1.5 seconde → somnolence détectée
 */
object EyeAspectRatio {
    
    /**
     * Calcule le EAR pour un œil en utilisant les landmarks ML Kit
     * ML Kit fournit un seul point pour chaque œil, donc on utilise une approximation
     * basée sur la probabilité d'ouverture et la position du visage
     */
    fun calculateEAR(
        leftEye: FaceLandmark?,
        rightEye: FaceLandmark?,
        leftEyeOpenProb: Float?,
        rightEyeOpenProb: Float?
    ): Double {
        // Si on a les probabilités d'ouverture, on les utilise directement
        val leftProb = leftEyeOpenProb ?: 0.5f
        val rightProb = rightEyeOpenProb ?: 0.5f
        
        // Convertir les probabilités en EAR approximatif
        // Probabilité 1.0 = œil complètement ouvert (EAR ~ 0.3)
        // Probabilité 0.0 = œil complètement fermé (EAR ~ 0.0)
        val leftEAR = leftProb * 0.3
        val rightEAR = rightProb * 0.3
        
        // EAR moyen des deux yeux
        return ((leftEAR + rightEAR) / 2.0).toDouble()
    }
    
    /**
     * Calcule le EAR en utilisant les distances entre les landmarks
     * Méthode alternative si on avait plus de points (comme avec MediaPipe)
     */
    fun calculateEARFromLandmarks(
        eyeLandmarks: List<android.graphics.PointF>
    ): Double {
        if (eyeLandmarks.size < 6) return 0.0
        
        // Calcul simplifié du EAR avec les points disponibles
        // EAR = (|p2-p6| + |p3-p5|) / (2 * |p1-p4|)
        // où p1-p6 sont les points autour de l'œil
        
        val vertical1 = distance(eyeLandmarks[1], eyeLandmarks[5])
        val vertical2 = distance(eyeLandmarks[2], eyeLandmarks[4])
        val horizontal = distance(eyeLandmarks[0], eyeLandmarks[3])
        
        return if (horizontal > 0) {
            (vertical1 + vertical2) / (2.0 * horizontal)
        } else {
            0.0
        }
    }
    
    private fun distance(p1: android.graphics.PointF, p2: android.graphics.PointF): Double {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt((dx * dx + dy * dy).toDouble())
    }
    
    /**
     * Détermine si les yeux sont fermés basé sur le EAR
     */
    fun areEyesClosed(ear: Double, threshold: Double = 0.20): Boolean {
        return ear < threshold
    }
}

