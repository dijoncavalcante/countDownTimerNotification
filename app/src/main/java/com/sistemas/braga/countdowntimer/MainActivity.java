package com.sistemas.braga.countdowntimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private String EVENT_DATE_TIME = "2019-12-31 10:30:00";
    private String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private TextView tv_days;
    /*
    variaveis para mostrar o countdown mais vibração
     */
    private static final String FORMAT = "%02d:%02d:%02d";
    Vibrator v;
    Button btnBaterPonto;
    /*
    variaveis para a notificação
     */
    // Constants for the notification actions buttons.
    private static final String ACTION_UPDATE_NOTIFICATION =            "com.android.example.notifyme.ACTION_UPDATE_NOTIFICATION";
    // Notification channel ID.
    private static final String PRIMARY_CHANNEL_ID =            "primary_notification_channel";
    // Notification ID.
    private static final int NOTIFICATION_ID = 0;
    private NotificationManager mNotifyManager;
    private NotificationReceiver mReceiver = new NotificationReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        /*
            Notificao
         */
        // Create the notification channel.
        createNotificationChannel();
        // Register the broadcast receiver to receive the update action from the notification.
        registerReceiver(mReceiver,new IntentFilter(ACTION_UPDATE_NOTIFICATION));
        /*
        Logica para calcular o countdown para sair do trabalho
         */
        Date dataHoraEntrada = new Date();
        dataHoraEntrada.setHours(16);
        dataHoraEntrada.setMinutes(8);
        dataHoraEntrada.setSeconds(0);

        Date dataHoraSaida = new Date();
        dataHoraSaida.setHours(16);
        dataHoraSaida.setMinutes(13);
        dataHoraSaida.setSeconds(10);

        long diff = dataHoraSaida.getTime() - dataHoraEntrada.getTime();

        new CountDownTimer(diff, 1000) { // adjust the milli seconds here 16069000

            public void onTick(long millisUntilFinished) {

                String minutosRestantes = String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                tv_days.setText(minutosRestantes);

                if (tv_days.getText().toString().equals("00:15:00")) {
                    tv_days.setBackgroundColor(Color.GREEN);
                    sendNotification(15);
                } else if (tv_days.getText().toString().equals("00:10:00")) {
                    tv_days.setBackgroundColor(Color.YELLOW);
                    sendNotification(10);
                } else if (tv_days.getText().toString().equals("00:05:00")) {
                    tv_days.setBackgroundColor(Color.RED);
                    // Send the notification
                    sendNotification(5);
                }
                else if(tv_days.getText().toString().equals("00:02:00")) {
                    sendNotification(2);
                }
                else if (tv_days.getText().toString().equals("00:01:00")) {
                    sendNotification(1);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(30000, VibrationEffect.DEFAULT_AMPLITUDE));
                        btnBaterPonto.setVisibility(View.VISIBLE);
                    } else {
                        //deprecated in API 26
                        v.vibrate(30000);
                    }
                }

            }

            public void onFinish() {
                //if(cb0147.ischecked)
                //////a = "Você COMPLETOU 10 horas de trabalho ja devia ter batido o ponto e deve sair das dependencias do Instituto SIDIA AMAZON TOWER!"
                //ELSE
                tv_days.setText("Você completou seu horário PADRÃO de trabalho e agora pode deixar as dependencias do Instituto SIDIA AMAZON TOWER!");
                tv_days.setBackgroundColor(Color.WHITE);
                sendNotification(1);
            }
        }.start();

    }


    private void initUI() {
        tv_days = findViewById(R.id.tvDia);
        btnBaterPonto = findViewById(R.id.btnBaterPonto);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void onClickbtnBaterPonto(View view) {
        v.cancel();
        btnBaterPonto.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public void createNotificationChannel() {

        // Create a notification manager object.
        mNotifyManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    (PRIMARY_CHANNEL_ID,
                            getString(R.string.notification_channel_name),
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription
                    (getString(R.string.notification_channel_description));

            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    public void sendNotification(int opcao) {
        Intent updateIntent = new Intent(ACTION_UPDATE_NOTIFICATION);
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, updateIntent, PendingIntent.FLAG_ONE_SHOT);
        // Build the notification with all of the parameters using helper method
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder(opcao);
        // Add the action button using the pending intent.
        //definir a imagem da notificacao
        notifyBuilder.addAction(R.drawable.ic_launcher_background, getString(R.string.ignore), updatePendingIntent);
        // Deliver the notification.
        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());
    }

    public void ignoreNotification(){
        mNotifyManager.cancel(NOTIFICATION_ID);
    }

    private NotificationCompat.Builder getNotificationBuilder(int opcao) {

        // Set up the pending intent that is delivered when the notification
        // is clicked.
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity
                (this, NOTIFICATION_ID, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        String txtNotificacao = "";

        if (opcao == 5) txtNotificacao = getString(R.string.notification_text_5);
        else if (opcao == 10) txtNotificacao = getString(R.string.notification_text_10);
       else if (opcao == 15) txtNotificacao = getString(R.string.notification_text_15);
       else if (opcao == 1) txtNotificacao = getString(R.string.notification_text_1);
        // Build the notification with all of the parameters.
        NotificationCompat.Builder notifyBuilder = new NotificationCompat
                .Builder(this, PRIMARY_CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(txtNotificacao)
                .setSmallIcon(R.drawable.ic_launcher_background)//definiar a imagem da notificação
                .setAutoCancel(true)
                .setContentIntent(notificationPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        return notifyBuilder;
    }

    public class NotificationReceiver extends BroadcastReceiver {

        public NotificationReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Update the notification.
            ignoreNotification();
        }
    }
}
