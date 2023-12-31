package com.arr.simple.ui.settings;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

import com.arr.preference.M3ListPreference;
import com.arr.preference.M3SwitchPreference;
import com.arr.simple.R;
import com.arr.simple.broadcast.BalanceBroadcast;
import com.arr.simple.broadcast.NotificationBalances;

public class BalancesPreference extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, new Balances())
                .commit();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public static class Balances extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(
                @Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.preference_balance, rootKey);

            // TODO: Programar el tiempo de ejecucion de actualizacion
            M3ListPreference update = findPreference("update_balances");
            update.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        if (newValue.equals("0")) {
                            stopAlarm();
                        } else if (newValue.equals("1")) {
                            long time = 60 * 60 * 1000; // actualiza cada 1 hora
                            setAlarm(time);
                        } else if (newValue.equals("2")) {
                            long time = 12 * 60 * 60 * 1000; // actualiza cada 12 hora
                            setAlarm(time);
                        } else if (newValue.equals("3")) {
                            long time = 60 * 60 * 60 * 1000; // actualiza cada 24 hora
                            setAlarm(time);
                        }
                        return true;
                    });

            // notification update balances
            M3SwitchPreference notify_balances = findPreference("not_update_balances");
            notify_balances.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        boolean isCheck = (Boolean) newValue;
                        if (isCheck) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
                                if (ContextCompat.checkSelfPermission(
                                                getActivity(),
                                                Manifest.permission.POST_NOTIFICATIONS)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(
                                            getActivity(),
                                            new String[] {Manifest.permission.POST_NOTIFICATIONS},
                                            60);
                                    return false;
                                }
                            }
                        }
                        return true;
                    });

            // TODO: Activar recordatorio de vencimiento
            M3SwitchPreference vence = findPreference("vence");
            vence.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        boolean isCheck = (Boolean) newValue;
                        if (isCheck) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (ContextCompat.checkSelfPermission(
                                                getActivity(), Manifest.permission.WRITE_CALENDAR)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(
                                            getActivity(),
                                            new String[] {Manifest.permission.WRITE_CALENDAR},
                                            60);
                                    return false;
                                }
                            }
                        }
                        return true;
                    });

            // TODO: Notificación con informacion de los datos y vencimiento
            M3SwitchPreference notifi = findPreference("balance_notif");
            notifi.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        boolean isCheck = (Boolean) newValue;
                        if (isCheck) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
                                if (ContextCompat.checkSelfPermission(
                                                getActivity(),
                                                Manifest.permission.POST_NOTIFICATIONS)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(
                                            getActivity(),
                                            new String[] {Manifest.permission.POST_NOTIFICATIONS},
                                            60);
                                    return false;
                                } else {
                                    requireActivity()
                                            .sendBroadcast(
                                                    new Intent(
                                                            getActivity(),
                                                            NotificationBalances.class));
                                    return true;
                                }
                            }
                            requireActivity()
                                    .sendBroadcast(
                                            new Intent(getActivity(), NotificationBalances.class));
                        } else {
                            NotificationManagerCompat manager =
                                    NotificationManagerCompat.from(getActivity());
                            manager.cancel(22);
                        }
                        return true;
                    });
        }

        // iniciar alarma
        private void setAlarm(long time) {
            AlarmManager manager =
                    (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getActivity(), BalanceBroadcast.class);
            int flag = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flag |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pending = PendingIntent.getBroadcast(getContext(), 0, intent, flag);
            manager.setRepeating(
                    AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time, time, pending);
        }

        private void stopAlarm() {
            AlarmManager manager =
                    (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            int flag = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flag |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pending =
                    PendingIntent.getBroadcast(
                            getContext(), 0, new Intent(getActivity(), BalanceBroadcast.class), flag);
            manager.cancel(pending);
        }
    }
}
