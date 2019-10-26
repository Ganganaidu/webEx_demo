package com.android.webex.spinsai.screentshot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.webex.spinsai.R;
import com.android.webex.spinsai.models.FilePaths;
import com.android.webex.spinsai.ui.BaseActivity;
import com.squareup.picasso.Picasso;
import java.io.File;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SSDisplayActivity extends BaseActivity implements ScreenShot.PixelShotListener {

    @BindView(R.id.localView)
    ImageView localView;

    @BindView(R.id.remoteView)
    ImageView remoteView;

    @BindView(R.id.nameToSave)
    EditText nameToSave;

    @BindView(R.id.submit)
    Button submit;

    @BindView(R.id.save)
    Button save;

    @BindView(R.id.rootMediaLayout)
    FrameLayout rootMediaLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private boolean isDataSendingToServer;
    private FilePaths previousPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_shot);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Preview");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent extras = getIntent();
        String filePath = extras.getStringExtra(PathListDisplayActivity.FILEPATH);
        String fileName = extras.getStringExtra(PathListDisplayActivity.FILEPATHNAME);
        previousPath = new FilePaths(filePath, fileName);

        nameToSave.setText(fileName);

        if (filePath == null) {
            localView.setImageBitmap(BitmapHelper.getInstance().getLocalBView());
            remoteView.setImageBitmap(BitmapHelper.getInstance().getRemoteBView());
        } else {
            File file = new File(filePath);
            localView.setVisibility(View.GONE);

            Picasso.with(this)
                    .load(file)
                    .into(remoteView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.submit)
    public void onSubmitClick() {
        //send to server
        isDataSendingToServer = true;
        saveData();
    }

    @OnClick(R.id.save)
    public void onSaveClick() {
        //save local
        isDataSendingToServer = false;
        saveData();
    }

    @Override
    public void onPixelShotSuccess(String path) {
        //save this path
        if (isDataSendingToServer) {
            //send data to server
            finish();
            //delete file from system local
        } else {
            //save path list to display later in detail view
            BitmapHelper.getInstance().setFilePathList(new FilePaths(path, getEditFiledName()));
            finish();
        }
    }

    @Override
    public void onPixelShotFailed() {
        //Do nothing
    }

    @Override
    public void onReturnBitmapLocal(Bitmap bitmap) {
        //Do nothing
    }

    @Override
    public void onReturnBitmap(Bitmap bitmap) {
        //Do nothing
    }

    private void saveData() {
        deleteOldFile();
        saveEditFiled();

        ScreenShot.of(rootMediaLayout)
                .toJPG()
                .setResultListener(this)
                .save(false);
    }

    private void deleteOldFile() {
        if (previousPath != null) {
            ScreenShot.deleteFilePath(previousPath);
        }
    }

    private void saveEditFiled() {
        nameToSave.setVisibility(View.GONE);
        String name = getEditFiledName();
        if (name.isEmpty()) {
            name = "";
        }
        BitmapHelper.getInstance().setImageHintByUser(name);
    }

    public String getEditFiledName() {
        return nameToSave.getText().toString().trim();
    }
}
