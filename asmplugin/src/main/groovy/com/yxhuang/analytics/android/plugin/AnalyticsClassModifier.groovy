package com.yxhuang.analytics.android.plugin

import com.sun.istack.Nullable
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Matcher

class AnalyticsClassModifier {

    private static Set<String> sExclude = new HashSet<>()

    // 过滤，不用插桩的类
    static {
        sExclude = new HashSet<>()
        sExclude.add('androidx.appcompat')
        sExclude.add('com.yxhuang.asmlib')
    }

    @Nullable
    static File modifyJar(File jarFile, File tempDir, boolean nameHex) {
        // 读取原 jar
        def file = new JarFile(jarFile, false)

        // 设置输出的 jar
        def hexName = ""
        if (nameHex) {
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream = null
            try {
                inputStream = file.getInputStream(jarEntry)
            } catch (Exception e) {
                println(" \n--- modifyJar Exception " + e.getMessage())
                e.printStackTrace()
                return null
            }
            String entryName = jarEntry.getName()
            if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                //ignore
            } else {
                String className
                JarEntry jarEntry2 = new JarEntry(entryName)
                jarOutputStream.putNextEntry(jarEntry2)

                byte[] modifiedClassBytes = null
                byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)
                println(" --- modifyJar entryName " + entryName)
                if (entryName.endsWith(".class")) {
                    className = entryName.replace(Matcher.quoteReplacement(File.separator), ".").replace(".class", "")
                    println(" --- modifyJar className " + className)
                    if (isShouldModify(className)) {
                        modifiedClassBytes = modifyClass(sourceClassBytes)
                    }
                }
                if (modifiedClassBytes == null) {
                    modifiedClassBytes = sourceClassBytes
                }
                jarOutputStream.write(modifiedClassBytes)
                jarOutputStream.closeEntry()
            }
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    /**
     *  过滤 .class 文件 增加编译速度
     * @param className
     * @return
     */
    protected static boolean isShouldModify(String className) {
        Iterator<String> iterator = sExclude.iterator()
        while (iterator.hasNext()) {
            String packageName = iterator.next()
            if (className.startsWith(packageName)) {
                return false
            }
        }
        if (className.contains('R$') ||
                className.contains('R2$') ||
                className.contains('R2.class') ||
                className.contains('BuildConfig.class')) {
            return false
        }

        return true
    }

    private static byte[] modifyClass(byte[] sourceClass){
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassVisitor classVisitor = new AnalyticsClassVisitor(classWriter)
        ClassReader classReader = new ClassReader(sourceClass)
        classReader.accept(classVisitor, ClassReader.SKIP_FRAMES)
        return classWriter.toByteArray()
    }

    /**
     * 对 class 文件进行修改
     * @param dir
     * @param classFile
     * @param tempDir
     * @return
     */
    static File modifyClassFile(File dir, File classFile, File tempDir){
        File modified = null

        try {
            String className = path2ClassName(classFile.absolutePath.replace(dir.absolutePath + File.separator, ""))
            byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
            byte[] modifyClassBytes = modifyClass(sourceClassBytes)
            if (modifyClassBytes){
                modified = new File(tempDir, className.replace('.', '') + '.class')
                if (modified.exists()){
                    modified.delete()
                }
                modified.createNewFile()
                new FileOutputStream(modified).write(modifyClassBytes)
            }
        } catch (Exception e) {
            e.printStackTrace()
            modified = classFile
        }
        return modified
    }

    private static String path2ClassName(String pathName){
        pathName.replace(File.separator, ".").replace(".class", "")
    }
}