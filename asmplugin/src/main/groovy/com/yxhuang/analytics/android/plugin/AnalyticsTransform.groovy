package com.yxhuang.analytics.android.plugin

import com.android.annotations.NonNull
import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import groovy.io.FileType

class AnalyticsTransform extends Transform {

    private AnalyticsExtension mAnalyticsExtension

    AnalyticsTransform(AnalyticsExtension analyticsExtension) {
        this.mAnalyticsExtension = analyticsExtension
    }

    @Override
    String getName() {
        return "AnalyticsAutoTrack"
    }

    /**
     * 需要处理的数据
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     *  Transform 要操作的内容范围
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        customTransform(transformInvocation.getContext(), transformInvocation.getInputs(),
                transformInvocation.getOutputProvider(), transformInvocation.incremental)
    }

    void customTransform(Context context, Collection<TransformInput> inputs, TransformOutputProvider outputProvider,
                         boolean isIncremental) {

        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        // 遍历
        inputs.forEach { TransformInput input ->
            // 遍历目录
            input.directoryInputs.each { DirectoryInput directoryInput ->
                handleDirectoryInputs(context, directoryInput, outputProvider)
            }
            // 遍历 jar
            input.jarInputs.each { JarInput jarInput ->
                handleJarInputs(context, jarInput, outputProvider)
            }
        }
    }

    private void handleDirectoryInputs(Context context, DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        println("== AnalyticsTransform directoryInputs = " + directoryInput.file.listFiles().toArrayString())

        File dest = outputProvider.getContentLocation(directoryInput.name,
                directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
        File dir = directoryInput.file

        if (dir) {
            Map<String, File> modifyMap = new HashMap<>()
            // 遍历以某一拓展名结尾的文件
            dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) { File classFile ->
                if (AnalyticsClassModifier.isShouldModify(classFile.name)) {
                    println("-----AnalyticsTransform modifyClassFile dir =" + dir.absolutePath
                            + "\nclassFile= " + classFile.name + "\ngetTemporaryDir " + context.getTemporaryDir()
                            + "\nmAnalyticsExtension.disableAutoTrack=" + mAnalyticsExtension.disableAutoTrack)
                    File modified = null
                    if (!mAnalyticsExtension.disableAutoTrack) {
                        modified = AnalyticsClassModifier.modifyClassFile(dir, classFile, context.getTemporaryDir())
                    }
                    if (modified != null) {
                        // 包名 + 类名  /com/yxhuang/autotrack/android/app/MainActivity.class*/
                        String key = classFile.absolutePath.replace(dir.absolutePath, "")
                        modifyMap.put(key, modified)
                    }
                }
            }
            // 复制到文件
            FileUtils.copyDirectory(directoryInput.file, dest)
            modifyMap.entrySet().each { Map.Entry<String, File> en ->
                File target = new File(dest.absolutePath + en.getKey())
                if (target.exists()) {
                    target.delete()
                }
                FileUtils.copyFile(en.getValue(), target)
                en.getValue().delete()
            }
        }
    }

    private void handleJarInputs(Context context, JarInput jarInput, TransformOutputProvider outputProvider) {
        println("\n\n== AnalyticsTransform jarInput = " + jarInput.file.name)

        String destName = jarInput.file.name
        // 截取文件路径的 md5 值重命名输出路文件
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath.substring(0, 8))
        // 获取 jar 的名字
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        // 获取输出文件
        File dest = outputProvider.getContentLocation(destName + "_" + hexName,
                jarInput.contentTypes, jarInput.scopes, Format.JAR)
        def modifiedJar = null
        if (!mAnalyticsExtension.disableAutoTrack) {
            modifiedJar = AnalyticsClassModifier.modifyJar(jarInput.file, context.getTemporaryDir(), true)
        }
        if (modifiedJar == null) {
            modifiedJar = jarInput.file
        }

        println("== AnalyticsTransform jarInput = modifiedJar " + modifiedJar.name + "\ndest=" + dest)
        FileUtils.copyFile(modifiedJar, dest)
    }
}