package com.lpmoon.spring.schedule;

import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Created by lpmoon on 17/11/24.
 */
public class JavassistChildClassGenerator {

    public static Class generate(Object source) throws NotFoundException, CannotCompileException, ClassNotFoundException, IOException {

        // generate new class extends source class
        ClassPool pool = ClassPool.getDefault();
        CtClass parent = pool.get(source.getClass().getName());
        CtClass child = pool.makeClass(source.getClass().getSimpleName() + "Child");
        child.setSuperclass(parent);
        child.defrost();

        // scan all methods with @Schedule
        ClassFile childClassFile = child .getClassFile();
        ConstPool childConstPool = childClassFile.getConstPool();

        CtMethod[] ctMethods = child.getMethods();
        for (CtMethod ctMethod : ctMethods) {
            Object scheduleAnnotation = ctMethod.getAnnotation(Scheduled.class);
            if (scheduleAnnotation != null) {
                String cron = ((Scheduled) scheduleAnnotation).cron();
                if (StringUtils.isEmpty(cron) || !cron.startsWith("$")) {
                    continue;
                }

                // parse original value of @Schedule.cron
                int patternStart = cron.indexOf("{");
                int patternEnd = cron.indexOf("}");
                String pattern = cron.substring(patternStart + 1, patternEnd);
                String newPattern = parent.getName() + "." + pattern;

                // generate new annotation
                AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(childConstPool, AnnotationsAttribute.visibleTag);
                Annotation annotation = new Annotation(Scheduled.class.getName(), childConstPool);
                annotation.addMemberValue("cron", new StringMemberValue("${" + newPattern + "}", childConstPool));
                annotationsAttribute.addAnnotation(annotation);

                // generate overrided method for child class, and set new annotation
                CtMethod overrideMethod = CtNewMethod.copy(ctMethod, child, null);
                for (Object attribute : ctMethod.getMethodInfo().getAttributes()) {
                    if (!(attribute instanceof CodeAttribute)) {
                        overrideMethod.getMethodInfo().addAttribute((AttributeInfo) attribute);
                    }
                }
                overrideMethod.getMethodInfo().addAttribute(annotationsAttribute);

                // generate parameter string for calling super method
                CodeAttribute codeAttribute = ctMethod.getMethodInfo().getCodeAttribute();
                LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
                int paramCount = ctMethod.getParameterTypes().length;
                StringBuilder paramNameStr = new StringBuilder();
                if (paramCount >= 1) {
                    paramNameStr.append("$1");
                }
                for (int i = 1; i < paramCount; i++) {
                    paramNameStr.append(", $" + (i + 1));
                }

                // set method body
                boolean voidReturn = ctMethod.getReturnType() == null;
                StringBuilder body = new StringBuilder();
                body.append("{")
                        .append("System.out.println(\"execute in father class; \");")
                        .append((voidReturn ? "" : "return ") + "super." + ctMethod.getName() + "(" + paramNameStr.toString() + ");") //
                    .append("}");

                overrideMethod.setBody(body.toString());

                // add overrided method
                child.addMethod(overrideMethod);
            }
        }

        return child.toClass();
    }
}
