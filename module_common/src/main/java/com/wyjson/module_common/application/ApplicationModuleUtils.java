package com.wyjson.module_common.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.wyjson.module_common.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.DexFile;

class ApplicationModuleUtils {

    private static final String APPLICATION_PACKAGE = ".application";
    private static final String COMMON_APPLICATION = ".CommonApplication";
    private static final String SP_APPLICATION_CLASS_NAME_CACHE_NAME = "SP_APPLICATION_CLASS_NAME_CACHE";
    private static final String APPLICATION_CLASS_NAME_KEY_MAP = "APPLICATION_CLASS_NAME_MAP";

    private static String[] modulesClassNameArray;

    public static void loadApplicationModule(Application application, Consumer<IApplication> consumer) {
        if (modulesClassNameArray == null) {
            try {
                modulesClassNameArray = get(application);
            } catch (InterruptedException | IOException | PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        for (String className : modulesClassNameArray) {
            try {
                Class<?> clazz = Class.forName(className);
                Object obj = clazz.newInstance();
                if (obj instanceof IApplication) {
                    consumer.accept((IApplication) obj);
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getApplicationModulePackageName(Application application) {
        String packageName = application.getPackageName();
        return packageName.substring(0, packageName.lastIndexOf('.')) + APPLICATION_PACKAGE;
    }

    private synchronized static String[] get(Application context) throws InterruptedException, IOException, PackageManager.NameNotFoundException {
        Set<String> classNameMap;
        if (BuildConfig.DEBUG || isNewVersion(context)) {
            classNameMap = getFileNameByPackageName(context, getApplicationModulePackageName(context));
            if (!classNameMap.isEmpty()) {
                context.getSharedPreferences(SP_APPLICATION_CLASS_NAME_CACHE_NAME, Context.MODE_PRIVATE).edit().putStringSet(APPLICATION_CLASS_NAME_KEY_MAP, classNameMap).apply();
            }
            updateVersion(context);
        } else {
            classNameMap = new HashSet<>(context.getSharedPreferences(SP_APPLICATION_CLASS_NAME_CACHE_NAME, Context.MODE_PRIVATE).getStringSet(APPLICATION_CLASS_NAME_KEY_MAP, new HashSet<String>()));
        }

        String[] classNameList = classNameMap.toArray(new String[]{});
        /**
         * 把{@link COMMON_APPLICATION}移动到最前面,保证最先执行
         */
        for (int i = 0; i < classNameList.length; i++) {
            if (classNameList[i].equals(getApplicationModulePackageName(context) + COMMON_APPLICATION)) {
                if (i == 0)
                    break;
                String temp = classNameList[i];
                classNameList[i] = classNameList[0];
                classNameList[0] = temp;
                break;
            }
        }
        return classNameList;
    }

    private static final String LAST_VERSION_NAME = "LAST_VERSION_NAME";
    private static final String LAST_VERSION_CODE = "LAST_VERSION_CODE";
    private static String NEW_VERSION_NAME;
    private static int NEW_VERSION_CODE;

    private static boolean isNewVersion(Context context) {
        PackageInfo packageInfo = getPackageInfo(context);
        if (null != packageInfo) {
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;

            SharedPreferences sp = context.getSharedPreferences(SP_APPLICATION_CLASS_NAME_CACHE_NAME, Context.MODE_PRIVATE);
            if (!versionName.equals(sp.getString(LAST_VERSION_NAME, null)) || versionCode != sp.getInt(LAST_VERSION_CODE, -1)) {
                // new version
                NEW_VERSION_NAME = versionName;
                NEW_VERSION_CODE = versionCode;

                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private static void updateVersion(Context context) {
        if (!android.text.TextUtils.isEmpty(NEW_VERSION_NAME) && NEW_VERSION_CODE != 0) {
            SharedPreferences sp = context.getSharedPreferences(SP_APPLICATION_CLASS_NAME_CACHE_NAME, Context.MODE_PRIVATE);
            sp.edit().putString(LAST_VERSION_NAME, NEW_VERSION_NAME).putInt(LAST_VERSION_CODE, NEW_VERSION_CODE).apply();
        }
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return packageInfo;
    }

    private static Set<String> getFileNameByPackageName(Context context, final String packageName) throws PackageManager.NameNotFoundException, IOException, InterruptedException {
        final Set<String> classNames = new HashSet<>();

        List<String> paths = getSourcePaths(context);
        final CountDownLatch parserCtl = new CountDownLatch(paths.size());

        for (final String path : paths) {
            DefaultPoolExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    DexFile dexfile = null;

                    try {
                        if (path.endsWith(EXTRACTED_SUFFIX)) {
                            dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                        } else {
                            dexfile = new DexFile(path);
                        }

                        Enumeration<String> dexEntries = dexfile.entries();
                        while (dexEntries.hasMoreElements()) {
                            String className = dexEntries.nextElement();
                            if (className.startsWith(packageName) && !className.contains("$")) {
                                classNames.add(className);
                            }
                        }
                    } catch (Throwable ignore) {

                    } finally {
                        if (null != dexfile) {
                            try {
                                dexfile.close();
                            } catch (Throwable ignore) {
                            }
                        }

                        parserCtl.countDown();
                    }
                }
            });
        }

        parserCtl.await();
        return classNames;
    }

    static class DefaultPoolExecutor extends ThreadPoolExecutor {

        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        private static final int INIT_THREAD_COUNT = CPU_COUNT + 1;
        private static final int MAX_THREAD_COUNT = INIT_THREAD_COUNT;
        private static final long SURPLUS_THREAD_LIFE = 30L;

        private static volatile DefaultPoolExecutor instance;

        public static DefaultPoolExecutor getInstance() {
            if (null == instance) {
                synchronized (DefaultPoolExecutor.class) {
                    if (null == instance) {
                        instance = new DefaultPoolExecutor(
                                INIT_THREAD_COUNT,
                                MAX_THREAD_COUNT,
                                SURPLUS_THREAD_LIFE,
                                TimeUnit.SECONDS,
                                new ArrayBlockingQueue<Runnable>(64),
                                new DefaultThreadFactory());
                    }
                }
            }
            return instance;
        }

        private DefaultPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                }
            });
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
        }
    }

    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final String namePrefix;

        public DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "MyApplication task pool No." + poolNumber.getAndIncrement() + ", thread No.";
        }

        public Thread newThread(@NonNull Runnable runnable) {
            String threadName = namePrefix + threadNumber.getAndIncrement();
            Thread thread = new Thread(group, runnable, threadName, 0);
            if (thread.isDaemon()) {   //设为非后台线程
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) { //优先级为normal
                thread.setPriority(Thread.NORM_PRIORITY);
            }

            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                }
            });
            return thread;
        }
    }

    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";

    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";
    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";

    private static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences(PREFS_FILE, Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? Context.MODE_PRIVATE : Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    private static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        File sourceApk = new File(applicationInfo.sourceDir);

        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir);

        String extractedFilePrefix = sourceApk.getName() + EXTRACTED_NAME_EXT;

        if (!isVMMultidexCapable()) {
            int totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1);
            File dexDir = new File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME);

            for (int secondaryNumber = 2; secondaryNumber <= totalDexNumber; secondaryNumber++) {
                String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
                File extractedFile = new File(dexDir, fileName);
                if (extractedFile.isFile()) {
                    sourcePaths.add(extractedFile.getAbsolutePath());
                } else {
                    throw new IOException("Missing extracted secondary dex file '" + extractedFile.getPath() + "'");
                }
            }
        }

        if (BuildConfig.DEBUG) {
            sourcePaths.addAll(tryLoadInstantRunDexFile(applicationInfo));
        }
        return sourcePaths;
    }

    private static List<String> tryLoadInstantRunDexFile(ApplicationInfo applicationInfo) {
        List<String> instantRunSourcePaths = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && null != applicationInfo.splitSourceDirs) {
            instantRunSourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
        } else {
            try {
                Class pathsByInstantRun = Class.forName("com.android.tools.fd.runtime.Paths");
                Method getDexFileDirectory = pathsByInstantRun.getMethod("getDexFileDirectory", String.class);
                String instantRunDexPath = (String) getDexFileDirectory.invoke(null, applicationInfo.packageName);

                File instantRunFilePath = new File(instantRunDexPath);
                if (instantRunFilePath.exists() && instantRunFilePath.isDirectory()) {
                    File[] dexFile = instantRunFilePath.listFiles();
                    for (File file : dexFile) {
                        if (null != file && file.exists() && file.isFile() && file.getName().endsWith(".dex")) {
                            instantRunSourcePaths.add(file.getAbsolutePath());
                        }
                    }
                }

            } catch (Exception e) {
            }
        }

        return instantRunSourcePaths;
    }

    private static final int VM_WITH_MULTIDEX_VERSION_MAJOR = 2;
    private static final int VM_WITH_MULTIDEX_VERSION_MINOR = 1;

    private static boolean isVMMultidexCapable() {
        boolean isMultidexCapable = false;
        try {
            String versionString = System.getProperty("java.vm.version");
            if (versionString != null) {
                Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
                if (matcher.matches()) {
                    try {
                        int major = Integer.parseInt(matcher.group(1));
                        int minor = Integer.parseInt(matcher.group(2));
                        isMultidexCapable = (major > VM_WITH_MULTIDEX_VERSION_MAJOR)
                                || ((major == VM_WITH_MULTIDEX_VERSION_MAJOR)
                                && (minor >= VM_WITH_MULTIDEX_VERSION_MINOR));
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return isMultidexCapable;
    }

}
