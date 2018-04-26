package com.github.davidburkett.builderberg;

import com.github.davidburkett.builderberg.utilities.TopLevelClassFinder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

public class BuilderbergAction extends AnAction {
    public BuilderbergAction() {
        super("InnerBuilderBuilder");
    }

    /**
     * Generates an inner-builder object for the selected .java file.
     */
    @Override
    public void actionPerformed(final AnActionEvent event) {
        final Project project = event.getProject();
        if (project != null) {
            final PsiClass topLevelClass = TopLevelClassFinder.findTopLevelClass(project, event);
            if (topLevelClass != null) {
                final PsiElementFactory psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);
                final Runnable builderGenerator = new BuilderbergRunnable(project, psiElementFactory, topLevelClass);

                WriteCommandAction.runWriteCommandAction(project, builderGenerator);


                /*final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
                codeStyleManager.reformat(topLevelClass.getContainingFile());*/
            }
        }
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