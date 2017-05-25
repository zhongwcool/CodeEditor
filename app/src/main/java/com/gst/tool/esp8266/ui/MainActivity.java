package com.gst.tool.esp8266.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.gst.tool.esp8266.R;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public final static int MODE_FILE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This always works
                Intent i = new Intent(MainActivity.this, FilePickerActivity.class);

                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

                // Configure initial directory by specifying a String.
                // You could specify a String like "/storage/emulated/0/", but that can
                // dangerous. Always use Android's API calls to get paths to the SD-card or
                // internal memory.
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, MODE_FILE);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == MODE_FILE && resultCode == Activity.RESULT_OK) {
            if (!intent.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // The URI will now be something like content://PACKAGE-NAME/root/path/to/file
                Uri uri = intent.getData();
                // A utility method is provided to transform the URI to a File object
                File file = Utils.getFileForUri(uri);
                // If you want a URI which matches the old return value, you can do
                Uri fileUri = Uri.fromFile(file);
                // Do something with the result...
                Intent i = new Intent(MainActivity.this, EditorActivity.class);
                //Bundle bundle = new Bundle();
                //bundle.putSerializable(EditorActivity.FILE_KEY, file);
                i.putExtra(EditorActivity.FILE_KEY, file);
                startActivity(i);

            } else {
                // Handling multiple results is one extra step
                ArrayList<String> paths = intent.getStringArrayListExtra(FilePickerActivity.EXTRA_PATHS);
                if (paths != null) {
                    for (String path : paths) {
                        Uri uri = Uri.parse(path);
                        // Do something with the URI
                        File file = Utils.getFileForUri(uri);
                        // If you want a URI which matches the old return value, you can do
                        Uri fileUri = Uri.fromFile(file);
                        // Do something with the result...
                    }
                }
            }
        }
    }
}
