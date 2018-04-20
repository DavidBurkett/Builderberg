package com.burkett.builderberg;

import com.intellij.psi.*;

public class InnerBuilderRunnable implements Runnable {
    private final PsiElementFactory psiElementFactory;
    private final PsiClass topLevelClass;

    public InnerBuilderRunnable(final PsiElementFactory psiElementFactory, final PsiClass topLevelClass) {
        this.psiElementFactory = psiElementFactory;
        this.topLevelClass = topLevelClass;
    }

    @Override
    public void run() {
        // Clean up previously-generated inner classes
        final PsiClass[] innerClasses = topLevelClass.getAllInnerClasses();
        for (final PsiClass innerClass : innerClasses) {
            innerClass.delete();
        }

        // Clean up previously-generated methods
        final PsiMethod[] methods = topLevelClass.getMethods();
        for (final PsiMethod method : methods) {
            method.delete();
        }

        // Generate new inner builder
        final BuilderGenerator builderGenerator = new BuilderGenerator(psiElementFactory);
        builderGenerator.generateInnerBuilder(topLevelClass);
    }
}
