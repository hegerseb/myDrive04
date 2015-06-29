package UI;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import Helper.NonFrameworkDatabaseHelper;
import mydrive04.fim.de.mydrive.R;

/**
 * Created by dev on 02.06.2015.
 */
public class FragementConsumption extends Fragment {

    private TextView actualConsumption, generalConsumption, date;
    private Button enterConsumption, save, leave;
    private SeekBar kilometers;
    private EditText tkilometer;
    private NumberPicker nbLitres1, nbLitres2;
    private Dialog consumptionDialog;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_consumption_layout, container, false);

        setUI(rootView);


        return rootView;
    }


    private void setUI(View v) {

        enterConsumption = (Button) v.findViewById(R.id.btEnterConsumption);
        actualConsumption = (TextView) v.findViewById(R.id.txAktuellD);
        generalConsumption = (TextView) v.findViewById(R.id.txGesamtD);
        date = (TextView) v.findViewById(R.id.txDate);

        actualizeView();

        enterConsumption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setDialog())
                    consumptionDialog.show();
            }
        });

    }

    private boolean setDialog() {
        consumptionDialog = new Dialog(getActivity());
        consumptionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        consumptionDialog.setContentView(R.layout.dialog_enter_consumption);


        kilometers = (SeekBar) consumptionDialog.findViewById(R.id.sbLiter);
        tkilometer = (EditText) consumptionDialog.findViewById(R.id.txKM);
        save = (Button) consumptionDialog.findViewById(R.id.btSave);
        leave = (Button) consumptionDialog.findViewById(R.id.btBreak);
        nbLitres1 = (NumberPicker) consumptionDialog.findViewById(R.id.npLiter1);
        nbLitres2 = (NumberPicker) consumptionDialog.findViewById(R.id.npLiter2);
        nbLitres1.setWrapSelectorWheel(false);
        nbLitres2.setWrapSelectorWheel(false);

        final String[] values = setOrder(0, 100);

        nbLitres1.setDisplayedValues(values);

        final String[] values2 = setOrder(0, 9);
        nbLitres2.setDisplayedValues(values2);

        nbLitres1.setMinValue(0);
        nbLitres2.setMinValue(0);
        nbLitres1.setMaxValue(100);
        nbLitres2.setMaxValue(9);
        nbLitres1.setValue(51);
        nbLitres1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        nbLitres2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        kilometers.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                tkilometer.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        tkilometer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                kilometers.setProgress(Integer.valueOf(v.getText().toString()));
                return false;
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kilometers.getProgress() > 1 && (nbLitres1.getValue() > 0 || nbLitres2.getValue() > 0)) {
                    int l = Integer.valueOf(values[nbLitres1.getValue()]);
                    int cl = Integer.valueOf(values2[nbLitres2.getValue()]);
                    int d = kilometers.getProgress();
                    double gc = (double) l + ((double) cl / 10.0);
                    storeRefuelData(gc, d);
                    consumptionDialog.dismiss();
                    actualizeView();
                } else
                    Toast.makeText(getActivity().getApplicationContext(), "Kilometer & Verbrauch angeben", Toast.LENGTH_SHORT).show();

            }


        });
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consumptionDialog.dismiss();
            }
        });
        return true;
    }

    private void actualizeView() {
        Cursor values = getRefuelData();
        double total=0, consumption=0, i;
        if(values != null){
            if (values.moveToFirst()) {
                for (i = 0; i < values.getCount(); i++) {
                    consumption = (values.getDouble(2) / values.getDouble(3)) * 100.00;
                    consumption = Math.round(consumption * 100.00) / 100.00;
                    total = total + consumption;
                    values.moveToNext();
                }
                total = total / i;
                total = Math.round(total * 100.00) / 100.00;
                generalConsumption.setText(String.valueOf(total));
            } else
                generalConsumption.setText("N.N.");
            if (values.moveToLast()) {
                consumption = (values.getDouble(2) / values.getDouble(3)) * 100.00;
                consumption = Math.round(consumption * 100.00) / 100.00;
                actualConsumption.setText(String.valueOf(consumption));
                date.setText(values.getString(1));
            } else {
                actualConsumption.setText("N.N.");
                date.setText("N.N.");
            }
        }
    }

    private void storeRefuelData(double liter, int kilometer) {
        NonFrameworkDatabaseHelper db = new NonFrameworkDatabaseHelper(getActivity().getApplicationContext());
        db.insertRefuel(liter, kilometer);
    }

    private Cursor getRefuelData() {
        NonFrameworkDatabaseHelper db = new NonFrameworkDatabaseHelper(getActivity().getApplicationContext());
        return db.readRefuel();
    }


    private String[] setOrder(int min, int max) {

        String values[] = new String[(max - min + 1)];

        for (int i = 1; (i + min) <= max; i++) {
            values[i] = Integer.toString((max - i + 1));
        }
        values[0] = Integer.toString(0);
        return values;
    }
}
