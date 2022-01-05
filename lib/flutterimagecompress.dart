import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

//compress image
class Flutterimagecompress {
  //channel
  static const MethodChannel _channel = const MethodChannel('flutterimagecompress');

  //compress and saved to
  static Future<String?> compressImageToSavePath(String path, String savePath, int quality, int maxWidth, int maxHeight) async {
    //nower path
    String nowerPath = path;
    //file
    if (nowerPath.startsWith("file://")) {
      nowerPath = nowerPath.replaceAll("file://", "");
    }
    //compress image
    final String? ret = await _channel.invokeMethod('compressImage', {
      "path": nowerPath,
      "savePath": savePath,
      "quality": quality.toString(),
      "maxWidth": maxWidth.toString(),
      "maxHeight": maxHeight.toString(),
    });
    return ret;
  }

  //comprees image and return path
  static Future<String?> compressImage(String path, int quality, int maxWidth, int maxHeight) async {
    //file
    String nowerPath = path;
    //file
    if (nowerPath.startsWith("file://")) {
      nowerPath = nowerPath.replaceAll("file://", "");
    }
    //compress
    final String? ret = await _channel.invokeMethod('compressImage', {
      "path": nowerPath,
      "savePath": "",
      "quality": quality.toString(),
      "maxWidth": maxWidth.toString(),
      "maxHeight": maxHeight.toString(),
    });
    return ret;
  }

  //default compress path
  static Future<String?> getCompressDefaultPath() async {
    final String? ret = await _channel.invokeMethod('getCompressDefaultPath', {});
    return ret;
  }

  //default cache path
  static Future<String?> getCacheDefaultPath() async {
    final String? ret = await _channel.invokeMethod('getCacheDefaultPath', {});
    return ret;
  }

  //save image
  static Future<String?> saveImage(Uint8List imageData, String savePath, String imageName) async {
    try {
      String? path = await _channel.invokeMethod('saveImage', {
        "imageData": imageData,
        "imageName": imageName,
        "savePath": savePath,
      });
      return path;
    } on PlatformException {
      rethrow;
    }
  }

  //save image to photos
  static Future<bool> saveImageToPhotos(Uint8List imageData) async {
    try {
      await _channel.invokeMethod('saveImageToPhotos', {
        "imageData": imageData,
      });
      return true;
    } on PlatformException {
      rethrow;
    }
  }
}
