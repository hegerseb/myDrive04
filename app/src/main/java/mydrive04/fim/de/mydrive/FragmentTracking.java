package mydrive04.fim.de.mydrive;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;import java.lang.Integer;import java.lang.Override;import java.lang.Runnable;import java.lang.String;import java.lang.System;import java.lang.Void;import mydrive03.hegerseb.fim.de.mydrive03.Feedback;import mydrive03.hegerseb.fim.de.mydrive03.NonFrameworkDatabaseHelper;import mydrive03.hegerseb.fim.de.mydrive03.R;import mydrive03.hegerseb.fim.de.mydrive03.Waker;

/**
 * Created by dev on 02.06.2015.
 */
public class FragmentTracking extends Fragment {
    private Button btStart;
    private static Dialog trackingDialog;
    private static ViewFlipper flipper;
    private static ProgressBar pbInitialize, pbUpload;
    private TextView txSpeed;
    private static TextView txDuration;
    private TextView txDistance;
    private TextView txMeanSpeed;
    private static TextView txTotalDuration;
    private TextView txTotalDistance;
    private static TextView txFeedback;
    private static Button btStopTracking, btAbortTracking;
    private static Button btNextActualConsumption;
    private static Button btNextFeedback;
    private static Button btNextReset;
    private static NumberPicker npActualConsumption1;
    private static NumberPicker npActualConsumption2;
    private static CheckBox cbHasConsumption;
    private static SharedPreferences preference;
    private static String[] values, values2;

    // instance of database
    private static NonFrameworkDatabaseHelper db;

    // timer for driving duration
    private static long timer = 0;
    static Handler timerHandler = new Handler();
    static Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - timer;
            int seconds = (int) millis / 1000;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            txDuration.setText(String.format("%d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    // Notification while Tracking
    private static NotificationCompat.Builder builder;
    private static Notification notification;
    private static NotificationManager notificationManager;

    public void handleNotification(boolean showing) {
        if (showing == true) {
            Intent notificationIntent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent = PendingIntent.getActivity(getActivity().getApplicationContext(), 0, notificationIntent, 0);
            builder = new NotificationCompat.Builder(getActivity().getApplicationContext());

            notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            ;

            builder.setAutoCancel(false);
            builder.setOngoing(true);
            builder.setContentTitle("myDrive");
            builder.setContentText("Tracking...");
            builder.setSmallIcon(R.drawable.wheel);
            builder.setContentIntent(intent);
            builder.setColor(getResources().getColor(R.color.green));
            notification = builder.build();

            notificationManager.notify(0, notification);
        } else {
            notificationManager.cancel(0);
        }
    }

    @Override
    public void onDestroy() {
        if (notificationManager != null)
            notificationManager.cancel(0);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_tracking_layout, container, false);

        btStart = (Button) rootview.findViewById(R.id.btStartTracking);
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkGPS()) {
                    preference = getActivity().getSharedPreferences("default", Context.MODE_PRIVATE);
                    db = new NonFrameworkDatabaseHelper(getActivity().getApplicationContext());
                    setTrackingDialog();
                }


            }
        });
        return rootview;
    }

    private void setTrackingDialog() {
        //set up dialog
        trackingDialog = new Dialog(getActivity());
        trackingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        trackingDialog.setCancelable(false);
        trackingDialog.setCanceledOnTouchOutside(false);
        trackingDialog.setContentView(R.layout.dialog_tracking);
        flipper = (ViewFlipper) trackingDialog.findViewById(R.id.viewFlipperTracking);
        trackingDialog.show();
        // initialize all UI elements
        pbInitialize = (ProgressBar) trackingDialog.findViewById(R.id.pbInitialize);
        txSpeed = (TextView) trackingDialog.findViewById(R.id.txSpeed);
        txDuration = (TextView) trackingDialog.findViewById(R.id.txDuration);
        txDistance = (TextView) trackingDialog.findViewById(R.id.txDistance);
        txFeedback = (TextView) trackingDialog.findViewById(R.id.txFeedback);
        btStopTracking = (Button) trackingDialog.findViewById(R.id.btStopTracking);
        npActualConsumption1 = (NumberPicker) trackingDialog.findViewById(R.id.npActualConsumption);
        npActualConsumption2 = (NumberPicker) trackingDialog.findViewById(R.id.npActualConsumption2);
        cbHasConsumption = (CheckBox) trackingDialog.findViewById(R.id.cbHasConsumption);
        btNextActualConsumption = (Button) trackingDialog.findViewById(R.id.btNextActualConsumption);
        txMeanSpeed = (TextView) trackingDialog.findViewById(R.id.txMeanSpeed);
        txTotalDistance = (TextView) trackingDialog.findViewById(R.id.txTotalDistance);
        txTotalDuration = (TextView) trackingDialog.findViewById(R.id.txTotalDuration);
        btNextFeedback = (Button) trackingDialog.findViewById(R.id.btNextFeedback);
        pbUpload = (ProgressBar) trackingDialog.findViewById(R.id.pbUpload);
        btNextReset = (Button) trackingDialog.findViewById(R.id.btNextReset);
        btAbortTracking = (Button) trackingDialog.findViewById(R.id.btAbortTracking);
        // show reset dialog
        showReset();
    }

    private void showReset() {
        btNextReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPbInitialize();
            }
        });
        btAbortTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackingDialog.dismiss();
            }
        });
    }

    private void showPbInitialize() {
        flipper.showNext();
        pbInitialize.setMax(30);
        pbInitialize.setIndeterminate(false);
        pbInitialize.getProgressDrawable().setColorFilter(R.color.green, PorterDuff.Mode.MULTIPLY);
        // TODO Hier Tracking starten
        // UI sleeping for ten seconds while other thread initializes pipeline
        new CounterThread().execute(30);
        handleNotification(true);
    }

    private static void showTracking() {
        flipper.showNext();
        timer = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
        btStopTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO beende Tracking
                // show enter consumption layout
                timerHandler.removeCallbacks(timerRunnable);
                showConsumptionInput();
            }
        });
    }

    private static void showConsumptionInput() {
        flipper.showNext();
        // if car has no consumption view, skip next dialog
        if (!preference.getBoolean("cbHasConsumption", true)) {
            flipper.showNext();
            showFeedback();
        } else {
            final SharedPreferences.Editor editor = preference.edit();
            // set up number pickers
            npActualConsumption1.setWrapSelectorWheel(false);
            npActualConsumption2.setWrapSelectorWheel(false);
            values = setOrder(0, 40);
            npActualConsumption1.setDisplayedValues(values);
            values2 = setOrder(0, 9);
            npActualConsumption2.setDisplayedValues(values2);
            npActualConsumption1.setMinValue(0);
            npActualConsumption2.setMinValue(0);
            npActualConsumption1.setMaxValue(40);
            npActualConsumption2.setMaxValue(9);
            npActualConsumption1.setValue(36);
            npActualConsumption1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            npActualConsumption2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            btNextActualConsumption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cbHasConsumption.isChecked()) {
                        editor.putBoolean("cbHasConsumption", false);
                        editor.commit();
                    }
                    // TODO Verbauchsanzeige & Datum in DB speichern
                    // initialize last Layout of dialog and uploading data
                    showPbupload();
                }
            });


        }
    }

    private static void showFeedback() {

        String s = Feedback.calculateSocialFeedback(getConsumption(), db.getFrequencyDistribution(0, 0)) + " vergleichbarer Fahrer verbrauchen weniger Kraftstoff.";
        // TODO Unterscheidung zwischen Feedbackarten
        txFeedback.setText(s);
        flipper.showNext();
        txTotalDuration.setText(txDuration.getText());
        storeFeedbackData(s, getConsumption());
        btNextFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Feedback in Verlauf speichern
                notificationManager.cancel(0);
                trackingDialog.dismiss();
                countDrive();
            }
        });
    }

    private static void countDrive() {
        int counter = preference.getInt("number of drives", -1);
        counter++;
        SharedPreferences.Editor editor = preference.edit();
        editor.putInt("number of drives", counter);
        editor.commit();
    }

    private static void storeFeedbackData(String s, double consumption) {
        db.insertFeedback(s, consumption);
    }

    private static double getConsumption() {
        int l = Integer.valueOf(values[npActualConsumption1.getValue()]);
        int cl = Integer.valueOf(values2[npActualConsumption2.getValue()]);
        double gc = (double) l + ((double) cl / 10.0);

        return gc;
    }

    private static void showPbupload() {
        flipper.showNext();
        pbUpload.setIndeterminate(true);
        pbUpload.setVisibility(View.VISIBLE);
        // TODO UPLOAD
        CounterThread thread = new CounterThread();
        thread.execute(10);

    }

    private static String[] setOrder(int min, int max) {

        String values[] = new String[(max - min + 1)];

        for (int i = 1; (i + min) <= max; i++) {
            values[i] = Integer.toString((max - i + 1));
        }
        values[0] = Integer.toString(0);
        return values;
    }

    private boolean checkGPS() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        } else
            return true;
    }

    private void buildAlertMessageNoGps() {
        final Dialog gpsDialog = new Dialog(getActivity());
        gpsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        gpsDialog.setContentView(R.layout.dialog_gps_alert);
        Button gpsSettings = (Button) gpsDialog.findViewById(R.id.btSettingsGPS);

        gpsSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                gpsDialog.dismiss();
            }
        });
        Button gpsAbort = (Button) gpsDialog.findViewById(R.id.btAbortGPS);
        gpsAbort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsDialog.dismiss();
            }
        });
        gpsDialog.setCancelable(false);

        gpsDialog.show();
    }

    public static void updateProgressBar(int i) {
        if (flipper.getDisplayedChild() == 1) {
            pbInitialize.setProgress(i);
            if (i == 30)
                showTracking();

        } else if (flipper.getDisplayedChild() == 4) {
            pbUpload.setProgress(i);
            if (i == 10) {
                showFeedback();


            }
        }
    }

    private static class CounterThread extends AsyncTask<Integer, Integer, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            int i = 0;
            while (i <= params[0]) {
                publishProgress(i);
                Waker.sleep(100);
                i++;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            FragmentTracking.updateProgressBar(values[0]);
        }
    }


}
