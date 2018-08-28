package com.github.davidburkett.builderberg.generators;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

public class CloneGenerator {
    private final Project project;
    private final PsiElementFactory psiElementFactory;
    private final JavadocGenerator javadocGenerator;

    public CloneGenerator(final Project project) {
        this.project = project;
        this.psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);
        this.javadocGenerator = new JavadocGenerator(psiElementFactory);
    }

    public void generateClone(final PsiClass topLevelClass) {
        // Create clone method
        final PsiManager psiManager = PsiManager.getInstance(project);
        final PsiType objectType = PsiType.getJavaLangObject(psiManager, topLevelClass.getResolveScope());
        final PsiMethod cloneMethod = psiElementFactory.createMethod("clone", objectType);

        // Add throws CloneNotSupportedException
        final PsiClassType type = (PsiClassType)psiElementFactory.createTypeFromText("java.lang.CloneNotSupportedException", cloneMethod);
        final PsiJavaCodeReferenceElement referenceElement = psiElementFactory.createReferenceElementByType(type);
        cloneMethod.getThrowsList().add(referenceElement);

        // Generate inheritDoc javadoc
        javadocGenerator.generateInheritDocJavadocForMethod(cloneMethod);

        // Add @Override annotation
        cloneMethod.getModifierList().addAnnotation("Override");

        // Add trivial comparison statement
        final PsiCodeBlock methodBody = cloneMethod.getBody();
        final PsiStatement cloneStatement = psiElementFactory.createStatementFromText("return super.clone();", cloneMethod);
        methodBody.add(cloneStatement);

        topLevelClass.add(cloneMethod);
    }
}
