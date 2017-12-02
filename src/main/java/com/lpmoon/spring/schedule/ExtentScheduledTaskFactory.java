package com.lpmoon.spring.schedule;

import com.lpmoon.spring.schedule.annotation.EnableExtentScheduled;
import com.lpmoon.spring.util.ObjectUtil;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

/**
 * Created by lpmoon on 17/11/24.
 */
public class ExtentScheduledTaskFactory {

    public static Object generate(Object source) throws Exception {

        // generate new class extends source class
        ClassPool pool = ClassPool.getDefault();
        CtClass parent = pool.get(source.getClass().getName());
        CtClass child = pool.makeClass(source.getClass().getSimpleName() + "Child");
        child.setSuperclass(parent);
        child.defrost();

        // scan all methods with @Schedule
        ClassFile childClassFile = child .getClassFile();
        ConstPool childConstPool = childClassFile.getConstPool();

        boolean hasScheduledToExtent = false;

        CtMethod[] ctMethods = child.getMethods();
        for (CtMethod ctMethod : ctMethods) {
            Object scheduleAnnotation = ctMethod.getAnnotation(Scheduled.class);
            Object enableExtentScheduledAnnotation = ctMethod.getAnnotation(EnableExtentScheduled.class);

            if (scheduleAnnotation != null && enableExtentScheduledAnnotation != null) {
                String cron = ((Scheduled) scheduleAnnotation).cron();
                if (StringUtils.isEmpty(cron) || !cron.startsWith("$")) {
                    continue;
                }

                // generate new pattern of @Schedule.cron
                String newPattern = generatPatternWithClassName(parent, cron);

                // generate new annotation
                AnnotationsAttribute annotationsAttribute = generateScheduleAnnotation(childConstPool, newPattern);

                // generate override method for child class, and set new annotation
                CtMethod overrideMethod = generateOverrideMethod(child, ctMethod, annotationsAttribute);

                // set body
                setOverrideMethodBody(ctMethod, overrideMethod);

                // add override method
                child.addMethod(overrideMethod);

                hasScheduledToExtent = true;
            }
        }

        if (hasScheduledToExtent) {
            Class childClass = child.toClass();
            return ObjectUtil.copy(source, childClass);
        }

        // if has no @Scheduled to extent, we just return original object
        return source;
    }

    private static void setOverrideMethodBody(CtMethod ctMethod, CtMethod overrideMethod) throws NotFoundException, CannotCompileException {
        // generate parameter string for calling super method
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
    }

    private static CtMethod generateOverrideMethod(CtClass child, CtMethod ctMethod, AnnotationsAttribute annotationsAttribute) throws CannotCompileException {
        CtMethod overrideMethod = CtNewMethod.copy(ctMethod, child, null);
        for (Object attribute : ctMethod.getMethodInfo().getAttributes()) {
            if (!(attribute instanceof CodeAttribute)) {
                overrideMethod.getMethodInfo().addAttribute((AttributeInfo) attribute);
            }
        }
        overrideMethod.getMethodInfo().addAttribute(annotationsAttribute);
        return overrideMethod;
    }

    private static AnnotationsAttribute generateScheduleAnnotation(ConstPool childConstPool, String newPattern) {
        AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(childConstPool, AnnotationsAttribute.visibleTag);
        Annotation annotation = new Annotation(Scheduled.class.getName(), childConstPool);
        annotation.addMemberValue("cron", new StringMemberValue("${" + newPattern + "}", childConstPool));
        annotationsAttribute.addAnnotation(annotation);
        return annotationsAttribute;
    }

    private static String generatPatternWithClassName(CtClass parent, String cron) {
        int patternStart = cron.indexOf("{");
        int patternEnd = cron.indexOf("}");
        String pattern = cron.substring(patternStart + 1, patternEnd);
        return parent.getName() + "." + pattern;
    }
}
