package com.android.webex.spinsai.screentshot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.android.webex.spinsai.R;
import com.android.webex.spinsai.models.FilePaths;
import com.android.webex.spinsai.screentshot.adapter.PathListAdapter;
import com.android.webex.spinsai.ui.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PathListDisplayActivity extends BaseActivity implements PathListAdapter.OnItemClickListener {

    public static final String FILEPATH = "FilePath";
    public static final String FILEPATHNAME = "FilePathName";

    @BindView(R.id.pathListRecycleView)
    RecyclerView pathListRecycleView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private PathListAdapter pathListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_list);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Preview List");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // use this setting to improve performance if you know that changes
        // in content do not change the menu_path size of the RecyclerView
        pathListRecycleView.setHasFixedSize(true);

        // use a linear menu_path manager
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        pathListRecycleView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        pathListAdapter = new PathListAdapter(this);
        pathListRecycleView.setAdapter(pathListAdapter);
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (pathListAdapter != null) {
            pathListAdapter.updateData(BitmapHelper.getInstance().getFilePathList());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onItemClick(FilePaths path) {
        Intent intent = new Intent(this, SSDisplayActivity.class);
        intent.putExtra(FILEPATH, path.getFilePath());
        intent.putExtra(FILEPATHNAME, path.getFileName());
        startActivity(intent);
    }
}
