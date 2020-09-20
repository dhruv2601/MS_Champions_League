package org.andresoviedo.app.model3D;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;

import org.andresoviedo.app.model3D.view.ModelActivity;
import org.andresoviedo.dddmodel2.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocalModalFetcherAct extends AppCompatActivity {

    //    private String ipv4Address = "192.168.29.10:5000";
    private String ipv4Address = "f8183edb.ngrok.io";

    private String upload = "executor";
    private String objDownload = "downloadObj";
    private String pngDownload = "downloadPng";
    private String mtlDownload = "downloadMtl";
    private SpinKitView spinKitView;

    String objDownloadURL = "http://" + ipv4Address + "/" + objDownload;
    String pngDownloadURL = "http://" + ipv4Address + "/" + pngDownload;
    String mtlDownloadURL = "http://" + ipv4Address + "/" + mtlDownload;

    private int downloadCount = 0;
    private String objPath = "";

    //    private String imageFilePath = "/sdcard/Download/image.jpg";
    private String imageFilePath = "/sdcard/DCIM/Camera/IMG_20200129_151958.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ZlTag", "inside onCreate");
        imageFilePath = getIntent().getExtras().getString("path");
        ipv4Address = getIntent().getExtras().getString("add") + ".ngrok.io";
        setContentView(R.layout.activity_local_modal_fetcher);
        Toast.makeText(this, "Building your 3D Face, this may take upto 30 seconds", Toast.LENGTH_LONG).show();
        spinKitView = findViewById(R.id.spin_kit);
        deleteExistingFiles();
        new UploadPhoto().execute();
    }

    class UploadPhoto extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            // update image file path from camera
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            String uploadURL = "http://" + ipv4Address + "/" + upload;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            }

            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath, options);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            byte[] byteArray = stream.toByteArray();

            RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "image.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

            // trigger progress bar
            postRequest(uploadURL, postBodyImage);
            Log.d("ZlTag", "postRequest");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("ZlTag", "PostExecute");
//            new ObjAssetDownloader().execute();
            super.onPostExecute(aVoid);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadReceiver != null) {
            downloadReceiver.abortBroadcast();
        }
    }


    class ObjAssetDownloader extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            // update image file path from camera
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            getRequest(objDownloadURL);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
//            new PngAssetDownloader().execute();
            super.onPostExecute(aVoid);
        }
    }

    class PngAssetDownloader extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            // update image file path from camera
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            String uploadURL = "http://" + ipv4Address + "/" + pngDownload;
            getRequest(uploadURL);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new MtlAssetDownloader().execute();
            super.onPostExecute(aVoid);
        }
    }

    class MtlAssetDownloader extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            // update image file path from camera
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            String uploadURL = "http://" + ipv4Address + "/" + mtlDownload;
            getRequest(uploadURL);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);
        }
    }


    void postRequest(String postUrl, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient();
        OkHttpClient.Builder b = new OkHttpClient.Builder();

        b.readTimeout(120000, TimeUnit.MILLISECONDS);
        b.writeTimeout(120000, TimeUnit.MILLISECONDS);
        client = b.build();

        Request request = new Request.Builder()
            .url(postUrl)
            .post(postBody)
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("ZlTag", "failure to connect " + e.toString());
                // Cancel the post on failure.
                call.cancel();

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Failed to connect to server -> show on activity
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

//                new ObjAssetDownloader().execute();
//                DownloadFile(Uri.parse(objDownloadURL), "image");
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DownloadFile(Uri.parse(objDownloadURL), "image.obj");
                        DownloadFile(Uri.parse(pngDownloadURL), "image_texture.png");
                        DownloadFile(Uri.parse(mtlDownloadURL), "image.mtl");
                    }
                }, 4000);
            }
        });
    }

    void getRequest(String postUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(postUrl)
            .get()
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("ZlTag", "failure to connect " + e.toString());
                // Cancel the post on failure.
                call.cancel();

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Failed to connect to server -> show on activity
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                Log.d("ZlTag", "success connect" + String.valueOf(response.code()));
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        });
    }


//    ---------------------------------

    private void deleteExistingFiles() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, "image.obj");
        if (file.exists()) {
            file.delete();
        }
        File file1 = new File(path, "image_texture.png");
        if (file1.exists()) {
            file1.delete();
        }
        File file2 = new File(path, "image.mtl");
        if (file2.exists()) {
            file2.delete();
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
            if (downloadCount == 2) {
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
