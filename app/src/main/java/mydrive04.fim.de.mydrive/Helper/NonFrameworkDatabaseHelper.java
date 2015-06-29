package Helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import funf.time.TimeUtil;


/**
 * Created by Sebastian Heger, 2015-06-26.
 */
public class NonFrameworkDatabaseHelper extends SQLiteOpenHelper {
    private static Context mcontext;
    private static final String DATABASE_NAME = "myDrive"; // Name of myDrive database
    private static final int DATABASE_VERSION = 2;
    public static String TABLE_NAME_DRIVE = "drive";
    public static String TABLE_NAME_REFUEL = "refuel";
    public static String TABLE_NAME_FEEDBACK = "feedback";
    public static String TABLE_NAME_SOCIAL_DISTRIBUTION = "frequencydistribution";

    private String CREATE_DRIVE_TABLE = "" +
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_DRIVE + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "starttime TEXT NOT NULL, " +
            "endtime TEXT NOT NULL" + ");";
    private String CREATE_REFUEL_TABLE = "" +
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_REFUEL + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "date TEXT NOT NULL, " +
            "liter REAL NOT NULL, " +
            "kilometer INTEGER NOT NULL" +
            ");";

    private String CREATE_FEEDBACK_TABLE = "" +
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_FEEDBACK + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "date TEXT NOT NULL, " +
            "feedback TEXT NOT NULL, " +
            "literper100km REAL);";

    private String CREATE_FREQUENCY_DISTRIBUTION_TABLE = "" +
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_SOCIAL_DISTRIBUTION + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "fueltype INTEGER NOT NULL," +
            "performanceclass INTEGER NOT NULL," +
            "consumption REAL NOT NULL," +
            "quantity INTEGER NOT NULL);";

    public NonFrameworkDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mcontext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


    }

    public void createDB() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL(CREATE_DRIVE_TABLE);
            db.execSQL(CREATE_FEEDBACK_TABLE);
            db.execSQL(CREATE_REFUEL_TABLE);
            db.execSQL(CREATE_FREQUENCY_DISTRIBUTION_TABLE);
        } catch (Exception e) {
            Log.e("myDrive", "creating DB: " + e.getMessage());
        }


        String mCSVfile = "social_distribution.csv";
        InputStream instream = null;
        AssetManager manager = mcontext.getAssets();
        try {
            instream = manager.open(mCSVfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader buffer = new BufferedReader(new InputStreamReader(instream));
        String line = "";
        db.beginTransaction();
        try {
            while ((line = buffer.readLine()) != null) {
                String columns[] = line.split(";");
                if (columns.length != 5) {
                    Log.d("myDrive", "Skipping bad CSV row");
                    continue;
                }
                ContentValues cv = new ContentValues();
                cv.put("fueltype", columns[1].trim());
                cv.put("performanceclass", columns[2].trim());
                cv.put("consumption", columns[3].trim());
                cv.put("quantity", columns[4].trim());
                db.insert("frequencydistribution", null, cv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }

    public void insertRefuel(double liter, int kilometer) {
        String INSERT_REFUEL = "" +
                "INSERT INTO refuel (date, liter, kilometer) VALUES (" +
                "'" +
                TimeUtil.getTimestamp() +
                "', " + liter + ", " + kilometer + ");";
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL(INSERT_REFUEL);
            db.close();
        } catch (Exception e) {
            Log.e("myDrive", "Insert into Refuel: " + e.getMessage());
        }
    }

    public Cursor readRefuel() {
        String SELECT_REFUEL = "" +
                "SELECT * FROM refuel;";
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery(SELECT_REFUEL, null);
            if (c != null) {
                return c;
            } else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void insertFeedback(String feedback, double literper100km) {

        String INSERT_FEEDBACK = "" +
                "INSERT INTO feedback (date, feedback, literper100km) VALUES (" +
                "'" +
                TimeUtil.getTimestamp() +
                "', '" + feedback + "', " + literper100km + ");";
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL(INSERT_FEEDBACK);
            db.close();
        } catch (Exception e) {
            Log.e("myDrive", "Insert into Feedback: " + e.getMessage());
        }
    }

    public Cursor readFeedback() {
        String SELECT_FEEDBACK = "" +
                "SELECT * FROM feedback;";
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery(SELECT_FEEDBACK, null);
            if (c != null) {
                return c;
            } else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertDrive(String starttime) {
        String INSERT_DRIVE = "" +
                "INSERT INTO drive (starttime, endtime)VALUES (" +
                "'" + starttime + "', '" +
                TimeUtil.getTimestamp() + "'" +
                ");";
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL(INSERT_DRIVE);
            db.close();
        } catch (Exception e) {
            Log.e("myDrive", "Insert into Drive: " + e.getMessage());
        }
    }

    public SQLiteDatabase getReadableMyDriveDatabase() {
        return getReadableDatabase();
    }

    public double[][] getFrequencyDistribution(int fueltype, int performanceclass) {
        if (fueltype < 2 && performanceclass < 5) {
            SQLiteDatabase db = getReadableDatabase();
            String SELECT_DISTRIBUTION = "SELECT consumption, quantity FROM " + "frequencydistribution" + " WHERE fueltype == " +
                    fueltype + " AND performanceclass == " + performanceclass + ";";
            Cursor values = db.rawQuery(SELECT_DISTRIBUTION, null);
            values.moveToFirst();
            double distribution[][] = new double[values.getCount()][values.getColumnCount()];
            for (int i = 0; i < values.getCount(); i++) {
                for (int j = 0; j < values.getColumnCount(); j++) {
                    try {
                        distribution[i][j] = values.getDouble(j);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                values.moveToNext();
            }
            return distribution;
        } else
            return null;
    }


}
