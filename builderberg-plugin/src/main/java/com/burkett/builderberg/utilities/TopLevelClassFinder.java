package com.burkett.builderberg.utilities;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;

public class TopLevelClassFinder {
    public static PsiClass findTopLevelClass(final Project project, final AnActionEvent event) {
        final Navigatable navigatable = event.getData(CommonDataKeys.NAVIGATABLE);
        if (navigatable != null && navigatable.canNavigate()) {
            navigatable.navigate(true);
            if (navigatable instanceof PsiClass) {
                return (PsiClass) navigatable;
            }
        } else {
            final Editor editor = event.getData(LangDataKeys.EDITOR);
            final PsiFile file = event.getData(LangDataKeys.PSI_FILE);
            if (editor != null && file != null) {
                return getTopLevelClassFromEditor(file, editor);
            }
        }

        return null;
    }

    private static PsiClass getTopLevelClassFromEditor(final PsiFile file, final Editor editor) {
        final int offset = editor.getCaretModel().getOffset();
        final PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        }

        final PsiClass topLevelClass = PsiUtil.getTopLevelClass(element);
        final PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        if (psiClass != null) {
            if (psiClass.hasModifierProperty(PsiModifier.STATIC) ||
                    psiClass.getManager().areElementsEquivalent(psiClass, topLevelClass)) {
                return psiClass;
            }
        }

        return null;
    }
}
