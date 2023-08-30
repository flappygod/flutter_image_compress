import 'package:flutter/services.dart';
import 'dart:typed_data';
import 'dart:async';

//compress image
class Flutterimagecompress {
  ///channel
  static const MethodChannel _channel = const MethodChannel('flutterimagecompress');

  ///compress and saved to
  static Future<String?> compressImageToSavePath(String path, String savePath, int quality, int maxWidth, int maxHeight) async {
    //current path
    String currentPath = path;
    //file
    if (currentPath.startsWith("file://")) {
      currentPath = currentPath.replaceAll("file://", "");
    }
    //compress image
    final String? ret = await _channel.invokeMethod('compressImage', {
      "path": currentPath,
      "savePath": savePath,
      "quality": quality.toString(),
      "maxWidth": maxWidth.toString(),
      "maxHeight": maxHeight.toString(),
    });
    return ret;
  }

  ///compress image and return path
  static Future<String?> compressImage(String path, int quality, int maxWidth, int maxHeight) async {
    //file
    String currentPath = path;
    //file
    if (currentPath.startsWith("file://")) {
      currentPath = currentPath.replaceAll("file://", "");
    }
    //compress
    final String? ret = await _channel.invokeMethod('compressImage', {
      "path": currentPath,
      "savePath": "",
      "quality": quality.toString(),
      "maxWidth": maxWidth.toString(),
      "maxHeight": maxHeight.toString(),
    });
    return ret;
  }

  ///default compress path
  static Future<String?> getCompressDefaultPath() async {
    final String? ret = await _channel.invokeMethod('getCompressDefaultPath', {});
    return ret;
  }

  ///default cache path
  static Future<String?> getCacheDefaultPath() async {
    final String? ret = await _channel.invokeMethod('getCacheDefaultPath', {});
    return ret;
  }

  ///save image
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

  ///save image to photos
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
