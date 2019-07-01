package com.sabbey.logintsf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.net.URL;

class DownLoadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView imageView;

    public DownLoadImageTask(ImageView imageView) {
        this.imageView = imageView;
    }

    /*
        doInBackground(Params... params)
            Override this method to perform a computation on a background thread.
     */
    protected Bitmap doInBackground(String... urls) {
        String urlOfImage = urls[0];
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlOfImage);
            Log.v("url", url.toString());
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());

        } catch (Exception e) { // Catch the download exception
            Log.d("error download", e.toString());
        }
        return bitmap;
    }

    /*
        onPostExecute(Result result)
            Runs on the UI thread after doInBackground(Params...).
     */
    protected void onPostExecute(Bitmap result) {

        imageView.setImageBitmap(result);
    }
}
