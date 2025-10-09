package com.example.lab5_starter;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private EditText addCityEditText;
    private EditText addProvinceEditText;
    private ListView cityListView;
    private Button deleteCityButton;
    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;
    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    private ListenerRegistration citiesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // views
        addCityButton = findViewById(R.id.buttonAddCity);
        addCityEditText = findViewById(R.id.city_name_edit);
        addProvinceEditText = findViewById(R.id.province_name_edit);
        cityListView = findViewById(R.id.listviewCities);
        deleteCityButton = findViewById(R.id.buttonDeleteCity);


        // list + adapter
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // firestore database
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        // realtime updates
        citiesListener = citiesRef.addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) {
                Log.w("Firestore", "Listen failed.", e);
                return;
            }
            cityArrayList.clear();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                String name = doc.getId();                 // doc ID = city name
                String province = doc.getString("Province");
                if (name != null && province != null) {
                    cityArrayList.add(new City(name, province));
                }
            }
            cityArrayAdapter.notifyDataSetChanged();
        });

        // add city
        addCityButton.setOnClickListener(v -> {
            String name = addCityEditText.getText().toString().trim();
            String province = addProvinceEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(province)) {
                Toast.makeText(this, "Enter city and province", Toast.LENGTH_SHORT).show();
                return;
            }

            addNewCity(new City(name, province));

            addCityEditText.setText("");
            addProvinceEditText.setText("");
        });
        deleteCityButton.setOnClickListener(v -> {
            String name = addCityEditText.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Enter the city name to delete", Toast.LENGTH_SHORT).show();
                return;
            }

            doDelete(name);
        });
        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(), "City Details");
        });
    }

    private void addNewCity(City city) {
        HashMap<String, String> data = new HashMap<>();
        data.put("Province", city.getProvinceName());

        citiesRef.document(city.getCityName())
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "City written"))
                .addOnFailureListener(e ->
                        Log.w("Firestore", "Write failed: " + e.getMessage(), e)
                );
    }
    private void doDelete(String cityName) {
        citiesRef.document(cityName).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "City not found: " + cityName, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    citiesRef.document(cityName).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Deleted: " + cityName, Toast.LENGTH_SHORT).show();
                                // No manual list update needed—snapshot listener will refresh the UI
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lookup failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (citiesListener != null) {
            citiesListener.remove();
            citiesListener = null;
        }
    }

    @Override
    public void updateCity(City city, String title, String year) {
        city.setCityName(title);          // LAB API
        city.setProvinceName(year);       // LAB API
        cityArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void addCity(City city) {
    }
}
