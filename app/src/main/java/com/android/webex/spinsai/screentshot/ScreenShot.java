package com.android.webex.spinsai.screentshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.android.webex.spinsai.models.FilePaths;
import com.android.webex.spinsai.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class ScreenShot {

    private static final String TAG = ScreenShot.class.getSimpleName();
    private static final String EXTENSION_JPG = ".jpg";
    private static final String EXTENSION_PNG = ".png";
    private static final String EXTENSION_NOMEDIA = ".nomedia";
    private static final int JPG_MAX_QUALITY = 100;

    private String path = Environment.DIRECTORY_PICTURES;
    private String filename = String.valueOf(System.currentTimeMillis());
    private String fileExtension = EXTENSION_JPG;
    private int jpgQuality = JPG_MAX_QUALITY;

    private PixelShotListener listener;
    private View view;

    private ScreenShot(@NonNull View view) {
        this.view = view;
    }

    public static ScreenShot of(@NonNull View view) {
        return new ScreenShot(view);
    }

    public ScreenShot setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public ScreenShot setPath(String path) {
        this.path = path;
        return this;
    }

    public ScreenShot setResultListener(PixelShotListener listener) {
        this.listener = listener;
        return this;
    }

    ScreenShot toJPG() {
        jpgQuality = JPG_MAX_QUALITY;
        setFileExtension(EXTENSION_JPG);
        return this;
    }

    public ScreenShot toJPG(int jpgQuality) {
        this.jpgQuality = jpgQuality;
        setFileExtension(EXTENSION_JPG);
        return this;
    }

    public ScreenShot toPNG() {
        setFileExtension(EXTENSION_PNG);
        return this;
    }

    public ScreenShot toNomedia() {
        setFileExtension(EXTENSION_NOMEDIA);
        return this;
    }

    /**
     * @throws NullPointerException If View is null.
     */

    public void save(boolean isLocalView) throws NullPointerException {

        if (!Utils.isStorageReady()) {
            throw new IllegalStateException("Storage was not ready for use");
        }
        if (!Utils.isPermissionGranted(getAppContext())) {
            throw new SecurityException("Permission WRITE_EXTERNAL_STORAGE is missing");
        }

        if (view instanceof SurfaceView) {
            PixelCopyHelper.getSurfaceBitmap((SurfaceView) view, new PixelCopyHelper.PixelCopyListener() {
                @Override
                public void onSurfaceBitmapReady(Bitmap surfaceBitmap) {
                    new BitmapSaver(getAppContext(), surfaceBitmap, isLocalView, listener).execute();
                }

                @Override
                public void onSurfaceBitmapError() {
                    Log.d(TAG, "Couldn't create a bitmap of the SurfaceView");
                    if (listener != null) {
                        listener.onPixelShotFailed();
                    }
                }
            });
        } else {
            new FileSaver(getAppContext(), getViewBitmap(), path, filename, fileExtension, jpgQuality, listener).execute();
        }
    }

    private void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    private Context getAppContext() {
        if (view == null) {
            throw new NullPointerException("The provided View was null");
        } else {
            return view.getContext().getApplicationContext();
        }
    }


    private Bitmap getViewBitmap() {
        Bitmap bitmap;
        if (view instanceof TextureView) {
            bitmap = ((TextureView) view).getBitmap();
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            canvas.setBitmap(null);
            return bitmap;
        } else {
            bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            canvas.setBitmap(null);
            return bitmap;
        }
    }


    public interface PixelShotListener {
        void onPixelShotSuccess(String path);

        void onPixelShotFailed();

        void onReturnBitmapLocal(Bitmap bitmap);

        void onReturnBitmap(Bitmap bitmap);
    }

    public static void deleteFileDir() {
        File directory = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_PICTURES);
        try {
            if (directory.exists()) {
                directory.deleteOnExit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void deleteFilePath(FilePaths path) {
        BitmapHelper.getInstance().removeOldFile(path);
        if (path.getFilePath() != null) {
            File file = new File(path.getFilePath());
            try {
                if (file.exists()) {
                    file.deleteOnExit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class BitmapSaver extends AsyncTask<Void, Void, Bitmap> {

        private final WeakReference<Context> weakContext;
        private Handler handler = new Handler(Looper.getMainLooper());
        private Bitmap bitmap;
        private PixelShotListener listener;
        private boolean isLocalView;

        BitmapSaver(Context context, Bitmap bitmap, boolean isLocalView, PixelShotListener listener) {
            this.weakContext = new WeakReference<>(context);
            this.bitmap = bitmap;
            this.listener = listener;
            this.isLocalView = isLocalView;
        }

        private void cancelTask() {
            cancel(true);
            if (listener != null) {
                handler.post(() -> listener.onPixelShotFailed());
            }
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap aVoid) {
            super.onPostExecute(aVoid);
            weakContext.clear();
            if (listener != null) {
                handler.post(() -> {
                    if (isLocalView) {
                        listener.onReturnBitmapLocal(bitmap);
                    } else {
                        listener.onReturnBitmap(bitmap);
                    }
                });
            }
        }
    }

    static class FileSaver extends AsyncTask<Void, Void, Void> implements MediaScannerConnection.OnScanCompletedListener {

        private final WeakReference<Context> weakContext;
        private Handler handler = new Handler(Looper.getMainLooper());
        private Bitmap bitmap;
        private String path;
        private String filename;
        private String fileExtension;
        private int jpgQuality;
        private PixelShotListener listener;
        private File file;

        FileSaver(Context context, Bitmap bitmap, String path, String filename, String fileExtension, int jpgQuality, PixelShotListener listener) {
            this.weakContext = new WeakReference<>(context);
            this.bitmap = bitmap;
            this.path = path;
            this.filename = filename;
            this.fileExtension = fileExtension;
            this.jpgQuality = jpgQuality;
            this.listener = listener;
        }

        private void cancelTask() {
            cancel(true);
            if (listener != null) {
                handler.post(() -> listener.onPixelShotFailed());
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            File directory = new File(Environment.getExternalStorageDirectory(), path);
            if (!directory.exists() && !directory.mkdirs()) {
                cancelTask();
                return null;
            }

            file = new File(directory, filename + fileExtension);
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                switch (fileExtension) {
                    case EXTENSION_JPG:
                        bitmap.compress(Bitmap.CompressFormat.JPEG, jpgQuality, out);
                        break;
                    case EXTENSION_PNG:
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                cancelTask();
            }

            bitmap.recycle();
            bitmap = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MediaScannerConnection.scanFile(weakContext.get(), new String[]{file.getPath()}, null, this);
            weakContext.clear();
        }

        @Override
        public void onScanCompleted(final String path, final Uri uri) {
            if (listener != null) {
                handler.post(() -> {
                    if (uri != null) {
                        Log.i(TAG, "Saved image to path: " + path);
                        Log.i(TAG, "Saved image to URI: " + uri);
                        listener.onPixelShotSuccess(path);
                    } else {
                        listener.onPixelShotFailed();
                    }
                });
            }
        }
    }
}
