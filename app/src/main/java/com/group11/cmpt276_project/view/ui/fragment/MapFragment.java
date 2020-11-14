package com.group11.cmpt276_project.view.ui.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;
import com.group11.cmpt276_project.R;
import com.group11.cmpt276_project.databinding.FragmentMapBinding;
import com.group11.cmpt276_project.service.model.ClusterItem;
import com.group11.cmpt276_project.service.model.GPSCoordiantes;
import com.group11.cmpt276_project.service.model.InspectionReport;
import com.group11.cmpt276_project.service.model.Restaurant;
import com.group11.cmpt276_project.utils.Constants;
import com.group11.cmpt276_project.view.ui.MainPageActivity;
import com.group11.cmpt276_project.view.ui.RestaurantDetailActivity;
import com.group11.cmpt276_project.viewmodel.ClusterRenderer;
import com.group11.cmpt276_project.viewmodel.InspectionReportsViewModel;
import com.group11.cmpt276_project.viewmodel.RestaurantsViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fragment to implement a map including user location and restaurant markers
public class MapFragment extends Fragment {

    private static final float DEFAULT_ZOOM = 12f;

    private FragmentMapBinding binding;
    private SupportMapFragment mapFragment;

    private RestaurantsViewModel restaurantsViewModel;
    private InspectionReportsViewModel inspectionReportsViewModel;

    private GPSCoordiantes selected;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private GoogleMap mGoogleMap;
    private ClusterManager clusterManager;
    private ClusterRenderer clusterRenderer;
    private ProgressBar progressBar;
    private HashMap<String, ClusterItem> visibleClusterItems = new HashMap<String, ClusterItem>();
    private List<ClusterItem> listClusterItems;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    try {
                        setUpClusters();
                        addClusterItemsToMap();
                        zoomToUserLocation();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }.execute();
        }
    };

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false);
        this.bind();
        return this.binding.getRoot();
    }

    private void bind() {
        this.restaurantsViewModel = RestaurantsViewModel.getInstance();
        this.inspectionReportsViewModel = InspectionReportsViewModel.getInstance();
        MainPageActivity activity = (MainPageActivity) getActivity();
        this.selected = activity.getGpsCoordinates();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(callback);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        createLocationRequest();

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkSettingAndStartLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    //Stops location updates
    @Override
    public void onStop() {
        super.onStop();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(20000);
        locationRequest.setFastestInterval(15000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void checkSettingAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this.getActivity());

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // Starts location updates
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        });
    }

    private void zoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();

        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mGoogleMap.setMyLocationEnabled(true);
            }
        });
    }

    private void setUpClusters() {
        clusterManager = new ClusterManager<>(this.getContext(), mGoogleMap);
        clusterRenderer = new ClusterRenderer(this.getActivity(), mGoogleMap, clusterManager);

        mGoogleMap.setOnCameraIdleListener(clusterManager);
        mGoogleMap.setOnMarkerClickListener(clusterManager);

        // Change Activity on info window click
        clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<ClusterItem>() {
            @Override
            public void onClusterItemInfoWindowClick(ClusterItem item) {
                for (Map.Entry<String, Restaurant> entry : restaurantsViewModel.get().getValue().entrySet()) {
                    LatLng tempLatLng = new LatLng(entry.getValue().getLatitude(), entry.getValue().getLongitude());
                    if (item.getPosition().equals(tempLatLng)) {
                        Intent intent = RestaurantDetailActivity.startActivity(getActivity(), entry.getValue().getTrackingNumber());
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private void addClusterItemsToMap() {
        if (this.mGoogleMap != null) {
            LatLngBounds bounds = this.mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
            for (Map.Entry<String, Restaurant> entry : this.restaurantsViewModel.get().getValue().entrySet()) {
                // Add marker
                String name = entry.getValue().getName();
                String address = entry.getValue().getPhysicalAddress();
                String hazardRating;
                String trackingNumber = this.restaurantsViewModel.getByTrackingNumber(entry.getValue().getTrackingNumber()).getTrackingNumber();
                double latitude = entry.getValue().getLatitude();
                double longitude = entry.getValue().getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                MarkerOptions markerOptions;
                BitmapDescriptor icon;

                // Set hazard rating string for each marker
                hazardRating = setHazardRating(entry);

                // Set icon for each marker
                icon = setIcon(entry);

                // Add marker to cluster
                markerOptions = new MarkerOptions().position(latLng).icon(icon).snippet(address + " - Hazardous Rating: " + hazardRating).title(name);
                ClusterItem clusterItem = new ClusterItem(markerOptions);
                clusterManager.addItem(clusterItem);
            }
        }
    }

    private String setHazardRating(Map.Entry<String, Restaurant> entry) {
        String hazardRating;
        String trackingNumber = this.restaurantsViewModel.getByTrackingNumber(entry.getValue().getTrackingNumber()).getTrackingNumber();
        InspectionReport inspectionReport = this.inspectionReportsViewModel.getMostRecentReport(trackingNumber);

        if (inspectionReport != null && inspectionReportsViewModel.getReports(entry.getValue().getTrackingNumber()).get(0).getHazardRating().equals(Constants.MODERATE)) {
            hazardRating = "Moderate";
        }
        else if (inspectionReport != null && inspectionReportsViewModel.getReports(entry.getValue().getTrackingNumber()).get(0).getHazardRating().equals(Constants.CRITICAL)){
            hazardRating = "Critical";
        }
        else {
            hazardRating = "Low";
        }

        return hazardRating;
    }

    private BitmapDescriptor setIcon(Map.Entry<String, Restaurant> entry) {
        BitmapDescriptor icon;
        String trackingNumber = this.restaurantsViewModel.getByTrackingNumber(entry.getValue().getTrackingNumber()).getTrackingNumber();
        InspectionReport inspectionReport = this.inspectionReportsViewModel.getMostRecentReport(trackingNumber);

        if (inspectionReport != null && inspectionReportsViewModel.getReports(entry.getValue().getTrackingNumber()).get(0).getHazardRating().equals(Constants.MODERATE)) {
            icon = BitmapDescriptorFactory.fromBitmap(changeMarker(R.drawable.neutral));
        }
        else if (inspectionReport != null && inspectionReportsViewModel.getReports(entry.getValue().getTrackingNumber()).get(0).getHazardRating().equals(Constants.CRITICAL)){
            icon = BitmapDescriptorFactory.fromBitmap(changeMarker(R.drawable.sad));
        }
        else {
            icon = BitmapDescriptorFactory.fromBitmap(changeMarker(R.drawable.happy));
        }

        return icon;
    }

    private Bitmap changeMarker(int drawable) {
        int height = 100;
        int width = 100;

        Bitmap b = BitmapFactory.decodeResource(getResources(), drawable);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        return smallMarker;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkSettingAndStartLocationUpdates();
            }
        }
    }
}