package com.komamitsu.android.msgpackexample;

import static org.msgpack.template.Templates.TString;
import static org.msgpack.template.Templates.tList;
import static org.msgpack.template.Templates.tMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.msgpack.MessagePack;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MsgpackExampleActivity extends Activity {
  public static final String TAG = "MsgpackExampleActivity ";
  private static final String TYPE_SERIALIZE = "serialize";
  private static final String TYPE_DESERIALIZE = "deserialize";
  private long start;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }

  @Override
  protected void onResume() {
    super.onResume();

    Executors.newSingleThreadExecutor().execute(new Runnable() {
      @Override
      public void run() {
        try {
          benchmarkJsonArray(50000);
          benchmarkJsonMap(30000);
          benchmarkMsgpackDynamicArray(50000);
          benchmarkMsgpackDynamicMap(30000);
        } catch (IOException e) {
          e.printStackTrace();
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void sleep() {
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private List<String> createList(int n) {
    List<String> src = new ArrayList<String>();
    for (int i = 0; i < n; i++) {
      src.add("0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
    }
    return src;
  }

  private Map<String, String> createMap(int n) {
    Map<String, String> src = new HashMap<String, String>();
    for (int i = 0; i < n; i++) {
      src.put(String.valueOf(i),
          "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
    }
    return src;
  }

  private void logStart(String methodName, String type) {
    start = System.currentTimeMillis();
    Log.i(TAG, methodName + ": " + type + " start");
  }

  private void logEnd(String methodName, String type) {
    Log.i(TAG, methodName + ": " + type + " end  :" + (System.currentTimeMillis() - start));
  }

  private void benchmarkMsgpackDynamicArray(int n) throws IOException {
    String methodName = "benchmarkMsgpackDynamicArray";
    List<String> src = createList(n);

    logStart(methodName, TYPE_SERIALIZE);
    MessagePack msgpack = new MessagePack();
    byte[] raw;
    raw = msgpack.write(src);
    logEnd(methodName, TYPE_SERIALIZE);

    logStart(methodName, TYPE_DESERIALIZE);
    List<String> dst = msgpack.read(raw, tList(TString));
    logEnd(methodName, TYPE_DESERIALIZE);
  }

  private void benchmarkMsgpackDynamicMap(int n) throws IOException {
    String methodName = "benchmarkMsgpackDynamicMap";
    Map<String, String> src = createMap(n);

    logStart(methodName, TYPE_SERIALIZE);
    MessagePack msgpack = new MessagePack();
    byte[] raw;
    raw = msgpack.write(src);
    logEnd(methodName, TYPE_SERIALIZE);

    logStart(methodName, TYPE_DESERIALIZE);
    Map<String, String> dst = msgpack.read(raw, tMap(TString, TString));
    logEnd(methodName, TYPE_DESERIALIZE);
  }

  private void benchmarkJsonArray(int n) throws JSONException {
    String methodName = "benchmarkJsonArray";
    List<String> src = createList(n);

    logStart(methodName, TYPE_SERIALIZE);
    String jsonString = new JSONArray(src).toString();
    logEnd(methodName, TYPE_SERIALIZE);

    logStart(methodName, TYPE_DESERIALIZE);
    JSONArray jsonArray = new JSONArray(jsonString);
    List<String> dst = new ArrayList<String>();
    for (int i = 0; i < jsonArray.length(); i++) {
      dst.add(jsonArray.getString(i));
    }
    Log.d(TAG, methodName + ": size=" + dst.size());
    logEnd(methodName, TYPE_DESERIALIZE);
  }

  private void benchmarkJsonMap(int n) throws JSONException {
    String methodName = "benchmarkJsonMap";
    Map<String, String> src = createMap(n);

    logStart(methodName, TYPE_SERIALIZE);
    String jsonString = new JSONObject(src).toString();
    logEnd(methodName, TYPE_SERIALIZE);

    logStart(methodName, TYPE_DESERIALIZE);
    JSONObject json = new JSONObject(jsonString);
    Map<String, String> dst = new HashMap<String, String>();
    Iterator keys = json.keys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      String value = json.getString(key);
      dst.put(key, value);
    }
    Log.d(TAG, methodName + ": size=" + dst.keySet().size());
    logEnd(methodName, TYPE_DESERIALIZE);
  }
}