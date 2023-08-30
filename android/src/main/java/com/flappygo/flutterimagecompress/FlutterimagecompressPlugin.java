package com.flappygo.flutterimagecompress;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.flappygo.flutterimagecompress.Interface.PermissionListener;
import com.flappygo.flutterimagecompress.tools.ImageReadTool;
import com.flappygo.flutterimagecompress.tools.LXImageReadOption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterimagecompressPlugin
 */
public class FlutterimagecompressPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.RequestPermissionsResultListener {

    //permission
    private final int RequestPermissionCode = 1;

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;

    //context
    private Context context;

    //activity
    private Activity activity;

    //binding
    private ActivityPluginBinding activityPluginBinding;

    //listener
    private PermissionListener permissionListener;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.context = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutterimagecompress");
        channel.setMethodCallHandler(this);
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        activity = null;
        context = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        addBinding(binding);
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        addBinding(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onDetachedFromActivity() {
        removeBinding();
    }

    //add
    private void addBinding(ActivityPluginBinding binding) {
        if (activityPluginBinding != null) {
            activityPluginBinding.removeRequestPermissionsResultListener(this);
        }
        activity = binding.getActivity();
        activityPluginBinding = binding;
        activityPluginBinding.addRequestPermissionsResultListener(this);
    }

    //remove
    private void removeBinding() {
        if (activityPluginBinding != null) {
            activityPluginBinding.removeRequestPermissionsResultListener(this);
        }
        activity = null;
        activityPluginBinding = null;
    }


    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    public static void registerWith(Registrar registrar) {
        FlutterimagecompressPlugin plugin = new FlutterimagecompressPlugin();
        registrar.addRequestPermissionsResultListener(plugin);
        plugin.context = registrar.activity();
        plugin.activity = registrar.activity();
        plugin.channel = new MethodChannel(registrar.messenger(), "flutterimagecompress");
        plugin.channel.setMethodCallHandler(plugin);
    }


    @Override
    public void onMethodCall(@NonNull final MethodCall call, @NonNull final Result result) {
        //compressImage
        switch (call.method) {
            case "compressImage": {
                final String path = call.argument("path");
                final String savePath = call.argument("savePath");

                String quality = call.argument("quality");
                String maxWidth = call.argument("maxWidth");
                String maxHeight = call.argument("maxHeight");
                String maxSize = call.argument("maxSize");

                if (quality == null || quality.isEmpty()) {
                    quality = "90";
                }
                if (maxWidth == null || maxWidth.isEmpty()) {
                    maxWidth = "0";
                }
                if (maxHeight == null || maxHeight.isEmpty()) {
                    maxHeight = "0";
                }
                if (maxSize == null || maxSize.isEmpty()) {
                    maxSize = "0";
                }

                //handler
                final Handler handler = new Handler(Looper.getMainLooper()) {
                    public void handleMessage(Message message) {
                        if (message.what == 1) {
                            result.success(message.obj);
                        } else {
                            if (message.obj != null) {
                                Exception ex = (Exception) message.obj;
                                result.error("ERROR", ex.getMessage(), null);
                            }
                        }
                    }
                };
                final String finalMaxWidth = maxWidth;
                final String finalMaxHeight = maxHeight;
                final String finalMaxSize = maxSize;
                final String finalQuality = quality;
                new Thread() {
                    public void run() {
                        try {
                            Bitmap bitmap = ImageReadTool.readFileBitmap(
                                    path,
                                    new LXImageReadOption(Integer.parseInt(finalMaxWidth), Integer.parseInt(finalMaxHeight), Integer.parseInt(finalMaxSize))
                            );
                            String truePath = savePath;
                            if (truePath == null || truePath.isEmpty()) {
                                truePath = getCompressDefaultPath(context);
                            }
                            if (!truePath.endsWith("/")) {
                                truePath = truePath + "/";
                            }
                            File savePathFile = new File(truePath);
                            if (!savePathFile.exists()) {
                                if (!savePathFile.isDirectory()) {
                                    savePathFile.delete();
                                }
                                if (!savePathFile.mkdirs()) {
                                    Message message = handler.obtainMessage(0, new Exception("mkdirs failed"));
                                    handler.sendMessage(message);
                                    return;
                                }
                            }
                            String fileSaveName = System.currentTimeMillis() + getRandom(10000, 99999) + ".jpg";
                            String retPath = truePath + fileSaveName;
                            File file = new File(retPath);
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, Integer.parseInt(finalQuality), out);
                            out.flush();
                            out.close();
                            Message message = handler.obtainMessage(1, retPath);
                            handler.sendMessageDelayed(message, 200);
                        } catch (Exception e) {
                            Message message = handler.obtainMessage(0, e);
                            handler.sendMessage(message);
                        }
                    }
                }.start();
                break;
            }
            //get default compress path
            case "getCompressDefaultPath":
                result.success(getCompressDefaultPath(context));
                break;
            //get default cache path
            case "getCacheDefaultPath":
                result.success(getCacheDefaultPath(context));
                break;
            //save to cache
            case "saveImage": {
                final byte[] imageData = call.argument("imageData");
                final String savePath = call.argument("savePath");
                final String imageName = call.argument("imageName");
                //Handler
                final Handler handler = new Handler(Looper.getMainLooper()) {
                    public void handleMessage(Message message) {
                        if (message.what == 1) {
                            result.success(message.obj);
                        } else {
                            Exception ex = (Exception) message.obj;
                            result.error("ERROR", ex.getMessage(), null);
                        }
                    }
                };
                new Thread() {
                    public void run() {
                        try {
                            String truePath = savePath;
                            if (truePath == null || truePath.equals("")) {
                                truePath = getCompressDefaultPath(context);
                            }
                            if (!truePath.endsWith("/")) {
                                truePath = truePath + "/";
                            }
                            File savePathFile = new File(truePath);
                            if (!savePathFile.exists()) {
                                if (!savePathFile.isDirectory()) {
                                    savePathFile.delete();
                                }
                                if (!savePathFile.mkdirs()) {
                                    Message message = handler.obtainMessage(0, new Exception("mkdirs failed"));
                                    handler.sendMessage(message);
                                    return;
                                }
                            }
                            if (!savePathFile.isDirectory()) {
                                truePath = getCompressDefaultPath(context);
                                savePathFile = new File(truePath);
                                if (!savePathFile.exists()) {
                                    if (!savePathFile.mkdirs()) {
                                        Message message = handler.obtainMessage(0, new Exception("mkdirs failed"));
                                        handler.sendMessage(message);
                                        return;
                                    }
                                }
                            }
                            String retPath = truePath + imageName;
                            File file = new File(retPath);
                            FileOutputStream out = new FileOutputStream(file);
                            out.write(imageData);
                            out.flush();
                            out.close();
                            Message message = handler.obtainMessage(1, retPath);
                            handler.sendMessageDelayed(message, 200);
                        } catch (Exception exception) {
                            Message message = handler.obtainMessage(0, exception);
                            handler.sendMessage(message);
                        }
                    }
                }.start();
                break;
            }
            //save to photo
            case "saveImageToPhotos":
                checkPermission(new PermissionListener() {
                    @Override
                    public void result(boolean flag) {
                        try {
                            final byte[] imageData = call.argument("imageData");
                            assert imageData != null;
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                            MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, System.currentTimeMillis() + "", "");
                            result.success(null);
                        } catch (Exception ex) {
                            result.error("ERROR", ex.getMessage(), null);
                        }
                    }
                });
                break;
            default:
                result.notImplemented();
                break;
        }
    }


    ///获取随机数
    public static String getRandom(int startNum, int endNum) {
        if (endNum > startNum) {
            Random random = new Random();
            return random.nextInt(endNum - startNum) + startNum + "";
        }
        return "";
    }

    ///检查权限
    private void checkPermission(PermissionListener listener) {
        int hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            listener.result(true);
        } else {
            permissionListener = listener;
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestPermissionCode);
        }
    }

    ///默认的压缩目录
    public static String getCompressDefaultPath(Context context) {
        return getCacheDefaultPath(context) + "imageCache/";
    }

    ///默认的目录
    public static String getCacheDefaultPath(Context context) {
        String cachePath = "/";
        try {
            if (context.getExternalCacheDir() != null) {
                cachePath = context.getExternalCacheDir().getPath() + "/";
            } else if (context.getCacheDir() != null) {
                cachePath = context.getCacheDir().getPath() + "/";
            }
            return cachePath;
        } catch (Exception e) {
            return cachePath;
        }
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestPermissionCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //success
                if (permissionListener != null) {
                    permissionListener.result(true);
                    permissionListener = null;
                }
            } else {
                //failure
                if (permissionListener != null) {
                    permissionListener.result(false);
                    permissionListener = null;
                }
            }
        }
        return false;
    }
}
