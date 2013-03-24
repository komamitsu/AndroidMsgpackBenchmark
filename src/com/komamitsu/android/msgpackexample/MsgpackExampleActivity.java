package com.komamitsu.android.msgpackexample;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.MapValue;
import org.msgpack.type.Value;
import org.msgpack.unpacker.BufferUnpacker;
import org.msgpack.unpacker.MessagePackUnpacker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MsgpackExampleActivity extends Activity {
  public static final String TAG = "MsgpackExampleActivity ";
  private static final String TYPE_REGISTER = "register";
  private static final String TYPE_SERIALIZE = "serialize";
  private static final String TYPE_DESERIALIZE = "deserialize";
  private long start;
  private static final String EXAMPLE_STRING_MINI =
      "0000000000111111111122222222223333333333444444444455555555556666666666777777777788888888889999999999";
  private static final String EXAMPLE_STRING;
  static {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      buf.append(EXAMPLE_STRING_MINI);
    }
    EXAMPLE_STRING = buf.toString();
  }
  private final CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder()
      .onMalformedInput(CodingErrorAction.REPORT)
      .onUnmappableCharacter(CodingErrorAction.REPORT);

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }

  static class TestRunner implements Runnable {
    MsgpackExampleActivity hoge;

    public TestRunner(MsgpackExampleActivity hoge) {
      this.hoge = hoge;
    }

    @Override
    public void run() {
      try {
        hoge.benchmarkMsgpackDynamicString(1);
        hoge.benchmarkMsgpackDynamicArray(10000);
        hoge.benchmarkMsgpackDynamicMap(10000);
        hoge.benchmarkJsonString();
        hoge.benchmarkJsonArray(10000);
        hoge.benchmarkJsonMap(10000);
        hoge.benchmarkMsgpackRegistry(1000);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    Executors.newCachedThreadPool().execute(new TestRunner(MsgpackExampleActivity.this));
    /*
    Executors.newSingleThreadExecutor().execute(new Runnable() {

      @Override
      public void run() {
        while (true) {
          Executors.newCachedThreadPool().execute(new TestRunner(MsgpackExampleActivity.this));
          Executors.newCachedThreadPool().execute(new TestRunner(MsgpackExampleActivity.this));
          Executors.newCachedThreadPool().execute(new TestRunner(MsgpackExampleActivity.this));
          Executors.newCachedThreadPool().execute(new TestRunner(MsgpackExampleActivity.this));
          Executors.newCachedThreadPool().execute(new TestRunner(MsgpackExampleActivity.this));
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            break;
          }
        }
      }
    });
        */
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
      src.add(EXAMPLE_STRING_MINI);
    }
    return src;
  }

  private Map<String, String> createMap(int n) {
    Map<String, String> src = new HashMap<String, String>();
    for (int i = 0; i < n; i++) {
      src.put(String.valueOf(i), EXAMPLE_STRING_MINI);
    }
    return src;
  }

  private void logStart(String methodName, String type) {
    start = System.currentTimeMillis();
    // Log.i(TAG, methodName + ": " + type + " start");
  }

  private void logEnd(String methodName, String type) {
    Log.i(TAG, methodName + ": " + type + " end  :" + (System.currentTimeMillis() - start));
  }

  private void benchmarkMsgpackDynamicString(int n) throws IOException {
    String methodName = "benchmarkMsgpackDynamicString";

    logStart(methodName, TYPE_SERIALIZE);
    MessagePack msgpack = new MessagePack();
    BufferPacker packer = msgpack.createBufferPacker();
    for (int i = 0; i < n; i++) {
      packer.write(EXAMPLE_STRING);
    }
    logEnd(methodName, TYPE_SERIALIZE);

    byte[] raw = packer.toByteArray();
    BufferUnpacker unpacker = msgpack.createBufferUnpacker(raw);

    logStart(methodName, TYPE_DESERIALIZE);
    for (int i = 0; i < n; i++) {
      String dst = unpacker.read(String.class);
      if (!dst.equals(EXAMPLE_STRING))
        throw new RuntimeException();
    }
    logEnd(methodName, TYPE_DESERIALIZE);
  }

  private void benchmarkMsgpackDynamicArray(int n) throws IOException {
    String methodName = "benchmarkMsgpackDynamicArray";
    List<String> src = createList(n);

    logStart(methodName, TYPE_SERIALIZE);
    MessagePack msgpack = new MessagePack();
    byte[] raw;
    raw = msgpack.write(src);
    logEnd(methodName, TYPE_SERIALIZE);

    MessagePackUnpacker unpacker = new MessagePackUnpacker(msgpack, new ByteArrayInputStream(raw));
    List<String> dst = new LinkedList<String>();
    logStart(methodName, TYPE_DESERIALIZE);
    ArrayValue arrayValue = msgpack.read(raw).asArrayValue();
    for (Value v : arrayValue) {
      String s = v.asRawValue().getString();
      dst.add(s);
      if (!s.equals(EXAMPLE_STRING_MINI))
        throw new RuntimeException();
    }
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
    Map<String, String> dst = new HashMap<String, String>();
    MapValue mapValue = msgpack.read(raw).asMapValue();
    for (Entry<Value, Value> kv : mapValue.entrySet()) {
      String s = kv.getValue().asRawValue().getString();
      dst.put(kv.getKey().asRawValue().getString(), s);
      if (!s.equals(EXAMPLE_STRING_MINI))
        throw new RuntimeException();
    }

    logEnd(methodName, TYPE_DESERIALIZE);
  }

  private void benchmarkJsonString() throws JSONException, UnsupportedEncodingException {
    String methodName = "benchmarkJsonString";

    logStart(methodName, TYPE_SERIALIZE);
    JSONObject json = new JSONObject();
    json.put("str", EXAMPLE_STRING);
    String jsonString = json.toString();
    logEnd(methodName, TYPE_SERIALIZE);

    byte[] raw = jsonString.getBytes("UTF-8");

    logStart(methodName, TYPE_DESERIALIZE);
    String jsonStr = new String(raw, "UTF-8");
    JSONObject dstJson = new JSONObject(jsonStr);
    String dst = dstJson.getString("str");
    logEnd(methodName, TYPE_DESERIALIZE);
  }

  private void benchmarkJsonArray(int n) throws JSONException, UnsupportedEncodingException {
    String methodName = "benchmarkJsonArray";
    List<String> src = createList(n);

    logStart(methodName, TYPE_SERIALIZE);
    String jsonString = new JSONArray(src).toString();
    logEnd(methodName, TYPE_SERIALIZE);

    byte[] jsonRaw = jsonString.getBytes();

    logStart(methodName, TYPE_DESERIALIZE);
    JSONArray jsonArray = new JSONArray(new String(jsonRaw, "UTF-8"));
    List<String> dst = new ArrayList<String>();
    for (int i = 0; i < jsonArray.length(); i++) {
      dst.add(jsonArray.getString(i));
    }
    Log.d(TAG, methodName + ": size=" + dst.size());
    logEnd(methodName, TYPE_DESERIALIZE);
  }

  private void benchmarkJsonMap(int n) throws JSONException, UnsupportedEncodingException {
    String methodName = "benchmarkJsonMap";
    Map<String, String> src = createMap(n);

    logStart(methodName, TYPE_SERIALIZE);
    String jsonString = new JSONObject(src).toString();
    logEnd(methodName, TYPE_SERIALIZE);

    byte[] jsonRaw = jsonString.getBytes();

    logStart(methodName, TYPE_DESERIALIZE);
    long bcstart = System.currentTimeMillis();
    String str = new String(jsonRaw, "UTF-8");
    Log.d(TAG, methodName + ": bcsize=" + jsonRaw.length + ", bctime=" + (System.currentTimeMillis() - bcstart));

    JSONObject json = new JSONObject(str);
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

  public static class Person {
    public int age;
    public String name;
    public String blood;
    public int money;
  }

  private void benchmarkMsgpackRegistry(int n) throws IOException {
    String methodName = "benchmarkMsgpackRegistry";

    MessagePack msgpack = new MessagePack();

    logStart(methodName, TYPE_REGISTER);
    for (int i = 0; i < n; i++) {
      msgpack.register(Person.class);
    }
    logEnd(methodName, TYPE_REGISTER);

    msgpack.register(Person.class);
    BufferPacker packer = msgpack.createBufferPacker();
    Person person = new Person();
    person.age = 23;
    person.name = "AAAAAAAAAAAAAAAAA ZZZZZZZZZZZZZZZZZZZZ";
    person.blood = "AB";
    person.money = 123456789;

    logStart(methodName, TYPE_SERIALIZE);
    for (int i = 0; i < n; i++) {
      packer.write(person);
    }
    logEnd(methodName, TYPE_SERIALIZE);

    byte[] raw = packer.toByteArray();
    BufferUnpacker unpacker = msgpack.createBufferUnpacker(raw);

    logStart(methodName, TYPE_DESERIALIZE);
    for (int i = 0; i < n; i++) {
      Person dst = unpacker.read(Person.class);
      if (dst.age != person.age)
        throw new RuntimeException();

      if (dst.money != person.money)
        throw new RuntimeException();

      if (!dst.name.equals(person.name))
        throw new RuntimeException();

      if (!dst.blood.equals(person.blood))
        throw new RuntimeException();
    }
    logEnd(methodName, TYPE_DESERIALIZE);
  }

}