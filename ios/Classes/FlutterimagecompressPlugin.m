#import "FlutterimagecompressPlugin.h"
#import <Photos/Photos.h>

@implementation FlutterimagecompressPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"flutterimagecompress"
                                     binaryMessenger:[registrar messenger]];
    FlutterimagecompressPlugin* instance = [[FlutterimagecompressPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if([@"compressImage" isEqualToString:call.method]){
        //path
        NSString* path=(NSString*)call.arguments[@"path"];
        //savePath
        NSString* savePath=(NSString*)call.arguments[@"savePath"];


        //quality
        NSString* qualityStr=(NSString*)call.arguments[@"quality"];
        NSString* maxWidthStr=(NSString*)call.arguments[@"maxWidth"];
        NSString* maxHeightStr=(NSString*)call.arguments[@"maxHeight"];
        NSString* maxSizeStr=(NSString*)call.arguments[@"maxSize"];

        NSInteger quality=(qualityStr ==nil || qualityStr == [NSNull null] ) ? 90:[qualityStr integerValue];
        NSInteger maxWidth=(maxWidthStr ==nil || maxWidthStr == [NSNull null]) ? 0:[maxWidthStr integerValue];
        NSInteger maxHeight=(maxHeightStr ==nil|| maxHeightStr == [NSNull null]) ? 0:[maxHeightStr integerValue];
        NSInteger maxSize=(maxSizeStr ==nil|| maxSizeStr == [NSNull null]) ? 0:[maxSizeStr integerValue];

        //get path image
        UIImage* image=[[UIImage alloc]initWithContentsOfFile:path];

        //nil
        if(image == nil){
            result([FlutterError  errorWithCode:@"Can't get image from the path"
                                        message:@"Can't get image from the path"
                                        details:nil]);
            return;
        }

        //scaled
        UIImage* scaledImage=[self OriginImage:image
                                       andPath:path
                                       andSize:CGSizeMake(maxWidth, maxHeight)
                                       maxSize:maxSize];
        //quality
        double qualityDobule=quality*1.0/100;
        //quality
        if(qualityDobule>1.0){
            qualityDobule=1.0;
        }
        //data  scaled
        NSData *imageData = UIImageJPEGRepresentation(scaledImage, qualityDobule);
        //true image
        UIImage* trueImage=[UIImage imageWithData:imageData];
        //data
        long data=[[NSDate date] timeIntervalSince1970]*1000;
        //save path
        if(savePath==nil||[savePath isEqualToString:@""]){
            savePath=[self getCompressDefaultPath];
        }
        //must end with "/
        if(![savePath hasSuffix:@"/"]){
            savePath=[NSString stringWithFormat:@"%@/",savePath];
        }
        //dic exist or mot
        NSFileManager *fileManager = [NSFileManager defaultManager];
        BOOL isDir = FALSE;
        BOOL isDirExist = [fileManager fileExistsAtPath:savePath
                                            isDirectory:&isDir];

        if(isDirExist){
            if(!isDir){
                savePath=[self getCompressDefaultPath];
                [fileManager createDirectoryAtPath:savePath
                       withIntermediateDirectories:YES
                                        attributes:nil
                                             error:nil];

            }
        }else{
            [fileManager createDirectoryAtPath:savePath
                   withIntermediateDirectories:YES
                                    attributes:nil
                                         error:nil];
        }

        //true path
        NSString* truePath=[NSString stringWithFormat:@"%@%ld%@",savePath,data,@".jpg"];

        //save to path
        NSException* exception=[self saveToDocument:trueImage
                                       withFilePath:truePath];
        //success
        if(exception==nil){
            result(truePath);
        }else{
            result([FlutterError  errorWithCode:exception.name message:exception.reason details:nil]);
        }
    }
    //default path
    else if([@"getCompressDefaultPath" isEqualToString:call.method]){
        result([self getCompressDefaultPath]);
    }
    //default cache path
    else if([@"getCacheDefaultPath" isEqualToString:call.method]){
        result([self getCacheDefaultPath]);
    }
    //save to
    else if([@"saveImage" isEqualToString:call.method]){
        //image data
        FlutterStandardTypedData *imageData = call.arguments[@"imageData"];
        //image name
        NSString* imageName=(NSString*)call.arguments[@"imageName"];
        //savePath
        NSString* savePath=(NSString*)call.arguments[@"savePath"];
        //trueImage
        UIImage *trueImage  = [UIImage imageWithData:imageData.data];
        if(savePath==nil||[savePath isEqualToString:@""]){
            savePath=[self getCompressDefaultPath];
        }
        if(![savePath hasSuffix:@"/"]){
            savePath=[NSString stringWithFormat:@"%@/",savePath];
        }
        NSFileManager *fileManager = [NSFileManager defaultManager];
        BOOL isDir = FALSE;
        BOOL isDirExist = [fileManager fileExistsAtPath:savePath
                                            isDirectory:&isDir];
        if(isDirExist){
            if(!isDir){
                savePath=[self getCompressDefaultPath];
                [fileManager createDirectoryAtPath:savePath
                       withIntermediateDirectories:YES
                                        attributes:nil
                                             error:nil];

            }
        }else{
            [fileManager createDirectoryAtPath:savePath
                   withIntermediateDirectories:YES
                                    attributes:nil
                                         error:nil];
        }

        NSString* truePath=[NSString stringWithFormat:@"%@%@",savePath,imageName];

        //save
        NSException* exception=[self saveToDocument:trueImage
                                       withFilePath:truePath];
        if(exception==nil){
            result(truePath);
        }else{
            result([FlutterError  errorWithCode:exception.name message:exception.reason details:nil]);
        }
    }

    //save to phone
    else if ([@"saveImageToPhotos" isEqualToString:call.method]) {

        [self isCanVisitPhotoLibrary:^(BOOL status){
            if(status){
                FlutterStandardTypedData *imageData = call.arguments[@"imageData"];
                UIImage *trueImage  = [UIImage imageWithData:imageData.data];
                UIImageWriteToSavedPhotosAlbum(trueImage, self, @selector(image:didFinishSavingWithError:contextInfo:), NULL);
                result(nil);
            }else{
                result([FlutterError  errorWithCode:@"no permission"
                                            message:@"no permission to save photo"
                                            details:nil]);
            }
        }];

    }
    else {
        result(FlutterMethodNotImplemented);
    }
}



//save check
- (void)isCanVisitPhotoLibrary:(void(^)(BOOL))result {
    PHAuthorizationStatus status = [PHPhotoLibrary authorizationStatus];
    if (status == PHAuthorizationStatusAuthorized) {
        result(YES);
        return;
    }
    else{
        if (@available(iOS 14, *)) {
            [PHPhotoLibrary requestAuthorizationForAccessLevel:PHAccessLevelReadWrite
                                                       handler:^(PHAuthorizationStatus status) {
                NSLog(@"%@",[NSThread currentThread]);
                dispatch_async(dispatch_get_main_queue(), ^{
                    if (status != PHAuthorizationStatusAuthorized) {
                        NSLog(@"no permission to visit photos");
                        result(NO);
                        return ;
                    }
                    result(YES);
                });
            }];
        } else {
            [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
                NSLog(@"%@",[NSThread currentThread]);
                dispatch_async(dispatch_get_main_queue(), ^{
                    if (status != PHAuthorizationStatusAuthorized) {
                        NSLog(@"no permission to visit photos");
                        result(NO);
                        return ;
                    }
                    result(YES);
                });
            }];
        }
    }
}


//path size
- (long long) fileSizeAtPath:(NSString*) filePath{
    NSFileManager* manager = [NSFileManager defaultManager];
    if ([manager fileExistsAtPath:filePath]){
        return [[manager attributesOfItemAtPath:filePath error:nil] fileSize];
    }
    return 0;
}


//Origin iamge
-(UIImage*)OriginImage:(UIImage *)image
               andPath:(NSString*)path
               andSize:(CGSize)size
               maxSize:(NSInteger) maxSize{

    //width
    CGFloat width = size.width;
    CGFloat height = size.height;

    //max size
    if(maxSize!=0){
        long long fileSize = [self fileSizeAtPath:path];
        long kbSize = fileSize/1024;
        //scale down
        double scaleDown = kbSize/maxSize;
        double squareDown = sqrtf(scaleDown);
        //width
        width = (image.size.width/squareDown);
        height = (image.size.height/squareDown);
    }

    //image size
    if(image.size.width<width&&image.size.height<height){
        return image;
    }

    //size
    float xy=image.size.width/image.size.height;
    float xy2=width/height;
    if(xy>xy2){
        CGRect rect=CGRectMake(0, 0, (int)width, (int)(width/xy));
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(rect.size.width, rect.size.height), NO, 1.0);
        [image drawInRect:rect];
    }else{
        CGRect rect=CGRectMake(0, 0, (int)(height*xy), (int)height);
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(rect.size.width, rect.size.height), NO, 1.0);
        [image drawInRect:rect];
    }
    UIImage* scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return scaledImage;
}

//save to Document
-(NSException*)saveToDocument:(UIImage *) image
                 withFilePath:(NSString *) filePath{
    
    if (image == nil) {
        return [NSException exceptionWithName:@"ERROR" reason:@"image data is nil" userInfo:nil];
    }
    if (filePath == nil) {
        return [NSException exceptionWithName:@"ERROR" reason:@"image path is nil" userInfo:nil];
    }
    @try {
        NSData *imageData = nil;
        NSString *extention = [filePath pathExtension];
        if ([extention isEqualToString:@"png"]) {
            imageData = UIImagePNGRepresentation(image);
        }else{
            imageData = UIImageJPEGRepresentation(image, 0);
        }
        if (imageData == nil || [imageData length] <= 0) {
            return [NSException exceptionWithName:@"ERROR" reason:@"image compress error" userInfo:nil];
        }
        [imageData writeToFile:filePath atomically:YES];
        return  nil;
    }
    @catch (NSException *exception) {
        return  exception;
    }
}

//default cache compress path
-(NSString*)getCompressDefaultPath{
    NSArray * cachesPaths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString * cachesDirectory = [cachesPaths objectAtIndex:0];
    return [NSString stringWithFormat:@"%@/imageCache/",cachesDirectory];
}

//default cache path
-(NSString*)getCacheDefaultPath{
    NSArray * cachesPaths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString * cachesDirectory = [cachesPaths objectAtIndex:0];
    return [NSString stringWithFormat:@"%@/",cachesDirectory];
}

- (void)image: (UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo:(void *)contextInfo{
    if (image == nil) {
        return;
    }
    NSString *msg = @"save image success";
    if(error != NULL){
        msg = @"save image failed" ;
    }
    NSLog(@"%@",msg);
}


@end
