package com.arr.simple;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.arr.bugsend.BugSend;
import com.arr.simple.broadcast.NotificationNewYear;
import com.arr.simple.databinding.ActivityMainBinding;
import com.arr.simple.databinding.NavRailHeaderBinding;
import com.arr.simple.log.CrashActivity;
import com.arr.simple.services.TrafficFloatingWindow;
import com.arr.simple.utils.Greeting.GreetingUtils;
import com.arr.simple.utils.profile.ImageUtils;
import com.arr.simple.utils.scanner.ScannerQR;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private DrawerLayout drawer;
    private ActivityMainBinding binding;
    private String code = "";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);

        // mostrar una notificación por año nuevo
        initNotificationNewYear();

        // TODO: Quitar el foco de los TextInputEditText al entrar a una Activity o Fragment
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        new BugSend(this).setLaunchActivity(CrashActivity.class).show();

        // TODO:StatusBarColor
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_0.getColor(this));

        // TODO: navigationBarColor
        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));

        // load name and greeat
        binding.appBarMain.textSaludo.setText(GreetingUtils.hello());
        if (getNombre().isEmpty()) {
            binding.appBarMain.textName.setText("Usuario");
        } else {
            binding.appBarMain.textName.setText(getNombre());
        }

        drawer = binding.drawerLayout;
        NavigationRailView railView = binding.navRail;
        NavigationView navigationView = binding.navView;

        // cambiar los iconos del navigationRail en Halloween
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        Menu menu = railView.getMenu();
        MenuItem i1 = menu.findItem(R.id.nav_rail_home);
        MenuItem i2 = menu.findItem(R.id.nav_rail_servicios);
        MenuItem i3 = menu.findItem(R.id.nav_rail_correo);
        MenuItem i4 = menu.findItem(R.id.nav_rail_telepuntos);
        MenuItem i5 = menu.findItem(R.id.nav_rail_settings);

        if (month == Calendar.OCTOBER && dayOfMonth >= 30 && dayOfMonth <= 31) {
            i1.setIcon(R.drawable.halloween_home);
            i2.setIcon(R.drawable.skull_24);
            i3.setIcon(R.drawable.halloween_pumpkin);
            i4.setIcon(R.drawable.halloween_ghost);
            i5.setIcon(R.drawable.halloween_spider);
        }

        // * del 25 al 31 se agregan iconos de navidad
        if (month == Calendar.DECEMBER && dayOfMonth >= 25 && dayOfMonth <= 31) {
            i1.setIcon(R.drawable.navidad_home);
            i2.setIcon(R.drawable.navidad_services);
            i3.setIcon(R.drawable.navidad_correo);
            i4.setIcon(R.drawable.navidad_telepuntos);
            i5.setIcon(R.drawable.navidad_settings);
        }

        // OnItemSelected railView
        railView.setOnItemSelectedListener(
                menuItem -> {
                    int id = menuItem.getItemId();
                    if (id == R.id.nav_rail_servicios) {
                        navController.navigate(id, null);
                    }
                    if (id == R.id.nav_rail_correo) {
                        navController.navigate(id, null);
                    }
                    if (id == R.id.nav_rail_telepuntos) {
                        openGoogleMap();
                        // startActivity(new Intent(this, Test.class));
                    }
                    if (id == R.id.nav_rail_settings) {
                        navController.navigate(id, null);
                    }

                    return false;
                });

        // TODO: Ocultar balances para android inferior a 8
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            binding.appBarMain
                    .bottomNavigation
                    .getMenu()
                    .findItem(R.id.nav_balance)
                    .setVisible(false);
        }

        // TODO: Acceder a escanear QR con numero de móvil
        View view = railView.getHeaderView();
        assert view != null;
        com.arr.simple.databinding.NavRailHeaderBinding header = NavRailHeaderBinding.bind(view);
        header.scanner.setOnClickListener(
                v -> {
                    ScanOptions scanner = new ScanOptions();
                    scanner.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
                    scanner.setCaptureActivity(ScannerQR.class);
                    scanner.setPrompt("Centre el QR para escanear el numero movil");
                    scanner.setOrientationLocked(true);
                    scanner.setBeepEnabled(false);
                    scanner.setTimeout(15000);
                    barcodeLauncher.launch(scanner);
                });

        // TODO: Passing each menu ID as a set of Ids because each
        // TODO: menu should be considered as top level destinations.
        mAppBarConfiguration =
                new AppBarConfiguration.Builder(
                                R.id.nav_home,
                                R.id.nav_balance,
                                R.id.nav_compras,
                                R.id.nav_llamadas,
                                R.id.nav_nauta)
                        .setOpenableLayout(drawer)
                        .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        supporNavigateBottomNavigation();
        NavigationUI.setupWithNavController(navigationView, navController);
        // loadProfile();

        // TODO: Navigation destination
        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) -> {
                    int id = destination.getId();
                    if (id == R.id.nav_rail_settings
                            || id == R.id.nav_ui
                            || id == R.id.nav_pref_balance
                            || id == R.id.nav_security
                            || id == R.id.nav_sim
                            || id == R.id.nav_perfil
                            || id == R.id.nav_rail_correo
                            || id == R.id.nav_rail_servicios
                            || id == R.id.nav_info_nauta
                            || id == R.id.nav_conectado
                            || id == R.id.nav_rail_about
                            || id == R.id.nav_mails
                            || id == R.id.nav_help) {
                        binding.appBarMain.contentToolbar.setVisibility(View.GONE);
                        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_0.getColor(this));
                        binding.appBarMain.bottomNavigation.setVisibility(View.GONE);
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                        }
                    }
                    if (id == R.id.nav_home
                            || id == R.id.nav_balance
                            || id == R.id.nav_compras
                            || id == R.id.nav_llamadas
                            || id == R.id.nav_nauta) {
                        binding.appBarMain.contentToolbar.setVisibility(View.VISIBLE);
                        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
                        binding.appBarMain.bottomNavigation.setVisibility(View.VISIBLE);
                        binding.appBarMain.toolbar.setNavigationIcon(loadProfile());
                    }
                });
    }

    private void openGoogleMap() {
        startActivity(
                new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.view_telepuntos))));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable loadProfile() {
        Bitmap image = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            image = new ImageUtils(this).setRounded(true).getSavedImage();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_DENIED) {
                image = new ImageUtils(this).setRounded(true).getSavedImage();
            }
        }
        if (image != null) {
            int width = 90;
            int height = 90;
            Bitmap profile = Bitmap.createScaledBitmap(image, width, height, true);
            return new BitmapDrawable(getResources(), profile);
        } else {
            return getDrawable(R.drawable.ic_account_circle_24px);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void supporNavigateBottomNavigation() {
        NavigationUI.setupWithNavController(binding.appBarMain.bottomNavigation, navController);
    }

    // TODO: Cargar nombre de la persona
    public String getNombre() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getString("name", "");
    }

    public CoordinatorLayout getCoordinator() {
        return binding.appBarMain.coordinator;
    }

    public BottomNavigationView getBottomNavigation() {
        return binding.appBarMain.bottomNavigation;
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public String getCode() {
        return code;
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(
                    new ScanContract(),
                    result -> {
                        if (result.getContents() != null) {
                            code = result.getContents();
                            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                                binding.drawerLayout.closeDrawer(GravityCompat.START);
                            }
                        }
                    });

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences spFloating = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isActive = spFloating.getBoolean("traffic", false);
        if (isActive) {
            Intent intent = new Intent(this, TrafficFloatingWindow.class);
            startService(intent);
        }
    }

    private void initNotificationNewYear() {
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationNewYear.class);
        intent.setAction("com.arr.simple.NOTIFICATION");
        PendingIntent pIntent =
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        alarm.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
    }
}
