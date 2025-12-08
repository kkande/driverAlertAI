package com.example.driveralert

/**
 * Estimateur de la pose de la tête (Head Pose Estimation)
 * Analyse l'orientation de la tête pour détecter la fatigue
 * Mesure les angles pitch, roll, yaw (angles d'Euler)
 */
object HeadPoseEstimator {
    
    /**
     * Estime si la tête est inclinée (signe de fatigue)
     * @param eulerX Pitch: inclinaison avant/arrière (tête qui tombe vers l'avant)
     * @param eulerY Yaw: rotation gauche/droite
     * @param eulerZ Roll: inclinaison gauche/droite
     */
    fun isHeadTilted(
        eulerX: Float?,
        eulerY: Float?,
        eulerZ: Float?
    ): Boolean {
        if (eulerX == null && eulerY == null && eulerZ == null) {
            return false
        }
        
        // Seuils pour détecter une inclinaison significative (augmentés pour être moins sensibles)
        val pitchThreshold = 25.0f // Degrés - tête qui tombe vers l'avant
        val rollThreshold = 25.0f  // Degrés - inclinaison latérale (augmenté)
        val yawThreshold = 45.0f   // Degrés - rotation (augmenté, car tourner la tête est normal)
        
        val pitch = eulerX ?: 0f
        val roll = eulerZ ?: 0f
        val yaw = eulerY ?: 0f
        
        // Tête qui tombe vers l'avant (pitch positif) = signe principal de fatigue
        val headFallingForward = pitch > pitchThreshold
        
        // Tête inclinée sur le côté (roll) = signe de fatigue seulement si très inclinée
        val headTiltedSideways = kotlin.math.abs(roll) > rollThreshold
        
        // Rotation (yaw) n'est PAS un signe de fatigue - c'est normal de tourner la tête
        // On ignore le yaw pour la détection de fatigue
        
        return headFallingForward || headTiltedSideways
    }
    
    /**
     * Retourne une description de l'orientation de la tête
     */
    fun getHeadPoseDescription(
        eulerX: Float?,
        eulerY: Float?,
        eulerZ: Float?
    ): String {
        if (eulerX == null && eulerY == null && eulerZ == null) {
            return "Pose non détectée"
        }
        
        val pitch = eulerX ?: 0f
        val roll = eulerZ ?: 0f
        val yaw = eulerY ?: 0f
        
        val descriptions = mutableListOf<String>()
        
        when {
            pitch > 20 -> descriptions.add("Tête penchée vers l'avant")
            pitch < -20 -> descriptions.add("Tête penchée vers l'arrière")
        }
        
        when {
            roll > 15 -> descriptions.add("Tête inclinée à droite")
            roll < -15 -> descriptions.add("Tête inclinée à gauche")
        }
        
        when {
            yaw > 30 -> descriptions.add("Tête tournée à droite")
            yaw < -30 -> descriptions.add("Tête tournée à gauche")
        }
        
        return descriptions.joinToString(", ").ifEmpty { "Tête droite" }
    }
}

