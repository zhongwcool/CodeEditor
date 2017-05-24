package com.gst.tool.esp8266.listener;

public interface FileChangeListener {
    void onFileOpen();

    void onFileChanged(boolean save);

    void onFileSave();
}
