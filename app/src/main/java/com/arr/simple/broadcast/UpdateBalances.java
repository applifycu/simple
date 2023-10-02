package com.arr.simple.broadcast;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.arr.simple.R;

import com.arr.ussd.ResponseUssd;
import com.arr.ussd.utils.UssdUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpdateBalances extends BroadcastReceiver {

    private final String[] ussdCodes = {
        "*222#", "*222*328#", "*222*266#", "*222*767#", "*222*869#"
    };
    private final String[] ussdKeys = {"saldo", "datos", "bonos", "sms", "min"};
    private SharedPreferences spBalance;
    private SharedPreferences.Editor editor;
    private UssdUtils ussd;
    private ResponseUssd response;
    private static final String CHANNEL_ID = "Balances";
    private static final String CHANNEL = "Update balances";
    private Context mContext;
    private String SIM;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        Log.e("Update", "onRecive called");

        // TODO: SharedPreferences
        spBalance = PreferenceManager.getDefaultSharedPreferences(context);
        editor = spBalance.edit();
        SIM = spBalance.getString("sim", "0");

        // ussd
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ussd = new UssdUtils(context);
            response = new ResponseUssd(ussd);
        }
        Handler handler = new Handler(Looper.getMainLooper());
        executeUssdRequest(handler, 0);

        // actualizar widget
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(
                () -> {
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    int[] appWidgetIds =
                            intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                    for (int appWidgetId : appWidgetIds) {
                        // Actualizar la vista del saldo del paquete
                        RemoteViews views =
                                new RemoteViews(context.getPackageName(), R.layout.balances_widget);
                        views.setTextViewText(R.id.appwidget_text_paquete, response.getDataAll());
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }
                });
    }

    private void updateNotificationBalances() {
        // actualizar notificación
        boolean isNotifi = spBalance.getBoolean("balance_notif", true);
        if (!isNotifi) {
            Intent broadcast = new Intent(mContext, NotificationBalances.class);
            mContext.sendBroadcast(broadcast);
        }
    }

    private void executeUssdRequest(Handler handler, int index) {
        if (index >= ussdCodes.length) {
            // Se han realizado todas las consultas
            updateHora();
            boolean isChecked = spBalance.getBoolean("not_update_balances", false);
            if (!isChecked) {
                createNotification(mContext, "Balances", "¡Se han actualizado sus balances!");
            }

            updateNotificationBalances();
            return;
        }
        String ussdCode = ussdCodes[index];
        String ussdKey = ussdKeys[index];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ussd.execute(Integer.parseInt(SIM), ussdCode, ussdKey);
        }
        handler.postDelayed(
                () -> {
                    String response = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        response = ussd.response(ussdKey);
                    }
                    if (!response.isEmpty()) {
                        executeUssdRequest(handler, index + 1);
                    } else {
                        executeUssdRequest(handler, index);
                    }
                },
                5000);
    }

    private void updateHora() {
        Calendar calendar = Calendar.getInstance();
        Date dat = calendar.getTime();
        SimpleDateFormat datFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String hActual = datFormat.format(dat);
        editor.putString("actualizado", "Última actualización: " + hActual.toString());
        editor.apply();
    }

    private void createNotification(Context context, String title, String message) {
        // Crear un canal de notificación para Android 8.0 y versiones posteriores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID, CHANNEL, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Crear la notificación utilizando NotificationCompat.Builder
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_logo_simple)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        // Mostrar la notificación
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(0, builder.build());
    }
}
