package com.group11.cmpt276_project.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Pair;

import com.group11.cmpt276_project.service.model.InspectionReport;
import com.group11.cmpt276_project.service.model.Restaurant;
import com.group11.cmpt276_project.service.model.Violation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Various utility functions that are useful such as loading json
 */
public class Utils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetwork() != null;
    }

    public static boolean isConnected(Context context) {
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(Constants.CONNECTION_TEST_URL).openConnection();
                connection.setRequestProperty("User-Agent", "ConnectionTest");
                connection.setRequestProperty("Connection", "close");
                connection.setConnectTimeout(1000);
                connection.connect();
                return connection.getResponseCode() == 200;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static String readJsonFromAssets(Context context, String fileName) throws IOException {
        InputStream inputStream = context.getAssets().open(fileName);

        int size = inputStream.available();

        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();

        return new String(buffer, Constants.ENCODING);
    }

    public static void writeJSONToStorage(Context context, String fileName, String toWrite) throws IOException {
        File file = new File(context.getFilesDir(), fileName);
        try (FileWriter fileWriter = new FileWriter(file);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(toWrite);
        }
    }

    public static String readJSONFromStorage(Context context, String fileName) throws IOException {
        File file = new File(context.getFilesDir(), fileName);

        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            StringBuilder stringBuilder = new StringBuilder();

            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }

            return stringBuilder.toString();
        }
    }

    public static List<List<String>> readCSVFromStorage(Context context, String fileName) throws IOException {
        File file = new File(context.getFilesDir(), fileName);

        try (FileReader fileReader = new FileReader(file)) {
            List<List<String>> rows = new ArrayList<>();
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(fileReader);

            for (CSVRecord record : records) {
                List<String> row = new ArrayList<>();

                for (int i = 0; i < record.size(); i++) {
                    row.add(record.get(i));
                }

                rows.add(row);
            }

            return rows;
        }
    }

    public static void deleteFileFromStorage(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        file.delete();
    }

    public static Map<String, Restaurant> csvToRestaurants(List<List<String>> csv) {
        Map<String, Restaurant> updatedMap = new HashMap<>();

        for (List<String> row : csv) {
            Restaurant restaurant = new Restaurant.RestaurantBuilder()
                    .withTrackingNumber(row.get(0))
                    .withName(row.get(1))
                    .withPhysicalAddress(row.get(2))
                    .withPhysicalCity(row.get(3))
                    .withFacilityType(row.get(4))
                    .withLatitude(Double.parseDouble(row.get(5)))
                    .withLongitude(Double.parseDouble(row.get(6)))
                    .build();
            updatedMap.put(row.get(0), restaurant);
        }

        return updatedMap;
    }

    public static Pair<Map<String, List<InspectionReport>>, Map<String, Violation>> csvToInspections(List<List<String>> csv) {

        Map<String, Violation> newViolations = new HashMap<>();
        Map<String, List<InspectionReport>> newInspections = new HashMap<>();

        for (List<String> row : csv) {
            String trackingNumber = row.get(0);

            if (trackingNumber == null | trackingNumber.isEmpty()) {
                continue;
            }

            String[] violLump = row.get(5).split("\\|");

            List<String> violations = new ArrayList<>();

            for (String viol : violLump) {
                String[] split = viol.split(",");

                if (split.length < 4) {
                    continue;
                }

                String id = split[0];

                Violation violation = new Violation.ViolationBuilder()
                        .withId(id)
                        .withStatus(split[1])
                        .withDetails(split[1])
                        .withType(split[2])
                        .build();

                newViolations.put(id, violation);
                violations.add(id);
            }

            InspectionReport inspectionReport = new InspectionReport.InspectionReportBuilder()
                    .withTrackingNumber(trackingNumber)
                    .withInspectionDate(row.get(1))
                    .withInspectionType(row.get(2))
                    .withNumberCritical(Integer.parseInt(row.get(3)))
                    .withNumberNonCritical(Integer.parseInt(row.get(4)))
                    .withViolLump(violations)
                    .withHazardRating(row.get(6))
                    .build();


            newInspections.computeIfAbsent(trackingNumber, (k) -> new ArrayList<>()).add(inspectionReport);
        }

        for (Map.Entry<String, List<InspectionReport>> entry : newInspections.entrySet()) {
            Collections.sort(entry.getValue(), (InspectionReport A, InspectionReport B) -> Integer.parseInt(B.getInspectionDate()) - Integer.parseInt(A.getInspectionDate()));
        }

        return new Pair(newInspections, newViolations);
    }
}
