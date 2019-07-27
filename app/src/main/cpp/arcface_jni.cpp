//
// Created by wayne on 19-4-25.
//
#include <android/bitmap.h>
#include <android/log.h>
#include <jni.h>
#include <string>
#include <vector>
#include <ctime>

// ncnn
#include "net.h"
#include "mat.h"

#include "base.h"
#include "arcface.h"
using namespace std;
#define TAG "FacedetectionSo"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
static Arcface *arc;
//sdk是否初始化成功
 bool detection_sdk_init_ok_ = false;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_mtcnn_1insightface_ARCFACE_ArcFaceModelInit(JNIEnv *env, jobject instance, jstring arcFaceModelPath_){
    LOGD("JNI开始特征提取模型初始化");
    //如果已初始化则直接返回
    if (detection_sdk_init_ok_) {
        //  LOGD("人脸检测模型已经导入");
        return true;
    }
    jboolean tRet = false;
    if (NULL == arcFaceModelPath_) {
        //   LOGD("导入的人脸检测的目录为空");
        return tRet;
    }

    //获取ArcFace模型的绝对路径的目录（不是/aaa/bbb.bin这样的路径，是/aaa/)
    const char *arcFaceModelPath = env->GetStringUTFChars(arcFaceModelPath_, 0);
    if (NULL == arcFaceModelPath) {
        return tRet;
    }

    string tArcFaceModelDir = arcFaceModelPath;
    string tLastChar = tArcFaceModelDir.substr(tArcFaceModelDir.length() - 1, 1);
    if ("\\" == tLastChar) {
        tArcFaceModelDir = tArcFaceModelDir.substr(0, tArcFaceModelDir.length() - 1) + "/";
    } else if (tLastChar != "/") {
        tArcFaceModelDir += "/";
    }
    LOGD("init, tArcFaceModelDir=%s", tArcFaceModelDir.c_str());

    arc = new Arcface(tArcFaceModelDir);

    env->ReleaseStringUTFChars(arcFaceModelPath_, arcFaceModelPath);
    detection_sdk_init_ok_ = true;
    tRet = true;
    return tRet;
}

JNIEXPORT jfloat JNICALL
Java_com_mtcnn_1insightface_ARCFACE_CalcSimularity(JNIEnv *env, jobject instance, jbyteArray imageData1_,
                                          jint imageWidth1, jint imageHeight1, jintArray info1_,
                                          jbyteArray imageData2_, jint imageWidth2,
                                          jint imageHeight2, jintArray info2_) {
    jbyte *imageData1 = env->GetByteArrayElements(imageData1_, NULL);
    if (NULL == imageData1){
        LOGD("导入数据为空，直接返回空");
        env->ReleaseByteArrayElements(imageData1_, imageData1, 0);
        return NULL;
    }
    unsigned char *featureImageCharData1 = (unsigned char*)imageData1;
    //没有给图片指定通道数是3还是4,可能产生问题
    ncnn::Mat ncnn_img1;
    ncnn_img1 = ncnn::Mat::from_pixels(featureImageCharData1, ncnn::Mat::PIXEL_RGBA2RGB, imageWidth1, imageHeight1);
    jbyte *imageData2 = env->GetByteArrayElements(imageData2_, NULL);
    if (NULL == imageData2){
        LOGD("导入数据为空，直接返回空");
        env->ReleaseByteArrayElements(imageData2_, imageData2, 0);
        return NULL;
    }
    unsigned char *featureImageCharData2 = (unsigned char*)imageData2;
    //没有给图片指定通道数是3还是4,可能产生问题
    ncnn::Mat ncnn_img2;
    ncnn_img2 = ncnn::Mat::from_pixels(featureImageCharData2, ncnn::Mat::PIXEL_RGBA2RGB, imageWidth2, imageHeight2);
    jint *info1 = env->GetIntArrayElements(info1_, NULL);
    jint *info2 = env->GetIntArrayElements(info2_, NULL);
    FaceInfo firstInfo;
    //firstInfo.score = 0;
    firstInfo.x[0] = info1[1];
    firstInfo.y[0] = info1[2];
    firstInfo.x[1] = info1[3];
    firstInfo.y[1] = info1[4];
    firstInfo.landmark[0] = info1[5];
    firstInfo.landmark[1] = info1[10];
    firstInfo.landmark[2] = info1[6];
    firstInfo.landmark[3] = info1[11];
    firstInfo.landmark[4] = info1[7];
    firstInfo.landmark[5] = info1[12];
    firstInfo.landmark[6] = info1[8];
    firstInfo.landmark[7] = info1[13];
    firstInfo.landmark[8] = info1[9];
    firstInfo.landmark[9] = info1[14];
    FaceInfo secondInfo;
    //secondInfo.score = 0;
    secondInfo.x[0] = info2[1];
    secondInfo.y[0] = info2[2];
    secondInfo.x[1] = info2[3];
    secondInfo.y[1] = info2[4];
    secondInfo.landmark[0] = info2[5];
    secondInfo.landmark[1] = info2[10];
    secondInfo.landmark[2] = info2[6];
    secondInfo.landmark[3] = info2[11];
    secondInfo.landmark[4] = info2[7];
    secondInfo.landmark[5] = info2[12];
    secondInfo.landmark[6] = info2[8];
    secondInfo.landmark[7] = info2[13];
    secondInfo.landmark[8] = info2[9];
    secondInfo.landmark[9] = info2[14];

    ncnn::Mat det1 = preprocess(ncnn_img1, firstInfo);
    ncnn::Mat det2 = preprocess(ncnn_img2, secondInfo);
    vector<float> feature1 = arc->getFeature(det1);
    vector<float> feature2 = arc->getFeature(det2);

    env->ReleaseByteArrayElements(imageData1_, imageData1, 0);
    env->ReleaseIntArrayElements(info1_, info1, 0);
    env->ReleaseByteArrayElements(imageData2_, imageData2, 0);
    env->ReleaseIntArrayElements(info2_, info2, 0);
    return calcSimilar(feature1, feature2);
}

JNIEXPORT jfloatArray JNICALL
Java_com_mtcnn_1insightface_ARCFACE_GetFeature(JNIEnv *env, jobject instance, jbyteArray imageData1_,
                                                   jint imageWidth1, jint imageHeight1, jintArray info1_) {
    jbyte *imageData1 = env->GetByteArrayElements(imageData1_, NULL);
    if (NULL == imageData1) {
        LOGD("导入数据为空，直接返回空");
        env->ReleaseByteArrayElements(imageData1_, imageData1, 0);
        return NULL;
    }
    unsigned char *featureImageCharData1 = (unsigned char *) imageData1;
    //没有给图片指定通道数是3还是4,可能产生问题
    ncnn::Mat ncnn_img1;
    ncnn_img1 = ncnn::Mat::from_pixels(featureImageCharData1, ncnn::Mat::PIXEL_RGBA2RGB,
                                       imageWidth1, imageHeight1);

    jint *info1 = env->GetIntArrayElements(info1_, NULL);
    FaceInfo firstInfo;
    //firstInfo.score = 0;
    //人脸框对应赋值
    firstInfo.x[0] = info1[1];
    firstInfo.y[0] = info1[2];
    firstInfo.x[1] = info1[3];
    firstInfo.y[1] = info1[4];
    //五个关键点坐标对应赋值
    firstInfo.landmark[0] = info1[5];
    firstInfo.landmark[1] = info1[10];
    firstInfo.landmark[2] = info1[6];
    firstInfo.landmark[3] = info1[11];
    firstInfo.landmark[4] = info1[7];
    firstInfo.landmark[5] = info1[12];
    firstInfo.landmark[6] = info1[8];
    firstInfo.landmark[7] = info1[13];
    firstInfo.landmark[8] = info1[9];
    firstInfo.landmark[9] = info1[14];

    LOGD("预处理开始：");
    clock_t preprocesstime = clock();
    ncnn::Mat det1 = preprocess(ncnn_img1, firstInfo);
    preprocesstime = clock() - preprocesstime;
    LOGD("预处理结束：%ld", preprocesstime);

    LOGD("提特征开始：");
    clock_t getfeaturetime = clock();
    vector<float> feature1 = arc->getFeature(det1);
    getfeaturetime = clock() - getfeaturetime;
    LOGD("提特征结束：%ld", getfeaturetime);

    float *feature = new float[128];
    vector<float>::iterator it;
    int i = 0;
    for(it=feature1.begin();it!=feature1.end();it++) {
        feature[i] = *it;
        i += 1;
    }

    jfloatArray tFeature = env->NewFloatArray(128);
    env->SetFloatArrayRegion(tFeature,0,128,feature);
    delete[] feature;

    env->ReleaseByteArrayElements(imageData1_, imageData1, 0);
    env->ReleaseIntArrayElements(info1_, info1, 0);

    return tFeature;
}

JNIEXPORT jfloat JNICALL
        Java_com_mtcnn_1insightface_ARCFACE_NewCalcSimilarity(JNIEnv *env, jobject instance,
                                                              jfloatArray feature1_,
                                                              jfloatArray feature2_) {
    jfloat *feature1 = env->GetFloatArrayElements(feature1_, NULL);
    vector<float> Feature1;
    for (int i=0;i<128;i++)
        Feature1.push_back(feature1[i]);
    jfloat *feature2 = env->GetFloatArrayElements(feature2_, NULL);
    vector<float> Feature2;
    for (int i=0;i<128;i++)
        Feature2.push_back(feature2[i]);
    env->ReleaseFloatArrayElements(feature1_, feature1, 0);
    env->ReleaseFloatArrayElements(feature2_, feature2, 0);
    return calcSimilar(Feature1, Feature2);
}

}
