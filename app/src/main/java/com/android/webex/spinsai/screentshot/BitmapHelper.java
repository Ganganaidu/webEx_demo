package com.android.webex.spinsai.screentshot;

import android.graphics.Bitmap;

import com.android.webex.spinsai.models.FilePaths;

import java.util.ArrayList;
import java.util.Iterator;

public class BitmapHelper {

    private static BitmapHelper instance;

    private Bitmap localBView;
    private Bitmap remoteBView;
    public String imageHintByuser;

    private ArrayList<FilePaths> filePathList = new ArrayList<>();

    public synchronized static BitmapHelper getInstance() {
        if (instance == null) {
            instance = new BitmapHelper();
        }
        return instance;
    }

    Bitmap getLocalBView() {
        return localBView;
    }

    public void setLocalBView(Bitmap localBView) {
        this.localBView = localBView;
    }

    Bitmap getRemoteBView() {
        return remoteBView;
    }

    public void setRemoteBView(Bitmap remoteBView) {
        this.remoteBView = remoteBView;
    }

    public ArrayList<FilePaths> getFilePathList() {
        return filePathList;
    }

    void setFilePathList(FilePaths filePath) {
        filePath.setFileName(imageHintByuser);
        filePathList.add(filePath);
    }

    public String getImageHintByUser() {
        return imageHintByuser;
    }

    public void setImageHintByUser(String imageHintByUser) {
        this.imageHintByuser = imageHintByUser;
    }

    public void removeOldFile(FilePaths filePath) {
        Iterator itr = filePathList.iterator();
        while (itr.hasNext()) {
            FilePaths path = (FilePaths) itr.next();
            if (path.getFilePath().equalsIgnoreCase(filePath.getFilePath())) {
                itr.remove();
            }
        }
    }

    public void clear() {
        recycleBitmap(localBView);
        recycleBitmap(remoteBView);
    }

    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.recycle();
        }
    }
}
