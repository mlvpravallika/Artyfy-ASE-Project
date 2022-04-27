package com.gallery.art.ui.home;

import static com.gallery.art.UserActivity.distance;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gallery.art.GpsTracker;
import com.gallery.art.R;
import com.gallery.art.admin_ui.manage.ManageFragment;
import com.gallery.art.databinding.GalleryRecyclerViewBinding;
import com.gallery.art.model.GalleryHomeListAdapter;
import com.gallery.art.model.MuseumItemClickListener;
import com.gallery.art.models.Museums;
import com.gallery.art.models.ViewMode;
import com.gallery.art.preferences;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment implements MuseumItemClickListener {

    private GalleryRecyclerViewBinding binding;
    private List<Museums> myListData = new ArrayList<>();
    private List<Museums> finalList = new ArrayList<>();
    private GpsTracker gpsTracker;
    private DatabaseReference museumsDb = FirebaseDatabase.getInstance().getReference().child("Museums");
    private String loggedInUser = "";
    private List<String> images = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = GalleryRecyclerViewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loggedInUser = preferences.getLoggedInUser(requireContext());
        myListData.clear();
        finalList.clear();
        FirebaseDatabase.getInstance().getReference()
                .child("imagesForMuseum")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    images.add(dataSnapshot.getValue(String.class));
                }
                init();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void init() {
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        CSVReader reader;
        try {
            reader = new CSVReader(new BufferedReader(
                    new InputStreamReader(requireActivity().getAssets().open("ODCAF_v1.0.csv"), "UTF-8")) );
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                if(nextLine[0].equals("Index") || nextLine[15].equals("..") || nextLine[16].equals("..")) {
                    continue;
                }
                int rnd = new Random().nextInt(images.size());
                String desc = nextLine[2];
                if(nextLine[2].equals("..")) {
                    desc = nextLine[3];
                }
                Museums museums = new Museums(nextLine[1],
                        desc,
                        images.get(rnd),
                        new HashMap<>(),
                        nextLine[12],
                        "public",
                        Double.parseDouble(nextLine[15]),
                        Double.parseDouble(nextLine[16]));
                myListData.add(museums);
            }
            System.out.println(finalList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        gpsTracker = new GpsTracker(requireActivity());
        if(gpsTracker.canGetLocation()) {
            double latitude1 = gpsTracker.getLatitude();  //42.299460;
            double longitude1 =  gpsTracker.getLongitude(); //-83.069637;
            fetchMuseumFromFirebase(latitude1,longitude1);
        }else{
            gpsTracker.showSettingsAlert();
        }
    }

    private void fetchMuseumFromFirebase(double latitude1, double longitude1) {
        museumsDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    if (!ds.getKey().equalsIgnoreCase(loggedInUser)){
                        myListData.add(ds.getValue(Museums.class));
                    }
                }
                loadRecyclerView(latitude1,longitude1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void loadRecyclerView(double latitude1, double longitude1){
        for (Museums art:
                myListData) {
            if (distance(latitude1, longitude1, art.getLatitude(), art.getLongitude()) <= 50.0) {
                finalList.add(art);
            }
        }
        GalleryHomeListAdapter adapter = new GalleryHomeListAdapter(finalList,this);
        binding.getRoot().setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.getRoot().setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle("Home");
    }

    @Override
    public void onMuseumClick(@NonNull Museums museums) {
        navigateToFragment(ManageFragment.Companion.viewMuseumInstance(museums, ViewMode.VIEW));
    }

    private void navigateToFragment(Fragment fragmentToNavigate) {
        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragmentToNavigate);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.addToBackStack(fragmentToNavigate.toString());
        fragmentTransaction.commit();
    }
}