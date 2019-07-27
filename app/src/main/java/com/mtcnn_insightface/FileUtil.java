package com.mtcnn_insightface;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class FileUtil {

    public static void newDirectory(String dirString){
        File file = new File(dirString);
        try {
            if (!file.exists()) {
                file.mkdir();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void deleteDirectory(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files == null) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        folder.delete();
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread); }
                inStream.close();
            }
        } catch (Exception e) {
            Log.i(TAG,"复制单个文件操作出错");
            e.printStackTrace();
        }
    }

    //todo,写一个函数，将1.jpg的特征向量写到某特定文件faculty1.txt之类的,鉴于数据量目前不大，先用博客最基础的方法实现
    //博客最后介绍了一个神速读写，如果以后要做人脸库的特征向量提取，就用博客提到的第三个方法
    //https://blog.csdn.net/Ctrl_qun/article/details/79360771
    public static void saveFloatToFile(float[] vectorFloat, String filePath) {
        FileWriter float_vector = null;
        try {
            float_vector = new FileWriter(filePath, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0;i < 128;i++) {
            try {
                float_vector.write(String.valueOf(vectorFloat[i]));
                float_vector.write(' ');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            float_vector.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            float_vector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveFloat1500ToFile(float[] vectorFloat, String filePath) {
        FileWriter float_vector = null;
        try {
            float_vector = new FileWriter(filePath, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0;i < 1500;i++) {
            try {
                float_vector.write(String.valueOf(vectorFloat[i]));
                float_vector.write(' ');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            float_vector.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            float_vector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //todo，写一个函数，从特征向量文件如faculty1.txt之类的读出某图片的特征向量
    public static float[] getFloatFromFile(String filePath, int lineFlag) {
        File file = new File(filePath);
        BufferedReader reader = null;
        float[] vector_float = new float[128];
        try {
            // 一次读一个字符
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            String float_num = "";
            int line_num = 1;
            int i = 0;
            while ((tempString = reader.readLine()) != null) {
                // 对于windows下，\r\n这两个字符在一起时，表示一个换行。
                // 但如果这两个字符分开显示时，会换两次行。
                // 因此，屏蔽掉\r，或者屏蔽\n。否则，将会多出很多空行。
                if (line_num == lineFlag) {
                    for (int j = 0; j < tempString.length(); j++) {
                        if (tempString.charAt(j) == ' ') {
                            vector_float[i] = Float.valueOf(float_num);
                            i += 1;
                            float_num = "";
                        } else {
                            float_num += tempString.charAt(j);
                        }
                    }
                    break;
                }
                line_num += 1;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vector_float;
    }

    public static int getLineOfTxt(String filePath){
        File file = new File(filePath);
        BufferedReader reader = null;
        int line_num = 0;
        try {
            // 一次读一个字符
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;

            while ((tempString = reader.readLine()) != null) {
                line_num += 1;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return line_num;
    }

    public static boolean fileIsExists(String strFile)
    {
        try
        {
            File f=new File(strFile);
            if(!f.exists())
            {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public static int getNumOfTxt(File file) {
        File[] files = file.listFiles();
        int count = 0;
        for (File f2 : files) {
            if (f2.isFile()) {
                if (f2.getName().endsWith(".txt")) {
                    //System.out.println(f2.getName());
                    count++;
                }
            }
        }
        return count;
    }

    public static int getNumOfDir(File file) {
        File[] files = file.listFiles();
        int count = 0;
        for (File f2 : files) {
            if (f2.isDirectory()) {
                count++;
            }
        }
        return count;
    }

    public static int getNumOfJpg(File file) {
        File[] files = file.listFiles();
        int count = 0;
        for (File f2 : files) {
            if (f2.isFile()) {
                if (f2.getName().endsWith(".jpg")) {
                    //System.out.println(f2.getName());
                    count++;
                }
            }
        }
        return count;
    }

    public static List<String> getPictures(final String strPath) {
        List<String> list = new ArrayList<String>();
        File file = new File(strPath);
        File[] allfiles = file.listFiles();
        if (allfiles == null) {
            return null;
        }
        for(int k = 0; k < allfiles.length; k++) {
            final File fi = allfiles[k];
            if(fi.isFile()) {
                int idx = fi.getPath().lastIndexOf(".");
                if (idx <= 0) {
                    continue;
                }
                String suffix = fi.getPath().substring(idx);
                if (suffix.toLowerCase().equals(".jpg") ||
                        suffix.toLowerCase().equals(".jpeg") ||
                        suffix.toLowerCase().equals(".bmp") ||
                        suffix.toLowerCase().equals(".png") ||
                        suffix.toLowerCase().equals(".gif") ) {
                    list.add(fi.getPath());
                }
            }
        }
        return list;
    }

    public static void copyFolder(String oldPath, String newPath){
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    //Log.i(TAG, "copyFolder: cannot create directory.");
                }
            }
            File oldFile = new File(oldPath);
            String[] files = oldFile.list();
            File temp;
            for (String file : files) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }
                if (temp.isDirectory()) {
                    //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (!temp.exists()) {
                    //Log.i(TAG, "copyFolder:  oldFile not exist.");
                } else if (!temp.isFile()) {
                    //Log.i(TAG, "copyFolder:  oldFile not file.");
                } else if (!temp.canRead()) {
                    //Log.i(TAG, "copyFolder:  oldFile cannot read.");
                } else {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } /* 如果不需要打log，可以使用下面的语句
                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (temp.exists() && temp.isFile() && temp.canRead()) {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                */
            }
        } catch (Exception e) { e.printStackTrace();
        }
    }
//        ---------------------
//                作者：奔跑的苍狼
//        来源：CSDN
//        原文：https://blog.csdn.net/u013642500/article/details/80067680
//        版权声明：本文为博主原创文章，转载请附上博文链接！


}

