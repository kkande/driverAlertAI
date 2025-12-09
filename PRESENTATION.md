# DriverAlert AI - Présentation Projet

## Structure de la Présentation (15-20 slides)

---

### SLIDE 1 : Page de Titre
**DriverAlert AI**
*Détection de Somnolence en Temps Réel par Vision Artificielle*

- Votre nom / Équipe
- Date de présentation
- Logo / Image du projet

**Vidéo suggérée :** Démo rapide (10-15 secondes) montrant l'application en action

---

### SLIDE 2 : Problématique
**Le Défi de la Fatigue au Volant**

- **Statistiques clés :**
  - La fatigue est l'une des principales causes d'accidents routiers
  - Conducteurs professionnels (taxis, VTC, bus, camions) particulièrement exposés
  - Longues heures de conduite → vigilance diminuée

- **Problème actuel :**
  - Systèmes embarqués coûteux (Tesla, BMW, Mercedes)
  - Inaccessibles à la majorité des conducteurs
  - Besoin d'une solution universelle et économique

**Vidéo suggérée :** Statistiques animées ou images d'accidents liés à la fatigue

---

### SLIDE 3 : Solution Proposée
**DriverAlert AI : Une Solution Mobile et Accessible**

- **Concept :**
  - Utiliser la caméra frontale du smartphone
  - Vision artificielle embarquée (ML Kit)
  - Fonctionnement offline
  - Accessible à tous

- **Avantages :**
  - ✅ Économique (pas de matériel additionnel)
  - ✅ Universel (fonctionne sur tout smartphone Android)
  - ✅ Portable (utilisable partout)
  - ✅ Temps réel (détection instantanée)

**Vidéo suggérée :** Montage montrant l'application sur différents smartphones

---

### SLIDE 4 : Objectifs du Projet
**Objectifs Généraux et Spécifiques**

**Objectif Général :**
Concevoir une application mobile intelligente capable d'analyser le visage du conducteur grâce à la vision artificielle et de détecter des signes de fatigue en temps réel.

**Objectifs Spécifiques :**
1. ✅ Détecter le visage et les yeux en temps réel (MLKit Face Detection)
2. ✅ Calculer le EAR (Eye Aspect Ratio) pour mesurer l'ouverture des yeux
3. ✅ Déterminer si les yeux restent fermés au-delà d'un seuil critique (3 secondes)
4. ✅ Estimer l'orientation de la tête (Head Pose Estimation)
5. ✅ Détecter bâillements, clignements lents, baisse de vigilance
6. ✅ Déclencher des alertes sonores et vibratoires
7. ✅ Offrir une solution offline, fonctionnelle en tout temps
8. ✅ Optimiser les performances (24-30 FPS)

**Vidéo suggérée :** Animation montrant chaque objectif avec des icônes

---

### SLIDE 5 : Architecture du Système
**Flux de Traitement**

```
CameraX → MLKit Face Detection → Landmarks → 
EAR & Head Pose → Détection Fatigue → Alerte
```

**Composants :**
- **CameraX** : Capture vidéo en temps réel (caméra frontale)
- **ML Kit Face Detection** : Détection du visage et extraction des landmarks
- **EAR Calculator** : Calcul du ratio d'ouverture des yeux
- **Head Pose Estimator** : Estimation de l'orientation de la tête
- **Fatigue Detector** : Analyse combinée et détection de fatigue
- **Alert System** : Alertes sonores et vibratoires

**Vidéo suggérée :** Diagramme animé montrant le flux de données

---

### SLIDE 6 : Technologies Utilisées
**Stack Technique**

**Plateforme :**
- Android (Kotlin)
- Jetpack Compose (UI moderne)

**Vision Artificielle :**
- CameraX (capture vidéo)
- ML Kit Face Detection (détection de visage)
- Landmarks extraction (points du visage)

**Détection de Fatigue :**
- EAR (Eye Aspect Ratio) algorithm
- Head Pose Estimation (angles d'Euler)
- Machine Learning embarqué

**Performance :**
- Coroutines (traitement asynchrone)
- Optimisation temps réel

**Vidéo suggérée :** Logo des technologies avec animations

---

### SLIDE 7 : EAR (Eye Aspect Ratio)
**Méthode de Détection Principale**

**Principe :**
- Le EAR diminue lorsque l'œil se ferme
- Mesure précise de l'ouverture des yeux

**Seuils de Détection :**
- EAR < 0.20 → Yeux fermés
- EAR > 0.25 → Yeux ouverts normalement
- EAR < 0.20 pendant > 3 secondes → **Endormissement détecté**

**Formule :**
```
EAR = (|p2-p6| + |p3-p5|) / (2 * |p1-p4|)
```

**Vidéo suggérée :** Animation montrant le calcul du EAR avec des points sur les yeux

---

### SLIDE 8 : Head Pose Estimation
**Détection de l'Inclinaison de la Tête**

**Angles Mesurés :**
- **Pitch** (X) : Inclinaison avant/arrière
  - Tête qui tombe vers l'avant = signe de fatigue
- **Roll** (Z) : Inclinaison gauche/droite
- **Yaw** (Y) : Rotation gauche/droite

**Seuils :**
- Pitch > 25° vers l'avant → Fatigue
- Roll > 25° → Fatigue
- Yaw ignoré (rotation normale)

**Vidéo suggérée :** Animation 3D montrant les angles de rotation de la tête

---

### SLIDE 9 : Détection Multi-Critères
**Système de Détection Intelligent**

**Signes de Fatigue Détectés :**
1. **Yeux fermés** (> 3 secondes) → CRITIQUE
2. **Yeux fermés** (> 2 secondes) → ÉLEVÉE
3. **Yeux fermés** (> 1 seconde) → MODÉRÉE
4. **Bâillement** (bouche ouverte + yeux fermés) → ÉLEVÉE
5. **Clignement lent** (> 500ms) → MODÉRÉE
6. **Tête inclinée** (avec yeux fermés) → MODÉRÉE

**Niveaux de Fatigue :**
- 🟢 NONE : Aucune fatigue
- 🟡 LOW : Fatigue légère
- 🟠 MODERATE : Fatigue modérée
- 🔴 HIGH : Fatigue élevée
- ⚠️ CRITICAL : Fatigue critique (alerte d'urgence)

**Vidéo suggérée :** Démo montrant chaque niveau de fatigue avec l'interface

---

### SLIDE 10 : Système d'Alertes
**Alertes Multi-Modales**

**Types d'Alertes :**
1. **Vibration** :
   - Fatigue légère : 100ms
   - Fatigue modérée : 200ms
   - Fatigue élevée : 300ms
   - Fatigue critique : 500ms

2. **Alerte Sonore d'Urgence** :
   - Déclenchée après 3 secondes d'endormissement
   - Alarme système à volume maximal
   - Arrêt automatique après 10 secondes

3. **Affichage Visuel** :
   - Overlay avec informations en temps réel
   - Code couleur selon le niveau de fatigue
   - Message d'alerte pour fatigue critique

**Vidéo suggérée :** Démo complète montrant les alertes en action

---

### SLIDE 11 : Interface Utilisateur
**Design Moderne et Intuitif**

**Écran de Démarrage :**
- Compte à rebours (5 secondes)
- Message explicatif sur la détection
- Démarrage automatique de la caméra

**Écran Principal :**
- Vue caméra frontale en temps réel
- Overlay d'informations :
  - Statut de détection de visage
  - Valeur EAR en temps réel
  - Description de la pose de la tête
  - Niveau de fatigue avec code couleur
  - Message d'alerte si nécessaire

**Vidéo suggérée :** Screenshots ou vidéo de l'interface en action

---

### SLIDE 12 : Performance et Optimisation
**Résultats Techniques**

**Performance Atteinte :**
- ✅ 24-30 FPS (traitement en temps réel)
- ✅ CPU < 50% (optimisation efficace)
- ✅ Faible consommation énergétique
- ✅ Fonctionnement offline (pas d'internet requis)
- ✅ Détection fiable 85-93%

**Optimisations :**
- Traitement asynchrone avec Coroutines
- Stratégie de backpressure (KEEP_ONLY_LATEST)
- Mode performance rapide de ML Kit
- Traitement sur thread séparé

**Vidéo suggérée :** Graphiques de performance animés

---

### SLIDE 13 : Démonstration Live
**Démo en Direct**

**Scénarios de Test :**
1. **Yeux ouverts normalement** → Aucune fatigue
2. **Fermeture des yeux 1 seconde** → Fatigue modérée
3. **Fermeture des yeux 2 secondes** → Fatigue élevée
4. **Fermeture des yeux 3+ secondes** → Alerte d'urgence
5. **Bâillement** → Fatigue élevée
6. **Tête inclinée avec yeux fermés** → Fatigue modérée

**Vidéo suggérée :** Enregistrement de la démo en direct ou vidéo pré-enregistrée

---

### SLIDE 14 : Limites et Améliorations Futures
**Perspectives d'Évolution**

**Limites Actuelles :**
- ⚠️ Faible luminosité (détection moins précise)
- ⚠️ Port de lunettes ou masques
- ⚠️ Distance variable entre visage et caméra
- ⚠️ Téléphone mal positionné

**Améliorations Futures :**
- 🔮 Intégration MediaPipe FaceMesh (478 landmarks)
- 🔮 Détection améliorée en faible luminosité
- 🔮 Support des lunettes et masques
- 🔮 Calibration automatique de la distance
- 🔮 Mode nuit avec infrarouge
- 🔮 Statistiques de conduite
- 🔮 Export des données de fatigue

**Vidéo suggérée :** Mockup des futures fonctionnalités

---

### SLIDE 15 : Comparaison avec Solutions Existantes
**Avantages Concurrentiels**

| Caractéristique | DriverAlert AI | Solutions Industrielles |
|----------------|----------------|------------------------|
| **Coût** | Gratuit | Très coûteux |
| **Accessibilité** | Tous smartphones | Voitures premium |
| **Installation** | Aucune | Intégration complexe |
| **Portabilité** | Oui | Non |
| **Offline** | Oui | Variable |
| **Mise à jour** | Facile | Difficile |

**Vidéo suggérée :** Tableau comparatif animé

---

### SLIDE 16 : Impact et Applications
**Cas d'Usage Réels**

**Applications :**
- 🚗 Conducteurs professionnels (taxis, VTC)
- 🚌 Chauffeurs de bus
- 🚛 Conducteurs de camions
- 🚕 Services de transport
- 👨‍👩‍👧‍👦 Conducteurs particuliers

**Impact Potentiel :**
- Réduction des accidents liés à la fatigue
- Amélioration de la sécurité routière
- Accessibilité pour tous les budgets
- Sensibilisation à la fatigue au volant

**Vidéo suggérée :** Témoignages ou statistiques d'impact

---

### SLIDE 17 : Conclusion
**Résumé du Projet**

**Réalisations :**
- ✅ Application fonctionnelle de détection de fatigue
- ✅ Vision artificielle embarquée (ML Kit)
- ✅ Détection multi-critères (EAR + Head Pose)
- ✅ Système d'alertes efficace
- ✅ Interface moderne et intuitive
- ✅ Performance optimisée (24-30 FPS)

**Message Clé :**
Un simple smartphone peut devenir un outil de sécurité routière puissant et accessible grâce à la vision artificielle embarquée.

**Vidéo suggérée :** Montage récapitulatif du projet

---

### SLIDE 18 : Questions & Réponses
**Merci pour votre attention !**

- Contact / Email
- Repository GitHub (si disponible)
- Démo disponible après la présentation

**Vidéo suggérée :** Slide de fin avec coordonnées

---

## Suggestions de Vidéos à Intégrer

### Vidéo 1 : Démo Complète (2-3 minutes)
- Compte à rebours
- Détection normale (yeux ouverts)
- Simulation de fatigue (fermeture des yeux)
- Alerte d'urgence
- Interface et informations affichées

### Vidéo 2 : Comparaison Avant/Après
- Sans l'application (conduite normale)
- Avec l'application (détection active)
- Montrer les alertes

### Vidéo 3 : Architecture Technique (Animation)
- Flux de données
- Composants du système
- Traitement en temps réel

### Vidéo 4 : Statistiques et Impact
- Graphiques animés
- Données sur la fatigue au volant
- Impact potentiel

---

## Conseils pour la Présentation

1. **Timing :** 15-20 minutes de présentation + 5-10 minutes de questions
2. **Vidéos :** Intégrer des vidéos courtes (30 secondes à 2 minutes max)
3. **Démo Live :** Prévoir une démo en direct si possible
4. **Backup :** Avoir une vidéo de backup au cas où la démo ne fonctionne pas
5. **Interactivité :** Poser des questions au public
6. **Visuels :** Utiliser des graphiques, diagrammes, et captures d'écran

---

## Outils Recommandés

- **PowerPoint / Google Slides** : Pour créer les slides
- **OBS Studio** : Pour enregistrer les vidéos de démo
- **Canva** : Pour créer des visuels attractifs
- **ScreenToGif** : Pour créer des GIFs animés
- **Adobe Premiere / DaVinci Resolve** : Pour monter les vidéos

