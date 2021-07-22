package com.flappygo.flutterimagecompress;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
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
import java.util.Random;
import java.util.UUID;

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

    //请求
    private final int RequestPermissionCode = 1;

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    //上下文
    private Context context;


    //activity
    private Activity activity;
    //binding
    private ActivityPluginBinding activityPluginBinding;
    //监听
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

    //绑定关系
    private void addBinding(ActivityPluginBinding binding) {
        if (activityPluginBinding != null) {
            activityPluginBinding.removeRequestPermissionsResultListener(this);
        }
        activity = binding.getActivity();
        activityPluginBinding = binding;
        activityPluginBinding.addRequestPermissionsResultListener(this);
    }

    //移除关系
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
        //压缩图片
        if (call.method.equals("compressImage")) {
            //系统图片路径
            final String path = call.argument("path");
            //压缩后保存的路径
            final String savePath = call.argument("savePath");
            //数据
            final String quality = call.argument("quality");
            //宽度
            final String maxWidth = call.argument("maxWidth");
            //高度
            final String maxHeight = call.argument("maxHeight");
            //handler
            final Handler handler = new Handler() {
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
            new Thread() {
                public void run() {
                    //然后保存来着
                    try {
                        //读取图像
                        Bitmap bitmap = ImageReadTool.readFileBitmap(path,
                                new LXImageReadOption(
                                        Integer.parseInt(maxWidth),
                                        Integer.parseInt(maxHeight),
                                        false));

                        //保存的地址
                        String truePath = savePath;
                        //空的，默认
                        if (truePath == null || truePath.equals("")) {
                            truePath = getCompressDefaultPath(context);
                        }
                        //保存的地址没有斜杠，补足斜杠
                        if (!truePath.endsWith("/")) {
                            truePath = truePath + "/";
                        }
                        //创建文件夹
                        File savePathFile = new File(truePath);
                        //如果不存在
                        if (!savePathFile.exists()) {
                            savePathFile.mkdirs();
                        }
                        //如果不是真实的地址
                        if (!savePathFile.isDirectory()) {
                            truePath = getCompressDefaultPath(context);
                            savePathFile = new File(truePath);
                            if (!savePathFile.exists()) {
                                savePathFile.mkdirs();
                            }
                        }
                        //保存
                        String fileSaveName = System.currentTimeMillis() + getRandom(1000, 9999) + ".jpg";
                        //图像名称
                        String retPath = truePath + fileSaveName;
                        //返回地址
                        File file = new File(retPath);
                        //读取
                        FileOutputStream out = new FileOutputStream(file);
                        //压缩
                        bitmap.compress(Bitmap.CompressFormat.JPEG, Integer.parseInt(quality), out);
                        //刷入
                        out.flush();
                        //关闭
                        out.close();
                        //成功
                        Message message = handler.obtainMessage(1, retPath);
                        //发送消息
                        handler.sendMessageDelayed(message, 200);
                    } catch (Exception e) {
                        //失败
                        Message message = handler.obtainMessage(0, e);
                        handler.sendMessage(message);
                    }
                }
            }.start();
        }
        //返回默认压缩地址
        else if (call.method.equals("getCompressDefaultPath")) {
            //获取压缩缓存地址
            result.success(getCompressDefaultPath(context));
        }
        //返回默认的缓存地址
        else if (call.method.equals("getCacheDefaultPath")) {
            //默认缓存地址
            result.success(getCacheDefaultPath(context));
        }
        //保存图片到本地
        else if (call.method.equals("saveImage")) {
            final byte[] imageData = call.argument("imageData");
            //保存的路径
            final String savePath = call.argument("savePath");
            //保存的名称
            final String imageName = call.argument("imageName");
            //Handler
            final Handler handler = new Handler() {
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
                    //然后保存来着
                    try {
                        //保存的地址
                        String truePath = savePath;
                        //空的，默认
                        if (truePath == null || truePath.equals("")) {
                            truePath = getCompressDefaultPath(context);
                        }
                        //保存的地址没有斜杠，补足斜杠
                        if (!truePath.endsWith("/")) {
                            truePath = truePath + "/";
                        }
                        //创建文件夹
                        File savePathFile = new File(truePath);
                        //如果不存在
                        if (!savePathFile.exists()) {
                            savePathFile.mkdirs();
                        }
                        //如果不是真实的地址
                        if (!savePathFile.isDirectory()) {
                            truePath = getCompressDefaultPath(context);
                            savePathFile = new File(truePath);
                            if (!savePathFile.exists()) {
                                savePathFile.mkdirs();
                            }
                        }
                        //保存
                        String fileSaveName = imageName;
                        //图像名称
                        String retPath = truePath + fileSaveName;
                        //返回地址
                        File file = new File(retPath);
                        //读取
                        FileOutputStream out = new FileOutputStream(file);
                        //写入数据
                        out.write(imageData);
                        //刷入
                        out.flush();
                        //关闭
                        out.close();
                        //成功
                        Message message = handler.obtainMessage(1, retPath);
                        //发送消息
                        handler.sendMessageDelayed(message, 200);
                    } catch (Exception exception) {
                        //失败
                        Message message = handler.obtainMessage(0, exception);
                        handler.sendMessage(message);
                    }
                }
            }.start();
        }
        //保存图片到相册
        else if (call.method.equals("saveImageToPhotos")) {
            //先检查权限
            checkPermission(new PermissionListener() {
                @Override
                public void result(boolean flag) {
                    //保存图片到相册
                    try {
                        //数据
                        final byte[] imageData = call.argument("imageData");
                        //转换为bitmap
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                        //保存到相册
                        MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, System.currentTimeMillis() + "", "");
                        //默认缓存地址
                        result.success(null);
                    } catch (Exception ex) {
                        result.error("ERROR", ex.getMessage(), null);
                    }
                }
            });

        } else {
            result.notImplemented();
        }
    }

    public static String getRandom(int startNum, int endNum) {
        if (endNum > startNum) {
            Random random = new Random();
            return random.nextInt(endNum - startNum) + startNum + "";
        }
        return "";
    }

    //检查权限
    private void checkPermission(PermissionListener listener) {
        int hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //未获取权限，请求权限
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            listener.result(true);
        }
        //未获取权限，请求权限
        else {
            permissionListener = listener;
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestPermissionCode);
        }
    }

    //获取CompressPath
    public static String getCompressDefaultPath(Context context) {
        String compressPath = getCacheDefaultPath(context) + "imagecache/";
        return compressPath;
    }

    /*********
     * 获取默认的保存图片地址
     * @param context 上下文
     * @return
     */
    public static String getCacheDefaultPath(Context context) {
        //默认根目录
        String cachePath = "/";
        try {
            //取得缓存目录
            if (context.getExternalCacheDir() != null) {
                cachePath = context.getExternalCacheDir().getPath() + "/";
            }
            //没取到再取
            else if (context.getCacheDir() != null) {
                cachePath = context.getCacheDir().getPath() + "/";
            }
            return cachePath;
        } catch (Exception e) {
            return cachePath;
        }
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RequestPermissionCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意了权限申请
                if (permissionListener != null) {
                    permissionListener.result(true);
                    permissionListener = null;
                }
            } else {
                //用户拒绝了权限申请，建议向用户解释权限用途
                if (permissionListener != null) {
                    permissionListener.result(false);
                    permissionListener = null;
                }
            }
        }
        return false;
    }
}
