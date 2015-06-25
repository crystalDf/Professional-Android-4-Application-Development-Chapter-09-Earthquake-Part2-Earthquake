package com.star.earthquake;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;


public class EarthquakeActivity extends AppCompatActivity {

    private static final int MENU_PREFERENCES = Menu.FIRST + 1;
    private static final int MENU_UPDATE = Menu.FIRST + 2;

    private static final int SHOW_PREFERENCES = 1;

    private boolean autoUpdateChecked = false;
    private int updateFreq = 0;
    private int minMag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake);

        updateFromPreferences();

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchableInfo searchableInfo =
                searchManager.getSearchableInfo(getComponentName());

        SearchView searchView = (SearchView) findViewById(R.id.searchView);

        searchView.setSearchableInfo(searchableInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_preferences);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case MENU_PREFERENCES:

                Class clazz = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ?
                        UserPreferenceActivity.class : UserFragmentPreferenceActivity.class;

                Intent i = new Intent(this, clazz);
                startActivityForResult(i, SHOW_PREFERENCES);
                return true;
        }

        return false;
    }

    private void updateFromPreferences() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        autoUpdateChecked =
                sharedPreferences.getBoolean(UserPreferenceActivity.PREF_AUTO_UPDATE, false);

        updateFreq = Integer.parseInt(
                sharedPreferences.getString(UserPreferenceActivity.PREF_UPDATE_FREQ, "60"));

        minMag = Integer.parseInt(
                        sharedPreferences.getString(UserPreferenceActivity.PREF_MIN_MAG, "3"));

    }

    public int getMinMag() {
        return minMag;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_PREFERENCES) {
            updateFromPreferences();

            FragmentManager fragmentManager = getSupportFragmentManager();
            final EarthquakeListFragment earthquakeListFragment =
                    (EarthquakeListFragment) fragmentManager.findFragmentById(
                            R.id.EarthquakeListFragment);

            earthquakeListFragment.refreshEarthquakes();
        }
    }
}
