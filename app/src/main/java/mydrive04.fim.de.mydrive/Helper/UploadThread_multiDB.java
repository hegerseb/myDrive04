package Helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.IOUtils;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import funf.pipeline.BasicPipeline;
import funf.storage.NameValueDatabaseHelper;
import funf.util.StringUtil;

/**
 * Created by Sebastian Heger, 2015-06-01.
 */
public class UploadThread_multiDB extends AsyncTask <Context, String, Long> {

    private BasicPipeline pipeline;
    private SQLiteDatabase db_funf, db_myDrive;
    private SQLiteOpenHelper datahelper_funf;
    private NonFrameworkDatabaseHelper datahelper_myDrive;


    private ConnectivityManager cm;
    private Context context;
    private Drive service;

    private File sync_file;
    private Uri share_file;
    private File fconn; // public for sharing file when exiting
    private BufferedOutputStream os = null;
    private boolean at_least_once_written = false;
    private String currentFilename;

    private SharedPreferences settings;

    private int UID;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Long doInBackground(Context... _context) {
        context = _context[0];
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // IMEI of phone is used as unique identifier TODO do this on first application setup and save as preference
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String sUID = telephonyManager.getDeviceId();
        UID = sUID.hashCode();
        service = getDriveService();
        if(service!=null){
            try{
                // get Database
                datahelper_funf = new NameValueDatabaseHelper(context, StringUtil.simpleFilesafe("default"),1);
                db_funf = datahelper_funf.getReadableDatabase();
                datahelper_myDrive = new NonFrameworkDatabaseHelper(context);
                db_myDrive = datahelper_myDrive.getReadableMyDriveDatabase();
                publishProgress("Laeuft");
            }catch (Exception e){
                publishProgress("db Laeuft nich");
                Log.e("myDrive", "Cannot open myDrive database for upload!");
            }
            String[] tables = {NameValueDatabaseHelper.DATA_TABLE.name, NonFrameworkDatabaseHelper.TABLE_NAME_DRIVE,
                    NonFrameworkDatabaseHelper.TABLE_NAME_REFUEL, NonFrameworkDatabaseHelper.TABLE_NAME_FEEDBACK};


            for (int i=0; i<tables.length;i++){
                if(syncDrive(tables[i])==true){
                    publishProgress("Erfolgreicher Upload: " + tables[i]);
                }
            }



        }else
        publishProgress("Laeuft nicht");
        // Calls onPostExecute();
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
            }

    @Override
    protected void onPostExecute(Long l) {
        super.onPostExecute(l);

    }

    private boolean syncDrive(String tablename){
        com.google.api.services.drive.model.File body;
        com.google.api.services.drive.model.File file;
        File fileContent;
        FileContent mediaContent;
        Drive.Files.Insert insert;
        MediaHttpUploader uploader;
        boolean network = true, try_upload=true;
        //create syncfile
        if(createFile(context, tablename)==true){
            publishProgress("File erstellt");
            //try to upload until right network is available
            while(try_upload==true){
                try {
                    network = checkNetwork();

                    //try to upload only if network is available
                    if(network == true){
                        publishProgress("Netzwerk verfuegbar");
                        // file's binary content
                        fileContent = new File(share_file.getPath());
                        mediaContent = new FileContent("text/plain", fileContent);
                        Log.e("myDrive", "Condend: " + fileContent);

                        // File's metadata
                        body = new com.google.api.services.drive.model.File();
                        body.setTitle(fileContent.getName());
                        body.setMimeType("text/plain");

                        Log.v("myDrive","... trying to upload driving data");

                        // now get the uploader handle and set resumable upload
                        insert = service.files().insert(body, mediaContent);
                        uploader = insert.getMediaHttpUploader();// insert: size = 0;
                        uploader.setDirectUploadEnabled(false);
                        uploader.setChunkSize(MediaHttpUploader.DEFAULT_CHUNK_SIZE);
                        Log.v("myDrive","... executing upload driving data");

                        do{
                            network=checkNetwork();
                            if(network==true){
                                file = insert.execute();
                                insertPermission(service, file.getId());
                                if(file!=null){
                                    publishProgress("File hochgeladen");
                                    Log.v("myDrive", "... upload successful!");
                                    sync_file.delete();
                                    try_upload=false;
                                    AppSingleton.setUploading(false);
                                }
                            } else {
                                Log.v("myDrive","...sleeping until right network becomes available");

                                Waker.sleep(AppSingleton.getWaitForNetwork());
                            }
                        }while (network==false);

                    }else {
                        Log.v("myDrive","...sleeping until network becomes available");
                        publishProgress("Netzwerk NICHT verfuegbar");
                        Waker.sleep(AppSingleton.getWaitForNetwork());
                    }
                } catch (Exception e){
                    Log.e("myDrive", "somethin went wrong in uploading sync data: " + e.toString());
                    publishProgress("Exception");
                    AppSingleton.setUploading(false);
                }
            }
        return true;
        } else {
            Log.v("myStress", "...nothing to sync, it seems");

            AppSingleton.setUploading(false);
            return false;
        }
        // TODO: logikhandling

    }

    private boolean checkNetwork(){

        NetworkInfo netInfo;

        //check network connectivity
        netInfo = cm.getActiveNetworkInfo();
        //any network available?
        if(netInfo != null){
            return true;
        }
        else {return false;} //  network not available

    }

    private static Permission insertPermission(Drive service, String id) {

        Permission newPermission = new Permission();
        newPermission.setValue("fim.mydrive@gmail.com");
        newPermission.setType("user");
        newPermission.setRole("writer");
        try{
            return service.permissions()
                    .insert(id, newPermission)
                    .execute();

        }catch (Exception e){
            Log.e("myDrive", "Error on setting Permission for GDrive-File"+e.getMessage());
        }
        return null;
    }

    @SuppressLint("NewApi")
    public Drive getDriveService() {
        String SERVICE_ACCOUNT_EMAIL = "601842526103-8hkm6843p8p1pteiecvhf53695tv033j@developer.gserviceaccount.com";
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonfactory = new JacksonFactory();
        GoogleCredential credential;
        Drive mservice = null;
        List<String> scopes = Arrays.asList(DriveScopes.DRIVE);

        try{
            InputStream input = context.getAssets().open("key.p12");
            File tempFile = File.createTempFile("key","p12");
            tempFile.deleteOnExit();
            try(FileOutputStream out = new FileOutputStream(tempFile)){
                IOUtils.copy(input, out);
            }
            credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport).setJsonFactory(jsonfactory)
                    .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
                    .setServiceAccountScopes(scopes)
                    .setServiceAccountPrivateKeyFromP12File(tempFile).build();
            mservice = new Drive.Builder(httpTransport,jsonfactory,null)
                    .setApplicationName("myDrive")
                    .setHttpRequestInitializer(credential).build();

        }catch (Exception e){
            Log.e("myDrive", "Error on connecting GDrive: " + e.getMessage());
        }

        return mservice;
    }

    private boolean createFile(Context context, String tablename) {
        String query;
        int t_column, s_column, v_column;
        Cursor values=null;
        String value, sensor;
        String line_to_write = "";
        byte [] writebyte;
        int number_values = 0, number_columns = 0;
        int i;
        Calendar cal = Calendar.getInstance();
        boolean syncing = true;


        Log.v("myDrive","start creating vales sync file");
        //path for templates
        File external = context.getExternalFilesDir(null);
        if(external==null)
            return false;

        sync_file = new File(external, "myDrive_temp");

        //get files directory
        String [] fileList = sync_file.list(null);
        //remove files in myDrive_temp directory
        if(fileList != null){
            for(i=0;i<fileList.length;i++){
                File remove = new File(sync_file, fileList[i]);
                remove.delete();
            }
        }
        try{
            //open file in public directory
            sync_file = new File(external,"myDrive_temp");
            //make sure that path exists
            sync_file.mkdirs();
            // open file and create, if necessary
            //TODO: unique ID um Fahrzeug erweitern
            long d = cal.getTimeInMillis();
            currentFilename = String.valueOf(UID)+"_"+tablename+".txt";


            fconn = new File (sync_file, currentFilename);
            os = new BufferedOutputStream(new FileOutputStream(fconn,true));
            // print version code
            try{
                String version = "myDrive "+context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0).versionName+"\n";
                os.write(version.getBytes());
                os.flush();
            }catch (Exception e){ e.printStackTrace();}

            // build URI for sharing
            share_file = Uri.fromFile(fconn);
            while (syncing == true){
                query = new String ("" +
                        "SELECT * FROM " + tablename);
                // TODO: Zeitpunktabfrage einbauen --> Shared Preferences
                // TODO: Zeitbehandlung --> bis wann wurde bereits hochgeladen?
                if(tablename==NameValueDatabaseHelper.DATA_TABLE.name){
                    values = db_funf.rawQuery(query, null);
                }else{
                    values = db_myDrive.rawQuery(query, null);
                }


                // garbage collect
                query = null;

                if (values == null){
                    if(at_least_once_written==true)
                        return true;
                    else{
                        os.close();
                        return  false;
                    }
                }
                // get number of rows
                number_values = values.getCount();
                number_columns = values.getColumnCount();
                // if nothing is to read (anymore)
                if (number_values == 0){
                    if (at_least_once_written == true)
                        return true;
                    else {
                        os.close();
                        return  false;
                    }
                }


                if(number_columns<=0){
                    if(at_least_once_written==true)
                        return true;
                    else {
                        os.close();
                        return false;
                    }
                }
                Log.v("myDrive", "reading next batch");

                //move to first row to start
                values.moveToFirst();
                //write column names into upload file
                int j;
                for(j=0;j<number_columns;j++){
                    line_to_write = line_to_write + values.getColumnName(j) + ";";
                }
                line_to_write = line_to_write + "\n";
                writebyte = line_to_write.getBytes();
                os.write(writebyte,0,writebyte.length);
                os.flush();

                line_to_write = "";
                // write column values into files
                for (i=0;i<number_values; i++){
                    for (j=0; j<number_columns; j++){
                        line_to_write = line_to_write + values.getString(j) + ";";
                    }
                    line_to_write = line_to_write + "\n";
                    // now write to file
                    writebyte = line_to_write.getBytes();
                    os.write(writebyte,0,writebyte.length);
                    os.flush();
                    Log.e("myDrive", line_to_write);
                    // garbage collect the output data
                    writebyte = null;
                    line_to_write = "";

                    // now move to next row
                    values.moveToNext();

                } // FOR SCHLEIFE BEENDET
                if (number_values > 0)
                    at_least_once_written = true;

                // close cursor to empty space
                values.close();

                syncing = false;


            } // WHILE SCHLEIFE BEENDET

        }catch (Exception e){
            try {
                if (os != null){
                    os.close();
                }
            } catch (Exception ex) {}
        }
        // now return
        if (at_least_once_written == true)
            return true;
        else
            return false;

    }

}
