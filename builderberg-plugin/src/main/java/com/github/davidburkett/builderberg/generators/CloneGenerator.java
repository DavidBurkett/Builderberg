package com.github.davidburkett.builderberg.generators;

import com.github.davidburkett.builderberg.utilities.AnnotationUtility;
import com.github.davidburkett.builderberg.utilities.MethodUtility;
import com.github.davidburkett.builderberg.utilities.TypeUtility;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

public class CloneGenerator {
    private final Project project;
    private final PsiElementFactory psiElementFactory;
    private final MethodUtility methodUtility;

    public CloneGenerator(final Project project) {
        this.project = project;
        this.psiElementFactory = PsiElementFactory.SERVICE.getInstance(project);
        this.methodUtility = new MethodUtility(psiElementFactory);
    }

    public void generateClone(final PsiClass topLevelClass) {
        // Add implements Cloneable
        addImplementsCloneable(topLevelClass);

        // Create clone method
        final PsiMethod cloneMethod = methodUtility.createPublicMethod("clone", TypeUtility.getJavaLangObject(project));

        // Add throws CloneNotSupportedException
        methodUtility.addThrows(cloneMethod, "java.lang.CloneNotSupportedException");

        // Generate inheritDoc javadoc
        methodUtility.addJavadoc(cloneMethod, ImmutableList.of("{@inheritDoc}"));

        // Add @Generated annotation
        AnnotationUtility.addGeneratedAnnotation(psiElementFactory, cloneMethod);

        // Add @Override annotation
        AnnotationUtility.addOverrideAnnotation(cloneMethod);

        // Add trivial comparison statement
        methodUtility.addReturnStatement(cloneMethod, "super.clone()");

        topLevelClass.add(cloneMethod);
    }

    private void addImplementsCloneable(final PsiClass topLevelClass) {
        final PsiClassType[] implementsClassTypes = topLevelClass.getImplementsList().getReferencedTypes();

        boolean alreadyImplemented = false;
        for (final PsiClassType implementsClassType : implementsClassTypes) {
            if (implementsClassType.getClassName().equalsIgnoreCase("Cloneable")) {
                alreadyImplemented = true;
                break;
            }
        }

        if (!alreadyImplemented) {
            final PsiClassType type = (PsiClassType)psiElementFactory.createTypeFromText("java.lang.Cloneable", topLevelClass);
            final PsiJavaCodeReferenceElement referenceElement = psiElementFactory.createReferenceElementByType(type);
            topLevelClass.getImplementsList().add(referenceElement);
        }
    }
}
