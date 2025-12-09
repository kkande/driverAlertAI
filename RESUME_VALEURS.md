# 📊 Résumé Rapide - Valeurs EAR et Head Pose

## 👁️ EAR (Eye Aspect Ratio)

### Valeurs Clés
```
EAR ≥ 0.25  → Yeux ouverts normalement ✅
EAR 0.20-0.25 → Yeux légèrement fermés ⚠️
EAR < 0.20   → Yeux fermés 🔴
```

### Durées et Alertes
```
Yeux fermés < 1s    → MODERATE (Vibration 200ms)
Yeux fermés 1-2s    → HIGH (Vibration 300ms)
Yeux fermés > 3s    → CRITICAL (Alarme sonore + Vibration 500ms)
```

### Calcul
```kotlin
EAR = (leftEyeOpenProb * 0.3 + rightEyeOpenProb * 0.3) / 2.0
```

---

## 🎯 Head Pose (Angles d'Euler)

### Pitch (eulerX) - Avant/Arrière
```
> 25°  → Tête qui tombe → Fatigue ✅
20-25° → Légère inclinaison → Surveillance ⚠️
-20 à 20° → Tête droite → Normal ✅
```

### Roll (eulerZ) - Gauche/Droite
```
> 25°  → Inclinaison droite → Fatigue ✅
< -25° → Inclinaison gauche → Fatigue ✅
-25 à 25° → Tête droite → Normal ✅
```

### Yaw (eulerY) - Rotation
```
❌ IGNORÉ - Rotation normale, pas un signe de fatigue
```

### Détection Combinée
```
Fatigue = (Pitch > 25° OU Roll > 25°) 
          ET Yeux fermés (EAR < 0.20)
```

---

## 📋 Tableau de Décision Rapide

| EAR | Pitch | Roll | Yeux | Fatigue |
|-----|-------|------|------|---------|
| 0.28 | 10° | 5° | Ouverts | ❌ NON |
| 0.15 | 30° | 10° | Fermés | ✅ OUI (Modérée) |
| 0.18 | 15° | 30° | Fermés | ✅ OUI (Modérée) |
| 0.12 | 5° | 5° | Fermés > 3s | ✅ OUI (Critique) |

---

## 🔍 Où Voir les Valeurs dans le Code

**EAR :**
- Calcul : `EyeAspectRatio.calculateEAR()`
- Seuil : `EAR_THRESHOLD = 0.20`
- Affichage : `earValue` dans `MainActivity`

**Head Pose :**
- Angles : `face.headEulerAngleX/Y/Z`
- Détection : `HeadPoseEstimator.isHeadTilted()`
- Description : `HeadPoseEstimator.getHeadPoseDescription()`
- Affichage : `headPoseDescription` dans `MainActivity`

