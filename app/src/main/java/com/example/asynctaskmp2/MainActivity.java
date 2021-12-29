package com.example.asynctaskmp2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int download_progress = 0;
    private Button btnDownload;
    private EditText etUrl;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etUrl = findViewById(R.id.etUrl);
        btnDownload = findViewById(R.id.btnDownload);


        btnDownload.setOnClickListener(this);


    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case download_progress:
                pd = new ProgressDialog(this);
                pd.setMessage("Downloading file...");
                pd.setIndeterminate(false);
                pd.setMax(100);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setCancelable(false);
                pd.show();
                return pd;
            default:
                return null;
        }
    }

    @Override
    public void onClick(View view) {
        String url = etUrl.getText().toString();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "Permission is Denied");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        } else {

            new DownloadImageAsyncTask().execute(url);

        }
    }

    class DownloadImageAsyncTask extends AsyncTask <String,String,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(download_progress);
        }

        protected String doInBackground(String... aurl) {

            int count;

            try {
                URL url = new URL(aurl[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int lenghOfFile = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                String fileName = connection.getHeaderField("Content-Disposition");
                if (fileName == null || fileName.length() < 1){
                    URL downloadUrl = connection.getURL();
                    fileName = downloadUrl.getFile();
                    fileName = fileName.substring(fileName.lastIndexOf("/")+1);
                } else {
                    fileName = URLDecoder.decode(fileName.substring(fileName.indexOf("filename=") + 9),
                            "UTF-8");
                    fileName = fileName.replaceAll("\"", "");
                }

                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/Download/" +fileName);


                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1){
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e){
                Log.e("Error : ", e.getMessage());
            }
            return null;
        }


        protected void onProgressUpdate (String...progress){
            pd.setProgress(Integer.parseInt(progress[0]));
        }

        protected void onPostExecute (String result){
            dismissDialog(download_progress);
            Toast.makeText(getApplicationContext(), "Download complete. File in /Download",
                    Toast.LENGTH_SHORT).show();

        }
    }
}