package com.example.driveralert

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.camera.view.PreviewView
import androidx.camera.view.LifecycleCameraController
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.driveralert.ui.theme.DriverAlertTheme
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DriverAlertTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CountdownCameraScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CountdownCameraScreen(modifier: Modifier = Modifier, countdownSeconds: Int = 5) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var secondsLeft by rememberSaveable { mutableStateOf(countdownSeconds) }
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var hasCameraPermission by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberCameraPermissionLauncher(
        onResult = { granted ->
            hasCameraPermission = granted
            showCamera = granted
        }
    )

    LaunchedEffect(countdownSeconds) {
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft--
        }
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            showCamera = true
        }
    }

    if (!showCamera) {
        CountdownDisplay(
            secondsLeft = secondsLeft,
            modifier = modifier.fillMaxSize()
        )
    } else {
        if (hasCameraPermission) {
            CameraPreview(
                modifier = modifier.fillMaxSize(),
                lifecycleOwner = lifecycleOwner
            )
        } else {
            PermissionRationale(
                onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CountdownDisplay(secondsLeft: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Le travail va commencer",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Text(
            text = "La caméra va se déclencher pour la vision artificielle afin de détecter la somnolence, l'endormissement ou le bâillement",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp)
        )
        Text(
            text = secondsLeft.coerceAtLeast(0).toString(),
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 64.sp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Autorisez l'accès à la caméra pour démarrer.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRequest, modifier = Modifier.padding(top = 16.dp)) {
            Text("Autoriser la caméra")
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner
) {
    val context = LocalContext.current
    
    var fatigueLevel by rememberSaveable { mutableStateOf(FatigueLevel.NONE) }
    var earValue by rememberSaveable { mutableStateOf(0.0) }
    var headPoseDescription by rememberSaveable { mutableStateOf("") }
    var faceDetected by rememberSaveable { mutableStateOf(false) }
    var detectedFace by rememberSaveable { mutableStateOf<com.google.mlkit.vision.face.Face?>(null) }
    
    val fatigueDetector = remember(context) {
        FatigueDetector(context) { level ->
            fatigueLevel = level
        }
    }
    
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    DisposableEffect(lifecycleOwner, previewView) {
        val executor = Executors.newSingleThreadExecutor()
        
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = com.google.mlkit.vision.common.InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        
                        val faceDetector = com.google.mlkit.vision.face.FaceDetection.getClient(
                            com.google.mlkit.vision.face.FaceDetectorOptions.Builder()
                                .setPerformanceMode(com.google.mlkit.vision.face.FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                                .setLandmarkMode(com.google.mlkit.vision.face.FaceDetectorOptions.LANDMARK_MODE_ALL)
                                .setClassificationMode(com.google.mlkit.vision.face.FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                                .setMinFaceSize(0.1f)
                                .enableTracking()
                                .build()
                        )
                        
                        faceDetector.process(image)
                            .addOnSuccessListener { faces ->
                                if (faces.isNotEmpty()) {
                                    val face = faces[0]
                                    detectedFace = face
                                    val result = FaceDetectionResult(
                                        face = face,
                                        leftEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_EYE),
                                        rightEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_EYE),
                                        leftCheek = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_CHEEK),
                                        rightCheek = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_CHEEK),
                                        noseBase = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.NOSE_BASE),
                                        leftMouth = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.MOUTH_LEFT),
                                        rightMouth = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.MOUTH_RIGHT),
                                        bottomMouth = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.MOUTH_BOTTOM),
                                        headEulerX = face.headEulerAngleX,
                                        headEulerY = face.headEulerAngleY,
                                        headEulerZ = face.headEulerAngleZ,
                                        leftEyeOpenProbability = face.leftEyeOpenProbability,
                                        rightEyeOpenProbability = face.rightEyeOpenProbability,
                                        smilingProbability = face.smilingProbability
                                    )
                                    
                                    faceDetected = result.hasValidLandmarks()
                                    if (faceDetected) {
                                        earValue = EyeAspectRatio.calculateEAR(
                                            result.leftEye,
                                            result.rightEye,
                                            result.leftEyeOpenProbability,
                                            result.rightEyeOpenProbability
                                        )
                                        
                                        headPoseDescription = HeadPoseEstimator.getHeadPoseDescription(
                                            result.headEulerX,
                                            result.headEulerY,
                                            result.headEulerZ
                                        )
                                        
                                        fatigueDetector.processFaceDetection(result)
                                    } else {
                                        fatigueLevel = FatigueLevel.NONE
                                        earValue = 0.0
                                        headPoseDescription = "Aucun visage détecté"
                                    }
                                } else {
                                    faceDetected = false
                                    detectedFace = null
                                    fatigueLevel = FatigueLevel.NONE
                                    earValue = 0.0
                                    headPoseDescription = "Aucun visage détecté"
                                }
                            }
                            .addOnFailureListener {
                                faceDetected = false
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }
            }
        
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, androidx.core.content.ContextCompat.getMainExecutor(context))
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            cameraProviderFuture.addListener({
                cameraProviderFuture.get().unbindAll()
            }, androidx.core.content.ContextCompat.getMainExecutor(context))
            executor.shutdown()
            fatigueDetector.release()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView }
        )
        
        // Overlay avec les informations de détection
        DetectionOverlay(
            fatigueLevel = fatigueLevel,
            earValue = earValue,
            headPoseDescription = headPoseDescription,
            faceDetected = faceDetected,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
    }
}

@Composable
private fun DetectionOverlay(
    fatigueLevel: FatigueLevel,
    earValue: Double,
    headPoseDescription: String,
    faceDetected: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = if (faceDetected) "Visage détecté" else "Recherche de visage...",
                style = MaterialTheme.typography.titleSmall,
                color = if (faceDetected) Color.Green else Color.Yellow
            )
            
            if (faceDetected) {
                Text(
                    text = "EAR: ${String.format("%.3f", earValue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Text(
                    text = "Pose: $headPoseDescription",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                val (fatigueText, fatigueColor) = when (fatigueLevel) {
                    FatigueLevel.NONE -> "Aucune fatigue" to Color.Green
                    FatigueLevel.LOW -> "Fatigue légère" to Color.Yellow
                    FatigueLevel.MODERATE -> "Fatigue modérée" to Color(0xFFFFA500) // Orange
                    FatigueLevel.HIGH -> "Fatigue élevée" to Color(0xFFFF6600) // Orange foncé
                    FatigueLevel.CRITICAL -> "⚠️ FATIGUE CRITIQUE" to Color.Red
                }
                
                Text(
                    text = fatigueText,
                    style = MaterialTheme.typography.titleMedium,
                    color = fatigueColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                if (fatigueLevel == FatigueLevel.CRITICAL) {
                    Text(
                        text = "Veuillez vous arrêter quelques minutes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberCameraPermissionLauncher(onResult: (Boolean) -> Unit) =
    androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )

@Composable
private fun rememberCameraController(
    context: android.content.Context,
    lifecycleOwner: LifecycleOwner
): LifecycleCameraController {
    return androidx.compose.runtime.remember(context, lifecycleOwner) {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            bindToLifecycle(lifecycleOwner)
        }
    }
}
