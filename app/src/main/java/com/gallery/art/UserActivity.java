package com.gallery.art;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.gallery.art.admin_ui.manage.ManageFragment;
import com.gallery.art.ui.artfinder.ArtFinderFragment;
import com.gallery.art.ui.artist.ArtistListFragment;
import com.gallery.art.ui.artist.manage.ArtistManageFragment;
import com.gallery.art.ui.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;

public class UserActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    int navigationPosition = 0;
    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_user);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,0,0);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.navigation_manage).setVisible(!preferences.getDataAs(this).equalsIgnoreCase("user"));

        navigationPosition = R.id.nav_home;
        navigationView.setCheckedItem(navigationPosition);
        addFragment(new HomeFragment());
        toolbar.setTitle("Home");
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.nav_home: {
                    navigationPosition = R.id.nav_home;
                    navigateToFragment(new HomeFragment());
                    toolbar.setTitle("Home");
                }
                break;
                case R.id.navigation_manage: {
                    navigationPosition = R.id.navigation_manage;
                    if (preferences.getDataAs(this).equalsIgnoreCase("Manager")) {
                        navigateToFragment(ManageFragment.Companion.newInstance());
                    }else {
                        navigateToFragment(ArtistManageFragment.Companion.newInstance());
                    }
                    toolbar.setTitle("Manage");
                }
                break;
                case R.id.nav_artists: {
                    navigationPosition = R.id.nav_artists;
                    navigateToFragment(new ArtistListFragment());
                    toolbar.setTitle("Aritsts");
                }
                break;
            }
            item.setChecked(true);
            drawer.closeDrawers();
            return true;
        });
    }

    private void addFragment(Fragment fragmentToNavigate) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.frameLayout, fragmentToNavigate);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    private void navigateToFragment(Fragment fragmentToNavigate) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragmentToNavigate);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.addToBackStack(fragmentToNavigate.toString());
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.LEFT)) {
            drawer.closeDrawer(Gravity.LEFT);
        }
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_art_finder) {
            navigateToFragment(ArtFinderFragment.newInstance());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void logout(MenuItem item) {
        startActivity(new Intent(this, SignInActivity.class));
        com.gallery.art.preferences.clearData(this);
        finish();
    }

    public static double distance(double lat_1, double lon_1, double lat_2, double lon_2) {

        double lattitude_difference = Math.toRadians(lat_2 - lat_1);
        double longitude_difference = Math.toRadians(lon_2 - lon_1);

        double dist = Math.sin(lattitude_difference/2) * Math.sin(lattitude_difference/2) +
                Math.cos(Math.toRadians(lat_1) * Math.cos(Math.toRadians(lat_2))) *
                        Math.sin(longitude_difference/2) * Math.sin(longitude_difference/2);

        double distance = 2 * Math.asin(Math.sqrt(dist));

        return(distance * 6371); // 6371 is the earth radius in kilometers
    }
}