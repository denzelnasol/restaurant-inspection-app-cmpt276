package com.group11.cmpt276_project.viewmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.group11.cmpt276_project.R;
import com.group11.cmpt276_project.service.model.ClusterItem;
import com.group11.cmpt276_project.service.model.InspectionReport;
import com.group11.cmpt276_project.service.model.Restaurant;
import com.group11.cmpt276_project.service.repository.IInspectionReportRepository;
import com.group11.cmpt276_project.service.repository.IRestaurantRepository;
import com.group11.cmpt276_project.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Singleton that holds a map of all restaurant markers and gives each one a unique coordinate
public class MapFragmentViewModel extends ViewModel {
    private MediatorLiveData<List<ClusterItem>> mClusterItems;


    private Map<String, Restaurant> restaurants;
    private Map<String, List<InspectionReport>> inspectionReports;

    private Context context;

    private Map<String, BitmapDescriptor> bitmapDescriptorMap;
    private BitmapDescriptor happyBitMap;
    private BitmapDescriptor sadBitMap;
    private BitmapDescriptor neutralBitMap;

    private final double DEFAULT_COORDINATE_OFFSET = 0.00002;

    public MapFragmentViewModel(Context context, LiveData<Map<String, Restaurant>> restaurants, LiveData<Map<String, List<InspectionReport>>> inspectionReports) {
        MapsInitializer.initialize(context);

        this.context = context;

        this.happyBitMap = BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.low_hazard_marker, context));
        this.sadBitMap = BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.high_hazard_marker, context));
        this.neutralBitMap = BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.neutral, context));

        this.bitmapDescriptorMap = new HashMap<String, BitmapDescriptor>(){{
            put(Constants.HIGH, sadBitMap);
            put(Constants.LOW, happyBitMap);
            put(Constants.MODERATE, neutralBitMap);
        }};

        this.mClusterItems = new MediatorLiveData<>();
        this.mClusterItems.addSource(restaurants, (data) -> {
            this.restaurants = data;
            this.generateClusters();
        });
        this.mClusterItems.addSource(inspectionReports, (data) -> {
            this.inspectionReports = data;
            this.generateClusters();
        });
    }

    public LiveData<List<ClusterItem>> getClusterItems() {
        return this.mClusterItems;
    }

    private void generateClusters() {

        if(this.restaurants != null && this.inspectionReports != null) {
            List<ClusterItem> newClusterItems = new ArrayList<>();

            Map<LatLng, Double> existingCoords = new HashMap<>();

            for (Map.Entry<String, Restaurant> entry : this.restaurants.entrySet()) {
                Restaurant restaurant = entry.getValue();

                String trackingNumber = entry.getKey();

                String name = restaurant.getName();
                String address = restaurant.getPhysicalAddress();
                String hazardRating = getHazardRating(trackingNumber);

                double latitude = restaurant.getLatitude();
                double longitude = restaurant.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                BitmapDescriptor icon = getIcon(hazardRating);

                if (existingCoords.containsKey(latLng)) {
                    double offset = existingCoords.get(latLng);
                    latLng = new LatLng(latLng.latitude + 0.00001, latLng.longitude + 0.00001);
                    existingCoords.put(latLng, offset + 0.00001);
                } else {
                    existingCoords.put(latLng, DEFAULT_COORDINATE_OFFSET);
                }
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(icon).snippet(this.context.getString(R.string.hazard_text, address, hazardRating)).title(name);
                ClusterItem clusterItem = new ClusterItem(markerOptions, trackingNumber);
                newClusterItems.add(clusterItem);
            }

            this.mClusterItems.setValue(newClusterItems);
        }

    }

    private Bitmap getBitmap(int drawable, Context context) {
        int height = 100;
        int width = 100;

        Bitmap b = BitmapFactory.decodeResource(context.getResources(), drawable);
        Bitmap bitMap = Bitmap.createScaledBitmap(b, width, height, false);

        return bitMap;
    }

    private BitmapDescriptor getIcon(String hazardRating) {
        return this.bitmapDescriptorMap.getOrDefault(hazardRating, happyBitMap);
    }

    private String getHazardRating(String trackingNumber) {

        List<InspectionReport> reports = this.inspectionReports.getOrDefault(trackingNumber, new ArrayList<>());

        InspectionReport mostRecent = null;

        if(!reports.isEmpty()) {
            mostRecent = reports.get(0);
        }

        String hazardRating = "";

        if (mostRecent != null) {
            hazardRating = mostRecent.getHazardRating();
        }

        return hazardRating;
    }
}
