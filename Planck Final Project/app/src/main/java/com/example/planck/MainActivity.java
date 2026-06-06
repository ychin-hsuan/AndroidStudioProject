package com.example.planck;

import androidx.fragment.app.Fragment;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            currentFragment = new TodayFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, currentFragment)
                    .commit();
            bottomNav.setSelectedItemId(R.id.nav_today);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_today)         selected = new TodayFragment();
            else if (id == R.id.nav_schedule) selected = new ScheduleFragment();
            else if (id == R.id.nav_todo)     selected = new TodoFragment();
            else if (id == R.id.nav_salary)   selected = new SalaryFragment();
            else if (id == R.id.nav_settings) selected = new SettingsFragment();

            if (selected != null) {
                currentFragment = selected;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
                return true;
            }
            return false;
        });
    }

    // OptionsMenu 建立
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    // OptionsMenu 點擊處理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            Fragment refreshed = null;
            int selectedId = bottomNav.getSelectedItemId();
            if (selectedId == R.id.nav_today) refreshed = new TodayFragment();
            else if (selectedId == R.id.nav_schedule) refreshed = new ScheduleFragment();
            else if (selectedId == R.id.nav_todo) refreshed = new TodoFragment();
            else if (selectedId == R.id.nav_salary) refreshed = new SalaryFragment();
            else if (selectedId == R.id.nav_settings) refreshed = new SettingsFragment();

            if (refreshed != null) {
                currentFragment = refreshed;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, refreshed)
                        .commit();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}