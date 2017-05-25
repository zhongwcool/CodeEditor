package com.gst.tool.esp8266.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gst.tool.esp8266.R;
import com.gst.tool.esp8266.listener.FileChangeListener;
import com.gst.tool.esp8266.listener.OnBottomReachedListener;
import com.gst.tool.esp8266.listener.OnScrollListener;
import com.gst.tool.esp8266.ui.component.CodeEditText;
import com.gst.tool.esp8266.ui.component.InteractiveScrollView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements TextWatcher {
    public static final String FILE_KEY = "FILE";
    private File file;
    private FileChangeListener fileChangeListener;

    private int CHUNK = 20000;
    private String FILE_CONTENT;
    private String currentBuffer;
    private StringBuilder loaded;

    private CodeEditText contentView;
    private View hidden;
    private InteractiveScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        file = (File) intent.getSerializableExtra(FILE_KEY);

        getSupportActionBar().setTitle(getFileName());

        contentView = (CodeEditText) findViewById(R.id.fileContent);
        hidden = findViewById(R.id.hidden);

        LinearLayout symbolLayout = (LinearLayout) findViewById(R.id.symbolLayout);
        View.OnClickListener symbolClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contentView.getText().insert(contentView.getSelectionStart(), ((TextView) view).getText().toString());
            }
        };
        for (int i = 0; i < symbolLayout.getChildCount(); i++) {
            symbolLayout.getChildAt(i).setOnClickListener(symbolClickListener);
        }

        if (file != null) {
            contentView.setVisibility(View.GONE);
            contentView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Consolas.ttf"));

            hidden.setVisibility(View.VISIBLE);
            scrollView = (InteractiveScrollView) findViewById(R.id.scrollView);
            scrollView.setOnBottomReachedListener(null);
            scrollView.setOnScrollListener((OnScrollListener) fileChangeListener);

            new DocumentLoader().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_undo: {
                Toast.makeText(this, "These feature is developing.", Toast.LENGTH_SHORT).show();
                return false;
            }
            case R.id.action_redo: {
                Toast.makeText(this, "These feature is developing.", Toast.LENGTH_SHORT).show();
                return false;
            }
            case R.id.action_publish: {
                Toast.makeText(this, "These feature is developing.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private String getFileSize() {
        String modifiedFileSize = null;
        double fileSize = 0.0;
        if (file.isFile()) {
            fileSize = (double) file.length();//in Bytes

            if (fileSize < 1024) {
                modifiedFileSize = String.valueOf(fileSize).concat("B");
            } else if (fileSize > 1024 && fileSize < (1024 * 1024)) {
                modifiedFileSize = String.valueOf(Math.round((fileSize / 1024 * 100.0)) / 100.0).concat("KB");
            } else {
                modifiedFileSize = String.valueOf(Math.round((fileSize / (1024 * 1204) * 100.0)) / 100.0).concat("MB");
            }
        } else {
            modifiedFileSize = "Unknown";
        }

        return modifiedFileSize;
    }

    public String getFilePath() {
        return file.getPath();
    }

    public String getFileName() {
        return file.getName();
    }

    public String getFileInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Size : " + getFileSize() + "\n");
        sb.append("Path : " + file.getPath() + "\n");

        return sb.toString();
    }

    public boolean isChanged() {
        if (FILE_CONTENT == null) {
            return false;
        }

        if (FILE_CONTENT.length() >= CHUNK && FILE_CONTENT.substring(0, loaded.length()).equals(currentBuffer))
            return false;
        else if (FILE_CONTENT.equals(currentBuffer))
            return false;

        return true;
    }

    private void loadInChunks(InteractiveScrollView scrollView, final String bigString) {
        loaded.append(bigString.substring(0, CHUNK));
        contentView.setTextHighlighted(loaded);
        scrollView.setOnBottomReachedListener(new OnBottomReachedListener() {
            @Override
            public void onBottomReached() {
                if (loaded.length() >= bigString.length())
                    return;
                else if (loaded.length() + CHUNK > bigString.length()) {
                    String buffer = bigString.substring(loaded.length(), bigString.length());
                    loaded.append(buffer);
                } else {
                    String buffer = bigString.substring(loaded.length(), loaded.length() + CHUNK);
                    loaded.append(buffer);
                }

                contentView.setTextHighlighted(loaded);
            }
        });
    }

    private void loadDocument(final String fileContent) {
        scrollView.smoothScrollTo(0, 0);

        contentView.setFocusable(false);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contentView.setFocusableInTouchMode(true);
            }
        });

        loaded = new StringBuilder();
        if (fileContent.length() > CHUNK)
            loadInChunks(scrollView, fileContent);
        else {
            loaded.append(fileContent);
            contentView.setTextHighlighted(loaded);
        }


        hidden.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
        contentView.addTextChangedListener(this);
        currentBuffer = contentView.getText().toString();

        if (isFileChangeListenerAttached()) fileChangeListener.onFileOpen();
    }

    public void save() {
        if (isChanged())
            new DocumentSaver().execute();
        else
            Toast.makeText(this, "No change in file", Toast.LENGTH_SHORT).show();
    }

    private void onPostSave() {
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();

        if (isFileChangeListenerAttached()) fileChangeListener.onFileSave();
    }

    private boolean isFileChangeListenerAttached() {
        return fileChangeListener != null;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                currentBuffer = contentView.getText().toString();

                if (isFileChangeListenerAttached()) fileChangeListener.onFileChanged(isChanged());
            }
        }, 1000);
    }

    private class DocumentLoader extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... paths) {

            try {
                BufferedReader br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
                try {
                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        sb.append("\n");
                        line = br.readLine();
                    }
                    FILE_CONTENT = sb.toString();
                    return FILE_CONTENT;
                } catch (IOException ioe) {
                } finally {
                    try {
                        br.close();
                    } catch (IOException ioe) {
                    }
                }
            } catch (FileNotFoundException fnfe) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            loadDocument(s);
        }
    }

    private class DocumentSaver extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            BufferedWriter output = null;
            String toSave = currentBuffer;
            try {
                output = new BufferedWriter(new FileWriter(file));
                if (FILE_CONTENT.length() > CHUNK) {
                    toSave = currentBuffer + FILE_CONTENT.substring(loaded.length(), FILE_CONTENT.length());
                }
                output.write(toSave);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                        FILE_CONTENT = toSave;
                    } catch (IOException ioe) {
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            onPostSave();
        }
    }
}
