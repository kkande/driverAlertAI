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
        
        // Seuils pour détecter une inclinaison significative
        val pitchThreshold = 20.0f // Degrés
        val rollThreshold = 15.0f  // Degrés
        val yawThreshold = 30.0f   // Degrés
        
        val pitch = eulerX ?: 0f
        val roll = eulerZ ?: 0f
        val yaw = eulerY ?: 0f
        
        // Tête qui tombe vers l'avant (pitch positif) = signe de fatigue
        val headFallingForward = kotlin.math.abs(pitch) > pitchThreshold && pitch > 0
        
        // Tête inclinée sur le côté (roll) = signe de fatigue
        val headTiltedSideways = kotlin.math.abs(roll) > rollThreshold
        
        // Rotation excessive (yaw) peut aussi indiquer de la fatigue
        val headRotated = kotlin.math.abs(yaw) > yawThreshold
        
        return headFallingForward || headTiltedSideways || headRotated
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

