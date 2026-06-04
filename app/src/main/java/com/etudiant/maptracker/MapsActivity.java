package com.etudiant.maptracker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    // === CONSTANTES CONFIGURABLES ===
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final float ZOOM_LEVEL = 16.0f;        // Niveau de zoom (1-21)
    private static final long MIN_TIME_MS = 2000;          // Délai min entre updates (ms)
    private static final float MIN_DISTANCE_M = 0;         // Distance min entre updates (m)
    private static final String MAP_TITLE = "Ma Position"; // Titre du marker

    // === VARIABLES D'ÉTAT ===
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Marker currentMarker; // Marker unique qui se déplace
    private boolean trackingStarted = false;

    // === CYCLE DE VIE ===
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialiser le LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Charger le fragment de carte
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // === CALLBACK CARTE PRÊTE ===
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Style de carte : normal, satellite, terrain, hybrid
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Activer les contrôles de zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        Toast.makeText(this, "Carte prête ✓", Toast.LENGTH_SHORT).show();

        // Vérifier permission et démarrer le tracking
        demarrerTracking();
    }

    // === TRACKING DE POSITION ===
    private void demarrerTracking() {
        boolean permissionOk = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!permissionOk) {
            // Demander la permission si pas encore accordée
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE
            );
            return;
        }

        if (trackingStarted) return; // Eviter double inscription
        trackingStarted = true;

        // --- Provider NETWORK ---
        boolean networkDispo = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (networkDispo) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_MS,
                    MIN_DISTANCE_M,
                    locationListener
            );
        }

        // --- Provider GPS ---
        boolean gpsDispo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gpsDispo) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_MS,
                    MIN_DISTANCE_M,
                    locationListener
            );
        }

        // Si aucun provider actif
        if (!networkDispo && !gpsDispo) {
            afficherDialogueGps();
        }

        // Afficher la dernière position connue immédiatement
        Location dernierePos = null;
        if (networkDispo) {
            dernierePos = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (dernierePos == null && gpsDispo) {
            dernierePos = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (dernierePos != null) {
            mettreAJourCarte(dernierePos);
        }
    }

    // === LISTENER DE POSITION ===
    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            mettreAJourCarte(location);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MapsActivity.this,
                    provider + " activé", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MapsActivity.this,
                    provider + " désactivé", Toast.LENGTH_SHORT).show();
            afficherDialogueGps();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Méthode obsolète sur API 29+ mais requise pour compatibilité
        }
    };

    // === MISE À JOUR DE LA CARTE ===
    private void mettreAJourCarte(Location location) {
        if (mMap == null) return;

        double lat = location.getLatitude();
        double lon = location.getLongitude();
        LatLng position = new LatLng(lat, lon);

        // Toast discret avec coordonnées
        Toast.makeText(this,
                String.format("📍 %.5f, %.5f", lat, lon),
                Toast.LENGTH_SHORT).show();

        // Un seul marker qui se déplace (propre, pas de pollution)
        if (currentMarker == null) {
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(MAP_TITLE)
                    .snippet(String.format("%.5f, %.5f", lat, lon))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );
        } else {
            currentMarker.setPosition(position);
            currentMarker.setSnippet(String.format("%.5f, %.5f", lat, lon));
        }

        // Animation fluide vers la position
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM_LEVEL));
    }

    // === DIALOGUE GPS DÉSACTIVÉ ===
    private void afficherDialogueGps() {
        new AlertDialog.Builder(this)
                .setTitle("Localisation désactivée")
                .setMessage("Le GPS semble désactivé. Voulez-vous l'activer dans les paramètres ?")
                .setCancelable(false)
                .setPositiveButton("Oui", (dialog, id) -> {
                    startActivity(new Intent(
                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton("Non", (dialog, id) -> dialog.cancel())
                .create()
                .show();
    }

    // === RÉSULTAT PERMISSION ===
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission accordée ✓", Toast.LENGTH_SHORT).show();
                trackingStarted = false; // Reset pour relance
                if (mMap != null) {
                    demarrerTracking();
                }
            } else {
                Toast.makeText(this,
                        "Permission refusée — la localisation ne fonctionnera pas.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // === NETTOYAGE ===
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Optionnel : arrêter les mises à jour en arrière-plan pour économiser la batterie
        // locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reprendre le tracking si nécessaire
        if (mMap != null && !trackingStarted) {
            demarrerTracking();
        }
    }
}
