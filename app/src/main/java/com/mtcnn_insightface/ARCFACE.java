package com.mtcnn_insightface;

public class ARCFACE {

    //arcface的模型导入初始化
    public native boolean ArcFaceModelInit(String arcFaceModelPath);

    //通过图片和人脸信息计算两个人脸特征向量的相似度
    public native float CalcSimularity(byte[] imageData1, int imageWidth1 , int imageHeight1,
                                       int[] info1,
                                       byte[] imageData2, int imageWidth2 , int imageHeight2,
                                       int[] info2);

    //得到一个人脸的特征向量
    public native float[] GetFeature(byte[] imageData1, int imageWidth1 , int imageHeight1,
                                     int[] info1);

    //计算两个特征向量之间的相似度
    public native float NewCalcSimilarity(float[] feature1, float[] feature2);
    static {
        System.loadLibrary("facerecognition");
    }
}
