package com.example.driveralert

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark

/**
 * Composant pour dessiner des contours personnalisés autour du visage détecté
 */
@Composable
fun FaceOverlay(
    face: Face?,
    modifier: Modifier = Modifier,
    faceColor: Color = Color.Green,
    fatigueColor: Color = Color.Red,
    isFatigueDetected: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    Canvas(modifier = modifier.fillMaxSize()) {
        if (face == null) return@Canvas
        
        val boundingBox = face.boundingBox
        val faceRect = Rect(
            left = boundingBox.left.toFloat(),
            top = boundingBox.top.toFloat(),
            right = boundingBox.right.toFloat(),
            bottom = boundingBox.bottom.toFloat()
        )
        
        // Couleur du contour selon l'état de fatigue
        val strokeColor = if (isFatigueDetected) fatigueColor else faceColor
        val strokeWidth = if (isFatigueDetected) 6f else 4f
        
        // Dessiner le rectangle principal autour du visage
        drawRect(
            color = strokeColor,
            style = Stroke(width = strokeWidth),
            topLeft = Offset(faceRect.left, faceRect.top),
            size = androidx.compose.ui.geometry.Size(
                width = faceRect.width,
                height = faceRect.height
            )
        )
        
        // Dessiner des coins arrondis personnalisés
        val cornerLength = 30f
        val cornerWidth = strokeWidth
        
        // Coin supérieur gauche
        drawLine(
            color = strokeColor,
            strokeWidth = cornerWidth,
            start = Offset(faceRect.left, faceRect.top),
            end = Offset(faceRect.left + cornerLength, faceRect.top)
        )
        drawLine(
            color = strokeColor,
            strokeWidth = cornerWidth,
            start = Offset(faceRect.left, faceRect.top),
            end = Offset(faceRect.left, faceRect.top + cornerLength)
        )
        
        // Coin supérieur droit
        drawLine(
            color = strokeColor,
            strokeWidth = cornerWidth,
            start = Offset(faceRect.right, faceRect.top),
            end = Offset(faceRect.right - cornerLength, faceRect.top)
        )
        drawLine(
            color = strokeColor,
            strokeWidth = cornerWidth,
            start = Offset(faceRect.right, faceRect.top),
            end = Offset(faceRect.right, faceRect.top + cornerLength)
        )
        
        // Coin inférieur gauche
        drawLine(
            color = strokeColor,
            strokeWidth = cornerWidth,
            start = Offset(faceRect.left, faceRect.bottom),
            end = Offset(faceRect.left + cornerLength, faceRect.bottom)
        )
        drawLine(
            color = strokeColor,
            strokeWidth = cornerWidth,
            start = Offset(faceRect.left, faceRect.bottom),
            end = Offset(faceRect.left, faceRect.bottom - cornerLength)
        )
        
        // Coin inférieur droit
        drawLine(
            color = strokeColor,
            strokeWidth = cornerWidth,
            start = Offset(faceRect.right, faceRect.bottom),
            end = Offset(faceRect.right - cornerLength, faceRect.bottom)
        )
        drawLine(
            color = strokeColor,
            strokeWidth = cornerWidth,
            start = Offset(faceRect.right, faceRect.bottom),
            end = Offset(faceRect.right, faceRect.bottom - cornerLength)
        )
        
        // Dessiner les landmarks importants si disponibles
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
        val noseBase = face.getLandmark(FaceLandmark.NOSE_BASE)
        
        // Cercle autour de l'œil gauche
        leftEye?.let { eye ->
            drawCircle(
                color = strokeColor.copy(alpha = 0.5f),
                radius = 15f,
                center = Offset(eye.position.x, eye.position.y),
                style = Stroke(width = 2f)
            )
        }
        
        // Cercle autour de l'œil droit
        rightEye?.let { eye ->
            drawCircle(
                color = strokeColor.copy(alpha = 0.5f),
                radius = 15f,
                center = Offset(eye.position.x, eye.position.y),
                style = Stroke(width = 2f)
            )
        }
        
        // Point sur le nez
        noseBase?.let { nose ->
            drawCircle(
                color = strokeColor,
                radius = 5f,
                center = Offset(nose.position.x, nose.position.y)
            )
        }
    }
}

