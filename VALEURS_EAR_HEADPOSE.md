# Valeurs EAR et Head Pose Estimation - Guide d'Interprétation

## 📊 EAR (Eye Aspect Ratio) - Valeurs et Interprétation

### 🔢 Calcul du EAR

**Formule utilisée dans le code :**
```kotlin
EAR = (leftEyeOpenProbability * 0.3 + rightEyeOpenProbability * 0.3) / 2.0
```

**Méthode :**
- Utilise les probabilités d'ouverture des yeux de ML Kit
- Probabilité 1.0 = œil complètement ouvert → EAR = 0.3
- Probabilité 0.0 = œil complètement fermé → EAR = 0.0
- Moyenne des deux yeux

---

### 📈 Plages de Valeurs EAR et Interprétation

| Valeur EAR | État des Yeux | Interprétation | Action |
|------------|---------------|----------------|--------|
| **0.25 - 0.30** | Yeux complètement ouverts | ✅ État normal, vigilance maximale | Aucune alerte |
| **0.20 - 0.25** | Yeux légèrement fermés | ⚠️ Fatigue légère possible | Surveillance |
| **0.15 - 0.20** | Yeux à moitié fermés | ⚠️ Fatigue modérée | Alerte légère |
| **< 0.20** | Yeux fermés | 🔴 Fatigue détectée | Alerte selon durée |
| **< 0.15** | Yeux très fermés | 🔴 Fatigue élevée | Alerte forte |

---

### ⏱️ Durées et Niveaux de Fatigue

**Seuils dans le code :**
```kotlin
EAR_THRESHOLD = 0.20              // Seuil pour yeux fermés
EAR_NORMAL_THRESHOLD = 0.25      // Seuil pour yeux ouverts normalement
EYES_CLOSED_DURATION_THRESHOLD_MS = 3000L  // 3 secondes
```

| Durée Yeux Fermés | EAR < 0.20 | Niveau de Fatigue | Alerte |
|-------------------|------------|-------------------|--------|
| **< 1 seconde** | Oui | 🟡 MODERATE | Vibration 200ms |
| **1 - 2 secondes** | Oui | 🟠 HIGH | Vibration 300ms |
| **> 3 secondes** | Oui | 🔴 CRITICAL | Alarme sonore + Vibration 500ms |

---

### 📝 Exemples de Valeurs EAR

**Yeux ouverts normalement :**
- `leftEyeOpenProbability = 0.95`
- `rightEyeOpenProbability = 0.98`
- **EAR calculé = (0.95 * 0.3 + 0.98 * 0.3) / 2 = 0.2895**
- ✅ **Interprétation :** Yeux bien ouverts, aucune fatigue

**Yeux légèrement fermés :**
- `leftEyeOpenProbability = 0.70`
- `rightEyeOpenProbability = 0.75`
- **EAR calculé = (0.70 * 0.3 + 0.75 * 0.3) / 2 = 0.2175**
- ⚠️ **Interprétation :** Yeux légèrement fermés, surveillance

**Yeux fermés :**
- `leftEyeOpenProbability = 0.10`
- `rightEyeOpenProbability = 0.15`
- **EAR calculé = (0.10 * 0.3 + 0.15 * 0.3) / 2 = 0.0375**
- 🔴 **Interprétation :** Yeux fermés, fatigue détectée

---

## 🎯 Head Pose Estimation - Valeurs et Interprétation

### 🔢 Angles d'Euler

**Trois angles mesurés :**
- **Pitch (eulerX)** : Inclinaison avant/arrière
- **Yaw (eulerY)** : Rotation gauche/droite
- **Roll (eulerZ)** : Inclinaison gauche/droite

**Source dans le code :**
```kotlin
headEulerX = face.headEulerAngleX  // Pitch
headEulerY = face.headEulerAngleY  // Yaw
headEulerZ = face.headEulerAngleZ  // Roll
```

---

### 📐 Valeurs Pitch (eulerX) - Inclinaison Avant/Arrière

| Angle | Interprétation | Signe de Fatigue |
|-------|----------------|------------------|
| **> 25°** | Tête penchée vers l'avant | ✅ **OUI** - Signe principal de fatigue |
| **20° - 25°** | Tête légèrement penchée | ⚠️ Surveillance |
| **-20° à 20°** | Tête droite | ❌ Non |
| **< -20°** | Tête penchée vers l'arrière | ❌ Non (regard vers le haut) |

**Seuil dans le code :**
```kotlin
pitchThreshold = 25.0f  // Degrés
headFallingForward = pitch > pitchThreshold
```

**Exemples :**
- `eulerX = 30°` → ✅ **Fatigue détectée** (tête qui tombe)
- `eulerX = 15°` → ❌ Pas de fatigue (tête droite)
- `eulerX = -10°` → ❌ Pas de fatigue (regard vers le haut)

---

### 📐 Valeurs Roll (eulerZ) - Inclinaison Latérale

| Angle | Interprétation | Signe de Fatigue |
|-------|----------------|------------------|
| **> 25°** | Tête inclinée à droite | ✅ **OUI** - Fatigue possible |
| **15° - 25°** | Légère inclinaison | ⚠️ Surveillance |
| **-15° à 15°** | Tête droite | ❌ Non |
| **< -25°** | Tête inclinée à gauche | ✅ **OUI** - Fatigue possible |

**Seuil dans le code :**
```kotlin
rollThreshold = 25.0f  // Degrés
headTiltedSideways = abs(roll) > rollThreshold
```

**Exemples :**
- `eulerZ = 30°` → ✅ **Fatigue détectée** (inclinaison droite)
- `eulerZ = -28°` → ✅ **Fatigue détectée** (inclinaison gauche)
- `eulerZ = 10°` → ❌ Pas de fatigue (légère inclinaison normale)

---

### 📐 Valeurs Yaw (eulerY) - Rotation Gauche/Droite

| Angle | Interprétation | Signe de Fatigue |
|-------|----------------|------------------|
| **> 45°** | Tête tournée à droite | ❌ **IGNORÉ** - Rotation normale |
| **-45° à 45°** | Rotation normale | ❌ Non |
| **< -45°** | Tête tournée à gauche | ❌ **IGNORÉ** - Rotation normale |

**Important :** Le Yaw est **IGNORÉ** dans la détection de fatigue car tourner la tête est un mouvement normal.

**Seuil dans le code (non utilisé pour fatigue) :**
```kotlin
yawThreshold = 45.0f  // Degrés (seulement pour description)
// Rotation (yaw) n'est PAS un signe de fatigue
```

**Exemples :**
- `eulerY = 50°` → ❌ **Pas de fatigue** (rotation normale)
- `eulerY = -40°` → ❌ **Pas de fatigue** (rotation normale)

---

### 🔍 Détection Combinée de Fatigue

**Logique dans le code :**
```kotlin
// La fatigue basée sur la pose de la tête nécessite :
// - Les yeux sont fermés ET
// - L'EAR est faible (< 0.20) ET
// - (Pitch > 25° OU Roll > 25°)
```

**Conditions pour déclencher fatigue modérée :**
1. ✅ `headTilted == true` (Pitch > 25° OU Roll > 25°)
2. ✅ `eyesClosed == true` (EAR < 0.20)
3. ✅ `ear < EAR_THRESHOLD` (0.20)

**Exemples de combinaisons :**

| Pitch | Roll | Yaw | Yeux | EAR | Fatigue Détectée ? |
|-------|------|-----|------|-----|-------------------|
| 30° | 10° | 20° | Fermés | 0.15 | ✅ **OUI** (Pitch + Yeux fermés) |
| 15° | 30° | -15° | Fermés | 0.18 | ✅ **OUI** (Roll + Yeux fermés) |
| 30° | 10° | 20° | Ouverts | 0.28 | ❌ **NON** (Yeux ouverts) |
| 10° | 5° | 50° | Fermés | 0.15 | ❌ **NON** (Pas d'inclinaison significative) |

---

## 📋 Description Textuelle de la Pose

**Fonction `getHeadPoseDescription()` dans le code :**

### Pitch (eulerX)
- `pitch > 20°` → "Tête penchée vers l'avant"
- `pitch < -20°` → "Tête penchée vers l'arrière"
- `-20° ≤ pitch ≤ 20°` → (pas de description)

### Roll (eulerZ)
- `roll > 15°` → "Tête inclinée à droite"
- `roll < -15°` → "Tête inclinée à gauche"
- `-15° ≤ roll ≤ 15°` → (pas de description)

### Yaw (eulerY)
- `yaw > 30°` → "Tête tournée à droite"
- `yaw < -30°` → "Tête tournée à gauche"
- `-30° ≤ yaw ≤ 30°` → (pas de description)

**Exemples de descriptions :**
- `"Tête droite"` → Tous les angles dans les plages normales
- `"Tête penchée vers l'avant, Tête inclinée à droite"` → Pitch > 20° et Roll > 15°
- `"Tête tournée à gauche"` → Yaw < -30° (mais pas utilisé pour fatigue)

---

## 🎯 Résumé des Seuils Critiques

### EAR (Eye Aspect Ratio)
- **≥ 0.25** : Yeux ouverts normalement → ✅ Aucune fatigue
- **0.20 - 0.25** : Yeux légèrement fermés → ⚠️ Surveillance
- **< 0.20** : Yeux fermés → 🔴 Fatigue détectée
- **< 0.20 pendant 3s** : Endormissement → 🚨 Alerte d'urgence

### Head Pose
- **Pitch > 25°** : Tête qui tombe → ✅ Signe de fatigue
- **Roll > 25°** : Tête inclinée → ✅ Signe de fatigue
- **Yaw** : Ignoré (rotation normale)

### Combinaison
- **Fatigue modérée** : Tête inclinée + Yeux fermés + EAR < 0.20

---

## 💡 Conseils d'Interprétation

1. **EAR est le critère principal** : Plus fiable que la pose de la tête
2. **Head Pose seul ne suffit pas** : Doit être combiné avec yeux fermés
3. **Durée importante** : Yeux fermés 0.5s = clignement normal, 3s = endormissement
4. **Yaw ignoré** : Tourner la tête est normal, pas un signe de fatigue
5. **Pitch positif** : Le plus important (tête qui tombe = fatigue)

---

## 🔧 Code de Référence

**Fichiers concernés :**
- `EyeAspectRatio.kt` : Calcul et interprétation EAR
- `HeadPoseEstimator.kt` : Calcul et interprétation Head Pose
- `FatigueDetector.kt` : Combinaison des critères et détection finale
- `MainActivity.kt` : Affichage des valeurs en temps réel

