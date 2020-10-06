package com.yxhuang.analytics.android.plugin;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Created by yxhuang
 * Date: 2020/10/2
 * Description:
 */
public class AnalyticsDefaultMethodVisitor extends AdviceAdapter {

    protected AnalyticsDefaultMethodVisitor(MethodVisitor methodVisitor, int access, String name,
                                            String descriptor) {
        super(Opcodes.ASM6, methodVisitor, access, name, descriptor);
    }
}
