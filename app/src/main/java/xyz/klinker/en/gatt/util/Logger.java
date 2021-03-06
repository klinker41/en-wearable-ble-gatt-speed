package xyz.klinker.en.gatt.util;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class Logger {

    private Activity activity;
    private String tag;
    private TextView logView;

    public Logger(Activity activity, String tag, TextView logView) {
        this.activity = activity;
        this.tag = tag;
        this.logView = logView;
    }

    public void v(String string) {
        Log.v(tag, string);
    }

    public void i(String string) {
        Log.i(tag, string);
        toLogView(string);
    }

    public void d(String string) {
        Log.d(tag, string);
        toLogView(string);
    }

    public void w(String string) {
        Log.w(tag, string);
        toLogView(string);
    }

    public void e(String string) {
        Log.e(tag, string);
        toLogView(string);
    }

    private void toLogView(String string) {
        activity.runOnUiThread(() ->
                logView.append(
                        String.format(
                                "\n%s: %s",
                                SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM)
                                        .format(System.currentTimeMillis()),
                                string)));

    }
}
