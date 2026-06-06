# MapTracker — Application Android de géolocalisation en temps réel

Projet Android développé dans le cadre d'un TP sur l'API Google Maps.

Salma AIT ZIDAN
## Fonctionnalités

- Affichage d'une carte Google Maps en plein écran
- Demande de permission de localisation au runtime (Android 6+)
- Écoute simultanée du provider **Réseau** et du provider **GPS**
- Marker unique qui se déplace à chaque nouvelle position (pas de pollution visuelle)
- Animation fluide de la caméra vers la position courante (zoom niveau 16)
- Dialogue d'alerte si le GPS est désactivé, avec redirection vers les paramètres
- Affichage de la dernière position connue au démarrage
- Nettoyage propre des listeners dans `onDestroy()`

---

## Structure du projet

```
MapTracker/
├── app/
│   ├── src/main/
│   │   ├── java/com/etudiant/maptracker/
│   │   │   └── MapsActivity.java       ← Logique principale
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_maps.xml   ← Fragment carte
│   │   │   └── values/
│   │   │       ├── google_maps_api.xml ← Clé API (à remplir)
│   │   │       ├── strings.xml
│   │   │       ├── colors.xml
│   │   │       └── themes.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## Installation et mise en route

### 1. Cloner le dépôt

```bash
git clone https://github.com/salmaaitzidan/Lab11.git
cd Lab11.git
```

### 2. Obtenir une clé API Google Maps

1. Aller sur [Google Cloud Console](https://console.cloud.google.com/)
2. Créer ou sélectionner un projet
3. **APIs & Services** → Bibliothèque → activer **Maps SDK for Android**
4. **APIs & Services** → Identifiants → **Créer des identifiants** → Clé API
5. (Recommandé) Restreindre la clé à l'application Android avec le package : `com.etudiant.maptracker`

### 3. Configurer la clé

Ouvrir `app/src/main/res/values/google_maps_api.xml` et remplacer :

```xml
<string name="google_maps_key" ...>VOTRE_CLE_API_ICI</string>
```

par votre vraie clé.

> ⚠️ **Ne jamais committer la clé API dans un dépôt public.**
> Utiliser plutôt un fichier `local.properties` ou des variables d'environnement CI/CD.

### 4. Ouvrir dans Android Studio

- Android Studio → **Open** → sélectionner le dossier `MapTracker`
- Laisser Gradle synchroniser
- Connecter un appareil ou lancer un émulateur avec la localisation activée
- **Run**

---

## Personnalisation rapide

Toutes les constantes sont regroupées en haut de `MapsActivity.java` :

| Constante | Valeur par défaut | Description |
|---|---|---|
| `ZOOM_LEVEL` | `16.0f` | Niveau de zoom (1–21). 15 = quartier, 18 = rue |
| `MIN_TIME_MS` | `2000` | Délai minimum entre deux mises à jour (ms) |
| `MIN_DISTANCE_M` | `0` | Distance minimum entre deux mises à jour (m) |
| `MAP_TITLE` | `"Ma Position"` | Texte du titre du marker |

Pour changer le **type de carte**, modifier dans `onMapReady()` :

```java
mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);    // Carte routière
mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // Vue satellite
mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);    // Satellite + routes
mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);   // Relief
```

Pour changer la **couleur du marker**, modifier dans `mettreAJourCarte()` :

```java
// Quelques teintes disponibles :
BitmapDescriptorFactory.HUE_AZURE    // Bleu clair (défaut)
BitmapDescriptorFactory.HUE_RED      // Rouge
BitmapDescriptorFactory.HUE_GREEN    // Vert
BitmapDescriptorFactory.HUE_ORANGE   // Orange
BitmapDescriptorFactory.HUE_VIOLET   // Violet
```

---

## Dépendances principales

```gradle
implementation 'com.google.android.gms:play-services-maps:18.2.0'
implementation 'com.google.android.gms:play-services-location:21.0.1'
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.11.0'
```

---

## Compatibilité

| Élément | Valeur |
|---|---|
| `minSdk` | 23 (Android 6.0 Marshmallow) |
| `targetSdk` | 34 (Android 14) |
| `compileSdk` | 34 |
| Langage | Java |

---

## Problèmes courants

**Carte blanche / "For development purposes only"**
→ Clé API absente, incorrecte, ou Maps SDK non activé dans la console Google.

**Aucun marker n'apparaît**
→ Permission refusée, ou provider réseau/GPS désactivé sur le téléphone.

**La position ne se met pas à jour**
→ `MIN_DISTANCE_M = 50` requiert de bouger physiquement. Mettre à `0` pour tester sur émulateur.

**Le dialogue GPS ne s'affiche pas**
→ `onProviderDisabled` ne se déclenche que lors d'un changement d'état. Vérifier l'état du GPS au démarrage est géré automatiquement par `demarrerTracking()`.

---
