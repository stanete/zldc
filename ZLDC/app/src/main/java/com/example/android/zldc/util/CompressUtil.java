package com.example.android.zldc.util;

import androidx.annotation.Size;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 压缩工具类。
 * gzip是将文件打包为tar.gz格式的压缩文件。
 * gzip只能对一个文件进行压缩，如果想压缩一大堆文件，就需要使用tar进行打包了。
 */
public class CompressUtil {

    private static final byte[] ZIP_HEADER_1 = new byte[]{0x1F, (byte) 0x8B, 0x03, 0x04};
    private static final byte[] ZIP_HEADER_2 = new byte[]{0x1F, (byte) 0x8B, 0x05, 0x06};
    private static final int BUFF_SIZE = 1024;

    /**
     * 判断文件是否压缩
     */
    public static boolean isCompressed(@Size(4) byte[] data) {
        if (data == null || data.length < ZIP_HEADER_1.length) return false;
        byte[] header = Arrays.copyOf(data, ZIP_HEADER_1.length);
        return Arrays.equals(header, ZIP_HEADER_1) || Arrays.equals(header, ZIP_HEADER_2);
    }

    /**
     * tar打包，GZip压缩
     *
     * @param file    待压缩的文件或文件夹
     * @param taos    压缩流
     * @param baseDir 相对压缩文件的相对路径
     */
    private static void tarGZip(File file, TarArchiveOutputStream taos, String baseDir) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                tarGZip(f, taos, baseDir + file.getName() + File.separator);
            }
        } else {
            byte[] buffer = new byte[BUFF_SIZE];
            int len = 0;
            FileInputStream fis = null;
            TarArchiveEntry tarArchiveEntry = null;
            try {
                fis = new FileInputStream(file);
                tarArchiveEntry = new TarArchiveEntry(baseDir + file.getName());
                tarArchiveEntry.setSize(file.length());
                taos.putArchiveEntry(tarArchiveEntry);
                while ((len = fis.read(buffer)) != -1) {
                    taos.write(buffer, 0, len);
                }
                taos.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) fis.close();
                    if (tarArchiveEntry != null) taos.closeArchiveEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * tar打包，GZip压缩
     *
     * @param srcFile 待压缩的文件或文件夹
     * @param dstDir  压缩至该目录，保持原文件名，后缀改为zip
     */
    public static void tarGZip(File srcFile, String dstDir) {
        File file = new File(dstDir);
        //需要判断该文件存在，且是文件夹
        if (!file.exists() || !file.isDirectory()) file.mkdirs();
        //先打包成tar格式
        String dstTarPath = dstDir + File.separator + srcFile.getName() + ".tar";
        String dstPath = dstTarPath + ".gz";

        FileOutputStream fos = null;
        TarArchiveOutputStream taos = null;
        try {
            fos = new FileOutputStream(dstTarPath);
            taos = new TarArchiveOutputStream(fos);
            tarGZip(srcFile, taos, "");
            taos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭数据流的时候要先关闭外层，否则会报Stream Closed的错误
                if (taos != null) taos.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File tarFile = new File(dstTarPath);
        fos = null;
        GZIPOutputStream gzip = null;
        FileInputStream fis = null;
        try {
            //再压缩成gz格式
            fos = new FileOutputStream(dstPath);
            gzip = new GZIPOutputStream(fos);
            fis = new FileInputStream(tarFile);
            int len = 0;
            byte[] buffer = new byte[BUFF_SIZE];
            while ((len = fis.read(buffer)) != -1) {
                gzip.write(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
                //关闭数据流的时候要先关闭外层，否则会报Stream Closed的错误
                if (gzip != null) gzip.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //删除生成的tar临时文件
        if (tarFile.exists()) tarFile.delete();
    }

    /**
     * GZip解压，tar解包
     *
     * @param srcFile 待压缩的文件或文件夹
     * @param dstDir  压缩至该目录，保持原文件名，后缀改为zip
     */
    public static void untarGZip(File srcFile, String dstDir) {
        File file = new File(dstDir);
        //需要判断该文件存在，且是文件夹
        if (!file.exists() || !file.isDirectory()) file.mkdirs();
        byte[] buffer = new byte[BUFF_SIZE];
        FileInputStream fis = null;
        GzipCompressorInputStream gcis = null;
        TarArchiveInputStream tais = null;
        try {
            fis = new FileInputStream(srcFile);
            gcis = new GzipCompressorInputStream(fis);
            tais = new TarArchiveInputStream(gcis);
            TarArchiveEntry tarArchiveEntry;
            int len = 0;
            while ((tarArchiveEntry = tais.getNextTarEntry()) != null) {
                File f = new File(dstDir + File.separator + tarArchiveEntry.getName());
                if (tarArchiveEntry.isDirectory()) f.mkdirs();
                else {
                    File parent = f.getParentFile();
                    if (!parent.exists()) parent.mkdirs();
                    FileOutputStream fos = new FileOutputStream(f);
                    while ((len = tais.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    fos.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
                //关闭数据流的时候要先关闭外层，否则会报Stream Closed的错误
                if (tais != null) tais.close();
                if (gcis != null) gcis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
