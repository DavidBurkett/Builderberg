package com.github.davidburkett.builderberg.utilities;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

public class CodeFormatter {
    public static void formatCode(final PsiClass topLevelClass, final Project project) {
        final PsiJavaFile psiJavaFile = (PsiJavaFile)topLevelClass.getContainingFile();

        final JavaCodeStyleManager javaCodeStyleManager = JavaCodeStyleManager.getInstance(project);
        javaCodeStyleManager.shortenClassReferences(psiJavaFile);
        javaCodeStyleManager.optimizeImports(psiJavaFile);

        final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
        codeStyleManager.reformat(psiJavaFile);
    }
}
