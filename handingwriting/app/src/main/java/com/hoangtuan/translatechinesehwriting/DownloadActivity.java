package com.hoangtuan.translatechinesehwriting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hoangtuan.translatechinesehwriting.UnZip.Decompress;
import com.hoangtuan.translatechinesehwriting.Interface.PostDownload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DownloadActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;
    private AlertDialog alertInternet;
    static boolean blockInternet = false;
    private LinearLayout llThuLai;
    private Button btnThuLai;
//=====

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dowload);

        if (haveNetworkConnection()) {
            downloadAndUnzipContent();
        }

        llThuLai = (LinearLayout) findViewById(R.id.llThuLai);
        btnThuLai = (Button) findViewById(R.id.btnThuLai);

        btnThuLai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadAndUnzipContent();
            }
        });
//downloadAndUnzipContent();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    class Download extends AsyncTask<String, String, String> {
        private static final String TAG = "DOWNLOADFILE";

        private PostDownload callback;
        Context context;
        FileDescriptor fd;
        private File file;
        private String downloadLocation;

        public Download(String downloadLocation, Context context, PostDownload callback) {
            this.context = context;
            this.callback = callback;
            this.downloadLocation = downloadLocation;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);

        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;

            try {
                URL url = new URL(aurl[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept-Encoding", "identity"); // <--- Add this line
                connection.setRequestMethod("GET");
                int lenghtOfFile = connection.getContentLength();
                Log.d(TAG, "Length of the file: " + lenghtOfFile);

                InputStream input = new BufferedInputStream(url.openStream());
                file = new File(downloadLocation);
                FileOutputStream output = new FileOutputStream(file); //context.openFileOutput("content.zip", Context.MODE_PRIVATE);
                Log.d(TAG, "file saved at " + file.getAbsolutePath());
                fd = output.getFD();

                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
            Log.d("AAAAAAAAA", progress[0]);
        }

        @Override
        protected void onPostExecute(String unused) {

            if (callback != null) callback.downloadDone(file);

            Intent intent
                    = new Intent(DownloadActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void downloadAndUnzipContent() {
//        String url = "http://45.77.45.115:1234/AppNhandangChuvietTiengTrung/conf.zip";
        String url = "http://upfile.vn/download/guest/AbrCNCjmFCBC/-dLm6rBmACBC/TxGUE_aoyHGc/tQGKnZOo7WGU/fd90f26e1a4fa532d/1522987534/4182995de62252b5e87354e2bd258c9d486ea1175b7413342/hvt.zip";
        Download download = new Download("/sdcard/content.zip", this, new PostDownload() {
            @Override
            public void downloadDone(File file) {

                Decompress unzip = new Decompress(DownloadActivity.this, file);
                pDialog.setMessage("Bắt đầu giải nén");
                unzip.unzip();
                pDialog.setMessage("Giải nén thành công");


            }
        });
        if (haveNetworkConnection()) {
            download.execute(url);
        }

    }

    //CheckInternet
    private void buildAlertMessageNoInternet() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.thong_bao_internet))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.co), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                        blockInternet = true;
                    }
                })
                .setNegativeButton(getResources().getString(R.string.khong), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        alertInternet.cancel();
                    }
                });
        if (alertInternet == null) {
            alertInternet = builder.create();
            alertInternet.show();
        } else {
            if (!alertInternet.isShowing()) {
                alertInternet = builder.create();
                alertInternet.show();
            }
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    protected void onResume() {
        super.onResume();
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!blockInternet) {
                    if (!haveNetworkConnection()) {
                        buildAlertMessageNoInternet();
                        llThuLai.setVisibility(View.VISIBLE);
                    }
                }
                handler.postDelayed(this, 1000);
            }
        });

    }


}
