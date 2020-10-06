package com.yxhuang.analytics.android.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class AnalyticsClassVisitor extends ClassVisitor {

    private static final String TAG = "AnalyticsClassVisitor"

    private static final String SDK_API_CLASS = "com/yxhuang/asmlib/DataAutoTrackHelper"

    private ClassVisitor mClassVisitor
    private String[] mInterface

    AnalyticsClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM6, classVisitor)
        mClassVisitor = classVisitor
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
        super.visit(version, access, name, signature, superName, interfaces)
        mInterface = interfaces
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor  methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        String nameDesc = name + descriptor

        println("------------AnalyticsClassVisitor name=" + name + " descriptor=" + descriptor + " nameDesc=" + nameDesc)


        methodVisitor = new AnalyticsDefaultMethodVisitor(methodVisitor, access, name, descriptor){

            // 这里插入指令
            @Override
            protected void onMethodExit(int opcode) {
                super.onMethodExit(opcode)
                if (mInterface != null && mInterface.length > 0){
                    if (mInterface.contains('android/view/view$OnClickListener') && nameDesc == 'onClick(Landroid/view/View;)V' ||
                            descriptor == '(Landroid/view/View;)V'){
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
                        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, SDK_API_CLASS,
                                'trackViewOnClick', '(Landroid/view/View;)V', false)
                    }
                }
            }
        }
        return methodVisitor
    }
}