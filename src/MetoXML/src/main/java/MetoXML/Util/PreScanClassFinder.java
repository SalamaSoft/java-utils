package MetoXML.Util;

import MetoXML.Cast.BaseTypesMapping;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 *
 * @author XingGu Liu
 *
 */
public class PreScanClassFinder implements ClassFinder {
    protected HashMap<String, Class<?>> _classFullNameMap = new HashMap<String, Class<?>>();

    protected HashMap<String, Class<?>> _classNameMap = new HashMap<String, Class<?>>();

    public PreScanClassFinder() {
    }

    public void clearPreScannedClass() {
        _classFullNameMap.clear();
        _classNameMap.clear();
    }

    public void loadClassOfPackage(String scanBasePackage) {
        loadClasses(scanBasePackage, _classNameMap, _classFullNameMap);
    }

    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        Class<?> cls = null;

        cls = BaseTypesMapping.GetSupportedTypeByDisplayName(className);
        if(cls != null) {
            return cls;
        }

        cls = _classFullNameMap.get(className);
        if(cls != null) {
            return cls;
        }

        cls = _classNameMap.get(className);
        if(cls != null) {
            return cls;
        }

        cls = getDefaultClassLoader().loadClass(className);
        if(cls != null) {
            return cls;
        }

        return null;
    }

    protected void loadClasses(String packageName,
                               HashMap<String, Class<?>> classNameMap,
                               HashMap<String, Class<?>> classFullNameMap) {

        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> enumerPackageUrl;
        try {
            enumerPackageUrl = getDefaultClassLoader().getResources(packageDirName);
            URL url = null;
            String protocol = null;
            String filePath = null;

            while (enumerPackageUrl.hasMoreElements()) {
                url = enumerPackageUrl.nextElement();
                protocol = url.getProtocol();

                logDebug("loadClasses() url:" + url.toString());

                if ("file".equals(protocol)) {
                    filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    if(File.separatorChar != '/') {
                        //the url on windows is like: /D://xxxxxxxxxx
                        if(filePath.charAt(0) == '/') {
                            filePath = filePath.substring(1);
                        }

                        filePath = filePath.replace('/', File.separatorChar);
                    }

                    ClassFileFilter classFilieFilter = new ClassFileFilter(filePath);
                    RecurseDirForClassFile recursePackageDir = new RecurseDirForClassFile(
                            packageName, classFilieFilter, classNameMap, classFullNameMap);
                    recursePackageDir.recursiveVisit();
                } else if ("jar".equals(protocol)) {
                    JarFile jarFile = null;
                    try {
                        jarFile = ((JarURLConnection) url.openConnection())
                                .getJarFile();
                        loadClassesForJarFile(packageName, jarFile, classNameMap, classFullNameMap);
                    } catch (IOException e) {
                        logError("loadClasses()", e);
                    } finally {
                        if(jarFile != null) {
                            try {
                                jarFile.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                }//if
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void loadClassesForJarFile(
            String targetPackageName,
            JarFile jarFile,
            HashMap<String, Class<?>> classNameMap,
            HashMap<String, Class<?>> classFullNameMap) {
        ClassJarEntryFilter entryFilter = new ClassJarEntryFilter(targetPackageName);

        Enumeration<JarEntry> entries = jarFile.entries();
        JarEntry entry;
        String className;
        String classFullName;
        int index;

        while(entries.hasMoreElements()) {
            entry = entries.nextElement();
            if(entryFilter.accept(entry)) {
                //Load class
                classFullName = entry.getName().replace('/', '.');
                if(classFullName.charAt(0) == '/') {
                    classFullName = classFullName.substring(1, classFullName.length() - 6);
                } else {
                    classFullName = classFullName.substring(0, classFullName.length() - 6);
                }

                index = classFullName.lastIndexOf('.');
                if(index < 0) {
                    className = classFullName;
                } else {
                    className = classFullName.substring(index + 1);
                }

                try {
                    //Add into the map
                    Class<?> cls = getDefaultClassLoader().loadClass(classFullName);
                    classNameMap.put(className, cls);
                    classFullNameMap.put(classFullName, cls);
                    logDebug("Loaded " + classFullName);
                } catch (ClassNotFoundException e) {
                    logError("loadClassesForJarFile()", e);
                }
            }
        }
    }

    public HashMap<String, Class<?>> getClassFullNameMap() {
        return _classFullNameMap;
    }

    public HashMap<String, Class<?>> getClassNameMap() {
        return _classNameMap;
    }

    protected class ClassFileFilter implements FileFilter {
        private String _targetPackagePath;

        public String getTargetPackagePath() {
            return _targetPackagePath;
        }

        public void setTargetPackagePath(String targetPackagePath) {
            _targetPackagePath = targetPackagePath;
        }

        public ClassFileFilter(String targetPackagePath) {
            _targetPackagePath = targetPackagePath;

            if(!_targetPackagePath.endsWith(File.separator)) {
                _targetPackagePath += File.separator;
            }
        }

        @Override
        public boolean accept(File pathname) {
            if(pathname.getAbsolutePath().startsWith(_targetPackagePath)
                    && pathname.getAbsolutePath().toLowerCase().endsWith(".class")) {
                return true;
            } else {
                return false;
            }
        }
    }

    protected class RecurseDirForClassFile extends DirectoryRecursiveVisitor {
        private String _targetPackageName;
        //private String _targetPackageDirName;
        private String _packageBaseDirPath;
        private ClassFileFilter _classFileFilter;

        private HashMap<String, Class<?>> _classNameMap;
        private HashMap<String, Class<?>> _classFullNameMap;

        public RecurseDirForClassFile (String targetPackageName, ClassFileFilter classFileFilter,
                                       HashMap<String, Class<?>> classNameMap,
                                       HashMap<String, Class<?>> classFullNameMap) {
            super(classFileFilter);

            _classFileFilter = classFileFilter;
            _targetPackageName = targetPackageName;
            //_targetPackageDirName = targetPackageName.replace('.', File.separatorChar);

            _packageBaseDirPath = classFileFilter._targetPackagePath.substring(
                    0,  classFileFilter._targetPackagePath.length() - 1 - _targetPackageName.length());

            logDebug("_packageBaseDirPath:" + _packageBaseDirPath);
            logDebug("_classFileFilter._targetPackagePath:" + _classFileFilter._targetPackagePath);

            _classNameMap = classNameMap;
            _classFullNameMap = classFullNameMap;
        }

        public void recursiveVisit() {
            super.recursiveVisit(new File(_classFileFilter._targetPackagePath));
        }

        @Override
        protected void dealEnterDirectory(File arg0) {
            //Nothing TODO
        }

        @Override
        protected void dealFile(File file) {
            String className = file.getName().substring(0, file.getName().length() - 6);
            String classFullName = file.getAbsolutePath().substring(
                    _packageBaseDirPath.length(), file.getAbsolutePath().length() - 6);
            classFullName = classFullName.replace(File.separatorChar, '.');

            try {
                //Add into the map
                Class<?> cls = getDefaultClassLoader().loadClass(classFullName);
                _classNameMap.put(className, cls);
                _classFullNameMap.put(classFullName, cls);

                logDebug("Loaded " + classFullName);
            } catch (ClassNotFoundException e) {
                logError("dealFile()", e);
            }
        }

        @Override
        protected void dealLeaveDirectory(File arg0) {
            //Nothing TODO

        }

        @Override
        protected void dealLeaveLeafDirectory(File arg0) {
            //Nothing TODO

        }
    }

    protected class ClassJarEntryFilter{
        private String _targetPackageJarEntryName;
        private String _targetPackageJarEntryName2;

        public ClassJarEntryFilter(String targetPackageName) {
            _targetPackageJarEntryName = targetPackageName.replace('.', '/') + '/';
            _targetPackageJarEntryName2 = '/' + _targetPackageJarEntryName;
        }

        public boolean accept(JarEntry entry) {
            String entryName = entry.getName();
            boolean result = true;

            if(entryName.charAt(0) == '/') {
                result = entryName.startsWith(_targetPackageJarEntryName2);
            } else {
                result = entryName.startsWith(_targetPackageJarEntryName);
            }

            if(result) {
                if(entryName.endsWith(".class")) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private abstract class DirectoryRecursiveVisitor {
        protected FileFilter _fileFilter = null;

        public DirectoryRecursiveVisitor (FileFilter fileFilter) {
            _fileFilter = fileFilter;
        }

        public void recursiveVisit(File curFolder) {
            File[] fileList = curFolder.listFiles();
            if (fileList != null) {
                int foldCount = 0;
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].isDirectory()) {
                        foldCount++;
                        dealEnterDirectory(fileList[i]);
                        recursiveVisit(fileList[i]);
                        dealLeaveDirectory(fileList[i]);
                    } else {
                        if (_fileFilter.accept(fileList[i])) {
                            dealFile(fileList[i]);
                        }
                    }
                }
                if (foldCount == 0) {
                    dealLeaveLeafDirectory(curFolder);
                }
            }
        }

        protected abstract void dealFile(File currentFile);
        protected abstract void dealEnterDirectory(File currentPath);
        protected abstract void dealLeaveDirectory(File currentPath);
        protected abstract void dealLeaveLeafDirectory(File currentPath);
    }

    private static void logDebug(String msg) {
        System.out.println(PreScanClassFinder.class.getName() + " -> " + msg);
    }

    private static void logError(String msg, Throwable e) {
        System.out.println(PreScanClassFinder.class.getName() + " -> " + msg);
        e.printStackTrace();
    }

    private static ClassLoader getDefaultClassLoader() {
        ClassLoader classLoader = null;

        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (classLoader == null) {
            classLoader = PreScanClassFinder.class.getClassLoader();
        }

        return classLoader;
    }

}
