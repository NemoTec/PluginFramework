
package com.ryg.plugin.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.ryg.plugin.utils.LOG;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * so库文件拷贝过程封装
 * @author singwhatiwanna, modify Nemo.
 */
public final class SoLibManager {

    private static final String TAG = SoLibManager.class.getSimpleName();

    /**
     * single instance of the SoLoader
     */
    private static SoLibManager sInstance = new SoLibManager();
    /**
     * app's lib dir
     */
    private static String sNativeLibDir = "";

    private SoLibManager() {
    }

    /**
     * @return
     */
    public static SoLibManager getSoLoader() {
        return sInstance;
    }

    /**
     * get cpu name, according cpu type parse relevant so lib
     *
     * @return ARM、ARMV7、X86、MIPS
     */
    private String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            br.close();
            String[] array = text.split(":\\s+", 2);
            if (array.length >= 2) {
                return array[1];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressLint("DefaultLocale")
    private String getCpuArch(String cpuName) {
        String cpuArchitect = Constants.PROCESSOR_ARCH_ARMEABI;
        if (cpuName.toLowerCase().contains(Constants.PROCESSOR_LABEL_ARMEABI)) {
            cpuArchitect = Constants.PROCESSOR_ARCH_ARMEABI;
        } else if (cpuName.toLowerCase().contains(Constants.PROCESSOR_LABEL_ARM64)) {
        	cpuArchitect = Constants.PROCESSOR_ARCH_ARM64;
		} else if (cpuName.toLowerCase().contains(Constants.PROCESSOR_LABEL_X86)) {
            cpuArchitect = Constants.PROCESSOR_ARCH_X86;
        } else if (cpuName.toLowerCase().contains(Constants.PROCESSOR_LABEL_MIPS)) {
            cpuArchitect = Constants.PROCESSOR_ARCH_MIPS;
        }

        return cpuArchitect;
    }

    /**
     * copy so lib to specify directory(/data/data/host_pack_name/pluginlib)
     *
     * @param dexPath      plugin path
     * @param nativeLibDir nativeLibDir
     */
    public void copyPluginSoLib(Context context, String dexPath, String nativeLibDir) {
        String cpuName = getCpuName();
        String cpuArchitect = getCpuArch(cpuName);

        sNativeLibDir = nativeLibDir;
        LOG.d(TAG, "cpuArchitect: " + cpuArchitect);
        long start = System.currentTimeMillis();

        ExecutorService soExcutor = Executors.newCachedThreadPool();
        CompletionService<String> completionService = new ExecutorCompletionService<String>(soExcutor);
        
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(dexPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int copySoTaskCount = 0;
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                if (zipEntry.isDirectory()) {
                    continue;
                }
                String zipEntryName = zipEntry.getName();
                if (zipEntryName.endsWith(".so") && zipEntryName.contains(cpuArchitect)) {
                    final long lastModify = zipEntry.getTime();
                    if (lastModify == Configs.getSoLastModifiedTime(context, zipEntryName)) {
                        // exist and no change
                        LOG.d(TAG, "skip copying, the so lib is exist and not change: " + zipEntryName);
                        continue;
                    }
                    completionService.submit(new CopySoTask(context, zipFile, zipEntry, lastModify));
                    copySoTaskCount = copySoTaskCount + 1;
                }
            }
			for (int index = 0; index < copySoTaskCount; index++) {
				try {
					String thread_id = completionService.take().get();
					LOG.d(TAG, "copy thread finished: " + thread_id);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	soExcutor.shutdown();
        }

        long end = System.currentTimeMillis();
        LOG.d(TAG, "### copy so time : " + (end - start) + " ms");
    }

    private class CopySoTask implements Callable<String> {

        private String mSoFileName;
        private ZipFile mZipFile;
        private ZipEntry mZipEntry;
        private Context mContext;
        private long mLastModityTime;

        CopySoTask(Context context, ZipFile zipFile, ZipEntry zipEntry, long lastModify) {
            mZipFile = zipFile;
            mContext = context;
            mZipEntry = zipEntry;
            mSoFileName = parseSoFileName(zipEntry.getName());
            mLastModityTime = lastModify;
        }

        private final String parseSoFileName(String zipEntryName) {
            return zipEntryName.substring(zipEntryName.lastIndexOf("/") + 1);
        }

        private void writeSoFile2LibDir() throws IOException {
            InputStream is = null;
            FileOutputStream fos = null;
            is = mZipFile.getInputStream(mZipEntry);
            fos = new FileOutputStream(new File(sNativeLibDir, mSoFileName));
            copy(is, fos);
            //mZipFile.close();
        }

        /**
         * 输入输出流拷贝
         *
         * @param is
         * @param os
         */
        public void copy(InputStream is, OutputStream os) throws IOException {
            if (is == null || os == null)
                return;
            BufferedInputStream bis = new BufferedInputStream(is);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            int size = getAvailableSize(bis);
            byte[] buf = new byte[size];
            int i = 0;
            while ((i = bis.read(buf, 0, size)) != -1) {
                bos.write(buf, 0, i);
            }
            bos.flush();
            bos.close();
            bis.close();
        }

        private int getAvailableSize(InputStream is) throws IOException {
            if (is == null)
                return 0;
            int available = is.available();
            return available <= 0 ? 1024 : available;
        }

        @Override
        public String call() throws Exception {
            try {
                writeSoFile2LibDir();
                Configs.setSoLastModifiedTime(mContext, mZipEntry.getName(), mLastModityTime);
                LOG.d(TAG, "copy so lib success: " + mZipEntry.getName());
            } catch (IOException e) {
                LOG.e(TAG, "copy so lib failed: " + e.toString());
                e.printStackTrace();
            }
            return Thread.currentThread().getName();
        }

    }
}
