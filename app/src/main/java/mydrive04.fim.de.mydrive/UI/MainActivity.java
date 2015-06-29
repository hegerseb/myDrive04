package UI;


import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;
import Helper.NonFrameworkDatabaseHelper;
import mydrive04.fim.de.mydrive.R;


public class MainActivity extends AppCompatActivity {

    private boolean DEBUG = false;
    private int DRIVECOUNTER = 8;

    private ViewPager pager;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mDrawerListItems;

    private Dialog impressumDialog, initializeDialog;
    private Button impressumBack, initializeNext, initializePrevious;
    private ViewFlipper flipper;
    private Spinner spCar, spPowerClass, spFuelType, spNumberDriver,
            spNumberKilometer, spNumberDriving, spContextDriving, spCostsDriving, spCostsRelevance,
            spEnvironmentRelevance, spSocialRelevance, spGender;
    private EditText etModel, etAge;


    private SharedPreferences preference;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        // setup DB
        NonFrameworkDatabaseHelper db = new NonFrameworkDatabaseHelper(this);
        db.createDB();
        // Shared Preferences
        preference = getPreferences(MODE_PRIVATE);

        if (!preference.getBoolean("firstUse", false)||DEBUG) {
            showInitializeDialog(preference);
        }

        if(preference.getInt("number of drives", -1)== DRIVECOUNTER){
            // show Dialog
        } else {
            Toast.makeText(getApplicationContext(),"Bisher erfasste Fahrten: " +
                    preference.getInt("number of drives", 0),Toast.LENGTH_LONG).show();
        }


        // ViewPager for Main Fragments
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setBackgroundResource(R.drawable.wallpaper_3); // set background image
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        pager.setCurrentItem(1); // UI starting with second screen

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mDrawerList = (ListView) findViewById(android.R.id.list);
        mDrawerListItems = getResources().getStringArray(R.array.drawer_list);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDrawerListItems));


        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        mDrawerLayout.closeDrawer(mDrawerList);
                        pager.setCurrentItem(1);
                        break;
                    }

                    case 1: {
                        showImpressumDialog();
                    }
                }
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
                invalidateOptionsMenu();
                syncState();
            }

            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
                invalidateOptionsMenu();
                syncState();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setTitle(getString(R.string.app_name));
        mDrawerToggle.syncState();

    }

    /**
     * Initialize and shows a Dialog with impressum text and FhG Logo
     */
    private void showImpressumDialog() {
        impressumDialog = new Dialog(MainActivity.this);
        impressumDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        impressumDialog.setContentView(R.layout.dialog_impressum);
        impressumBack = (Button) impressumDialog.findViewById(R.id.return_Main);
        impressumBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                impressumDialog.dismiss();
            }
        });
        impressumDialog.show();
    }

    /**
     * Initialize and shows a Dialog with different questions before first use
     *
     * @param preference
     */
    private void showInitializeDialog(SharedPreferences preference) {

        editor = preference.edit();
        editor.putInt("intializeSide", 0);
        initializeDialog = new Dialog(MainActivity.this);
        initializeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        initializeDialog.setContentView(R.layout.dialog_initialize_welcome);
        initializeDialog.setCancelable(false);
        initializeDialog.setCanceledOnTouchOutside(false);

        spCar = (Spinner) initializeDialog.findViewById(R.id.spCar);
        ArrayAdapter<CharSequence> dataAdapter;
        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.cars, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCar.setAdapter(dataAdapter);

        spPowerClass = (Spinner) initializeDialog.findViewById(R.id.spPowerClass);
        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.powerclass, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPowerClass.setAdapter(dataAdapter);

        spFuelType = (Spinner) initializeDialog.findViewById(R.id.spFuelType);
        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.fueltype, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFuelType.setAdapter(dataAdapter);

        spNumberDriver = (Spinner) initializeDialog.findViewById(R.id.spNumberDriver);
        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.numberstofive, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNumberDriver.setAdapter(dataAdapter);

        spNumberKilometer = (Spinner) initializeDialog.findViewById(R.id.spNumberKilometer);
        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.kilometer, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNumberKilometer.setAdapter(dataAdapter);

        spNumberDriving = (Spinner) initializeDialog.findViewById(R.id.spNumberDriving);
        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.numberdriving, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNumberDriving.setAdapter(dataAdapter);

        spContextDriving = (Spinner) initializeDialog.findViewById(R.id.spContextDriving);
        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.contextarray, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spContextDriving.setAdapter(dataAdapter);

        spCostsDriving = (Spinner) initializeDialog.findViewById(R.id.spCostsDriving);
        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.costsarray, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCostsDriving.setAdapter(dataAdapter);

        spCostsRelevance = (Spinner) initializeDialog.findViewById(R.id.spCostsRelevance);
        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.relevance, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCostsRelevance.setAdapter(dataAdapter);

        spEnvironmentRelevance = (Spinner) initializeDialog.findViewById(R.id.spEnvironmentalRelevance);
        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.relevance, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEnvironmentRelevance.setAdapter(dataAdapter);

        spSocialRelevance = (Spinner) initializeDialog.findViewById(R.id.spSocialRelevance);
        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.relevance, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSocialRelevance.setAdapter(dataAdapter);

        spGender = (Spinner) initializeDialog.findViewById(R.id.spGender);
        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.genderarray, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(dataAdapter);

        etAge = (EditText) initializeDialog.findViewById(R.id.etAge);
        etModel = (EditText) initializeDialog.findViewById(R.id.etModel);


        initializeNext = (Button) initializeDialog.findViewById(R.id.btNext);
        flipper = (ViewFlipper) initializeDialog.findViewById(R.id.viewFlipper);
        initializeNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flipper.getDisplayedChild() < flipper.getChildCount() - 1)
                    flipper.showNext();
                else {
                    if (checkData()) {
                        editor.putBoolean("firstUse", true);
                        editor.commit();
                        initializeDialog.dismiss();
                    } else
                        Toast.makeText(getApplicationContext(), "Bitte füllen Sie alle Felder aus!", Toast.LENGTH_SHORT).show();
                }


            }
        });
        initializePrevious = (Button) initializeDialog.findViewById(R.id.btLast);
        initializePrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flipper.getDisplayedChild() > 0)
                    flipper.showPrevious();
                else
                    Toast.makeText(getApplicationContext(), "Sie befinden sich am Anfang!", Toast.LENGTH_SHORT).show();
            }
        });
        initializeDialog.show();

    }

    private boolean checkData() {
        if (spCar.getSelectedItemId() != 0 &&
                spPowerClass.getSelectedItemId() != 0 &&
                spFuelType.getSelectedItemId() != 0 &&
                spNumberDriver.getSelectedItemId() != 0 &&
                spNumberKilometer.getSelectedItemId() != 0 &&
                spNumberDriving.getSelectedItemId() != 0 &&
                spContextDriving.getSelectedItemId() != 0 &&
                spCostsDriving.getSelectedItemId() != 0 &&
                spCostsRelevance.getSelectedItemId() != 0 &&
                spEnvironmentRelevance.getSelectedItemId() != 0 &&
                spSocialRelevance.getSelectedItemId() != 0 &&
                spGender.getSelectedItemId() != 0
                ) {
            String a = etAge.getText().toString().trim();
            String m = etModel.getText().toString().trim();
            if (a.isEmpty() || a.length() == 0 || a.equals("") || a.equals(" "))
                if (m.isEmpty() || m.length() == 0 || m.equals("") || m.equals(" "))
                    return false;
                else
                    return false;
            else
                return true;
        } else
            return false;

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                } else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(0);
        super.onDestroy();
    }
}
