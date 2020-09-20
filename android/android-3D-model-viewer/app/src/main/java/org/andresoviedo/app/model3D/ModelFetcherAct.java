package org.andresoviedo.app.model3D;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

import org.andresoviedo.app.model3D.view.ModelActivity;
import org.andresoviedo.dddmodel2.R;

import java.io.File;
import java.net.URI;

public class ModelFetcherAct extends AppCompatActivity {

    private Button btnFetcher;
    public static final String storageConnectionString = "DefaultEndpointsProtocol=https;"
        + "AccountName=bandersnatch;"
        + "AccountKey=bDGQUO0fk1fYqFQo82Km1BPwpijgJUNz5wmFAZ31U96eVqsfZNBabvzXeSqa33jqgmQPsul3PpjFNzTw2TrV7A==";

    private String[] pathArray = new String[10];
    private int count = 0;
    private int downloadCount = 0;
    private String objPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_fetcher);

        btnFetcher = findViewById(R.id.btn_fetch_modal);
        btnFetcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchBlob().execute();
            }
        });
    }

    class FetchBlob extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            count = 0;
            downloadCount = 0;

            Log.d("ZlTag", "inside doInBackground");
            try {
                CloudStorageAccount account = CloudStorageAccount
                    .parse(storageConnectionString);

                // Create a blob service client
                CloudBlobClient blobClient = account.createCloudBlobClient();

                // Get a reference to a container
                // The container name must be lower case
                // Append a random UUID to the end of the container name so that
                // this sample can be run more than once in quick succession.
                CloudBlobContainer container = blobClient.getContainerReference("modal");

                // List the blobs in a container, loop over them and
                // output the URI of each of them
                for (ListBlobItem blobItem : container.listBlobs()) {
//                    act.outputText(view, blobItem.getUri().toString());
                    URI downloadUri = blobItem.getUri();
                    Log.d("ZlTag", "blobURI: " + downloadUri);
                    String[] segments = downloadUri.getPath().split("/");
                    String blobFileName = segments[segments.length - 1];
                    Log.d("ZlTag", "blobNames: " + blobFileName);
                    pathArray[count++] = blobFileName;
                    DownloadFile(Uri.parse(downloadUri.toString()), blobFileName);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("ZlTag", "inside onPostExecute");
        }
    }

    private long DownloadFile(Uri downloadPath, String fileName) {
        //todo -> show loader
        long output = 0;
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(downloadPath);
        request.setTitle("AZURE FTW");
        request.setDescription("DOWNLOADING MODALS");
        //Setting the location to which the file is to be downloaded

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS.toString(), fileName);
        if (fileName.contains(".obj")) {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, fileName);

            objPath = file.getAbsolutePath();
            Log.d("ZlTag", "path: " + objPath);
//            downloadCount++;
        }
//        if (fileName.contains("mlt")) {
//            downloadCount++;
//        }
//        if (fileName.contains("png")) {
//            downloadCount++;
//        }

        if (downloadManager != null) {
            output = downloadManager.enqueue(request);
        } else {
            Toast.makeText(this, "Unable to download at the moment!", Toast.LENGTH_SHORT).show();
        }
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);
        return output;
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            // launch modal once all downloaded
            Log.d("ZlTag", "download success");
            if (downloadCount == 5) {
                // launch model with paths
                if (objPath != null) {
                    Log.d("ZlTag", "path:: " + objPath);
                    launchModelRendererActivity(objPath);
                }
            }
            downloadCount++;
        }
    };

    private void launchModelRendererActivity(String filename) {
        Log.i("Menu", "Launching renderer for '" + filename + "'");
        Intent intent = new Intent(getApplicationContext(), ModelActivity.class);
        Bundle b = new Bundle();
        b.putString("uri", filename);
        b.putString("immersiveMode", "true");
        intent.putExtras(b);
        startActivity(intent);
    }
}
