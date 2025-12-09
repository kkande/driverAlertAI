# DriverAlert AI - Résumé Présentation (Points Clés)

## SLIDE 1 : Titre
- **DriverAlert AI** - Détection de Somnolence par Vision Artificielle
- Votre nom, date

## SLIDE 2 : Problématique
- Fatigue = cause majeure d'accidents
- Systèmes existants coûteux (Tesla, BMW)
- Besoin d'une solution accessible

## SLIDE 3 : Solution
- Smartphone + caméra frontale
- ML Kit embarqué
- Offline, économique, universel

## SLIDE 4 : Objectifs
- Détecter visage/yeux (MLKit)
- Calculer EAR
- Head Pose Estimation
- Alertes sonores/vibratoires
- 24-30 FPS

## SLIDE 5 : Architecture
**CameraX → MLKit → Landmarks → EAR & Head Pose → Détection → Alerte**

## SLIDE 6 : Technologies
- Android (Kotlin) + Compose
- CameraX + ML Kit
- EAR Algorithm
- Coroutines

## SLIDE 7 : EAR (Eye Aspect Ratio)
- EAR < 0.20 = Yeux fermés
- EAR < 0.20 pendant 3s = Endormissement
- Formule : `EAR = (|p2-p6| + |p3-p5|) / (2 * |p1-p4|)`

## SLIDE 8 : Head Pose
- Pitch > 25° (tête vers l'avant) = Fatigue
- Roll > 25° (inclinaison) = Fatigue
- Yaw ignoré (rotation normale)

## SLIDE 9 : Détection Multi-Critères
- Yeux fermés 3s → CRITIQUE
- Yeux fermés 2s → ÉLEVÉE
- Yeux fermés 1s → MODÉRÉE
- Bâillement → ÉLEVÉE
- Clignement lent → MODÉRÉE

## SLIDE 10 : Alertes
- Vibration (100-500ms selon niveau)
- Alarme sonore (3s d'endormissement)
- Affichage visuel avec code couleur

## SLIDE 11 : Interface
- Compte à rebours 5s
- Vue caméra temps réel
- Overlay : EAR, pose, niveau fatigue

## SLIDE 12 : Performance
- 24-30 FPS
- CPU < 50%
- Offline
- Détection 85-93% fiable

## SLIDE 13 : Démo Live
- Test yeux ouverts → Aucune fatigue
- Test fermeture 3s → Alerte urgence
- Test bâillement → Fatigue élevée

## SLIDE 14 : Limites & Futur
- Limites : Luminosité, lunettes
- Futur : MediaPipe, mode nuit, stats

## SLIDE 15 : Comparaison
- DriverAlert : Gratuit, accessible, portable
- Solutions industrielles : Coûteux, voitures premium

## SLIDE 16 : Impact
- Conducteurs professionnels
- Réduction accidents
- Accessible à tous

## SLIDE 17 : Conclusion
- Application fonctionnelle
- Vision IA embarquée
- Smartphone = outil sécurité routière

## SLIDE 18 : Q&A
- Merci
- Contact
- Démo disponible

---

## Vidéos Essentielles à Préparer

1. **Démo complète** (2-3 min) : Application en action
2. **Test de fatigue** (1 min) : Simulation yeux fermés → alerte
3. **Interface** (30s) : Navigation dans l'app
4. **Architecture** (30s) : Animation du flux de données

