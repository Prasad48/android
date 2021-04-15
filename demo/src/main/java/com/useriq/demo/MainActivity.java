package com.useriq.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.useriq.OrientationActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentTransaction ft;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, MainFragment.newInstance(), "home");
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if (fragmentManager.getBackStackEntryCount() == 1) {
            finish();
        } else {
            fragmentManager.popBackStackImmediate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updatedUser(String id, String userName, String api) {
        View header = navigationView.getHeaderView(0);
        ((TextView)header.findViewById(R.id.userId)).setText(id);
        ((TextView) header.findViewById(R.id.userName)).setText(userName);
        ((TextView) header.findViewById(R.id.api)).setText(api);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        switch (item.getItemId()) {
            case R.id.home: {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("home");
                ft = fragmentManager.beginTransaction();
                if (fragment == null) {
                    ft.replace(R.id.container, MainFragment.newInstance(), "home");
                    ft.addToBackStack(null);
                    ft.commit();
                } else {
                    ft.replace(R.id.container, fragment, "home");
                    ft.commit();
                }
                break;
            }
            case R.id.tab: {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("tabs");
                ft = fragmentManager.beginTransaction();
                if (fragment == null) {
                    ft.replace(R.id.container, TabsFragment.newInstance(), "tabs");
                    ft.addToBackStack(null);
                    ft.commit();
                } else {
                    ft.replace(R.id.container, fragment, "tabs");
                    ft.commit();
                }
                break;
            }
            case R.id.clippedRect: {
                startActivity(new Intent(MainActivity.this, ClippedRectActivity.class));
            }
            break;
            case R.id.systemModes: {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("sysModes");
                ft = fragmentManager.beginTransaction();
                if (fragment == null) {
                    ft.replace(R.id.container, SystemModesFragment.newInstance(), "sysModes");
                    ft.addToBackStack(null);
                    ft.commit();
                } else {
                    ft.replace(R.id.container, fragment, "sysModes");
                    ft.commit();
                }
                break;
            }
            case R.id.layers: {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("layers");
                ft = fragmentManager.beginTransaction();
                if (fragment == null) {
                    ft.replace(R.id.container, LayersFragment.newInstance(), "layers");
                    ft.addToBackStack(null);
                    ft.commit();
                } else {
                    ft.replace(R.id.container, fragment, "layers");
                    ft.commit();
                }
                break;
            }
            case R.id.web_view: {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("web_view");
                ft = fragmentManager.beginTransaction();
                if (fragment == null) {
                    ft.replace(R.id.container, WebViewFragment.newInstance(), "web_view");
                    ft.addToBackStack(null);
                    ft.commit();
                } else {
                    ft.replace(R.id.container, fragment, "web_view");
                    ft.commit();
                }
                break;
            }
            case R.id.landscape: {
                startActivity(new Intent(this, OrientationActivity.class));
                break;
            }
            case R.id.track: {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("track");
                ft = fragmentManager.beginTransaction();
                if (fragment == null) {
                    ft.replace(R.id.container, TrackFragment.newInstance(), "track");
                    ft.addToBackStack(null);
                    ft.commit();
                } else {
                    ft.replace(R.id.container, fragment, "track");
                    ft.commit();
                }
                break;
            }
            default:
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
