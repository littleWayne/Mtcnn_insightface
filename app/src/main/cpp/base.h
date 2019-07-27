#ifndef BASE_H
#define BASE_H
#include <cmath>
#include <cstring>
#include "net.h"

typedef struct FaceInfo {
    float score;
    int x[2]; //人脸框左上角和右下角的两个x坐标
    int y[2]; //人脸框左上角和右下角的两个y坐标
    float area;
    float regreCoord[4];
    int landmark[10]; //10个值从左到右分别对应5个关键点的x，y坐标，即x1，y1，x2，y2......格式
} FaceInfo;

ncnn::Mat resize(ncnn::Mat src, int w, int h);

ncnn::Mat bgr2rgb(ncnn::Mat src);

ncnn::Mat rgb2bgr(ncnn::Mat src);

void getAffineMatrix(float* src_5pts, const float* dst_5pts, float* M);

void warpAffineMatrix(ncnn::Mat src, ncnn::Mat &dst, float *M, int dst_w, int dst_h);

#endif
