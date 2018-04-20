package com.burkett.builderberg;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;

public class InnerBuilderAction extends AnAction {
    public InnerBuilderAction() {
        super("InnerBuilderBuilder");
    }

    /**
     * Generates an inner-builder object for the selected .java file.
     */
    @Override
    public void actionPerformed(final AnActionEvent event) {
        final Project project = event.getProject();
        if (project != null) {
            final PsiClass topLevelClass = getTopLevelClass(project, event);
            if (topLevelClass != null) {
                final PsiElementFactory psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);
                final Runnable builderGenerator = new InnerBuilderRunnable(psiElementFactory, topLevelClass);

                WriteCommandAction.runWriteCommandAction(project, builderGenerator);
            }
        }
    }

    private PsiClass getTopLevelClass(final Project project, final AnActionEvent event) {
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

    private PsiClass getTopLevelClassFromEditor(final PsiFile file, final Editor editor) {
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

    /**
     * Update the context menu option to only display when right-clicking on .java files.
     */
    @Override
    public void update(final AnActionEvent event) {
        final VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        final boolean isJavaFile = isJavaFile(file);
        event.getPresentation().setEnabled(isJavaFile);
        event.getPresentation().setVisible(isJavaFile);
    }

    private static boolean isJavaFile(final VirtualFile file) {
        return file != null && file.getName().endsWith(".java");
    }
}