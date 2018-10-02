package com.github.davidburkett.builderberg.utilities;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;

import java.util.List;

public class MethodUtility {
    private final PsiElementFactory psiElementFactory;

    public MethodUtility(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
    }

    public PsiMethod createPublicStaticMethod(final String methodName, final PsiType returnType, final PsiParameter... parameters) {
        final PsiMethod method = createMethod(methodName, returnType, parameters);
        PsiUtil.setModifierProperty(method, PsiModifier.PUBLIC, true);
        PsiUtil.setModifierProperty(method, PsiModifier.STATIC, true);

        return method;
    }

    public PsiMethod createPublicMethod(final String methodName, final PsiType returnType, final PsiParameter... parameters) {
        final PsiMethod method = createMethod(methodName, returnType, parameters);
        PsiUtil.setModifierProperty(method, PsiModifier.PUBLIC, true);

        return method;
    }

    public PsiMethod createPrivateMethod(final String methodName, final PsiType returnType, final PsiParameter... parameters) {
        final PsiMethod method = createMethod(methodName, returnType, parameters);
        PsiUtil.setModifierProperty(method, PsiModifier.PRIVATE, true);

        return method;
    }

    public PsiMethod createPrivateConstructor() {
        final PsiMethod constructor = psiElementFactory.createConstructor();
        PsiUtil.setModifierProperty(constructor, PsiModifier.PRIVATE, true);

        return constructor;
    }

    private PsiMethod createMethod(final String methodName, final PsiType returnType, final PsiParameter... parameters) {
        final PsiMethod method = psiElementFactory.createMethod(methodName, returnType);

        // Add parameter
        for (final PsiParameter parameter : parameters) {
            PsiUtil.setModifierProperty(parameter, PsiModifier.FINAL, true);
            method.getParameterList().add(parameter);
        }

        return method;
    }

    public void addParameter(final PsiMethod method, final String parameterName, final PsiType parameterType) {
        final PsiParameter parameter = psiElementFactory.createParameter(parameterName, parameterType);
        PsiUtil.setModifierProperty(parameter, PsiModifier.FINAL, true);
        method.getParameterList().add(parameter);
    }

    public void addStatement(final PsiMethod method, final String statementText) {
        final PsiStatement statement = psiElementFactory.createStatementFromText(statementText, method);
        method.getBody().add(statement);
    }

    public void addReturnStatement(final PsiMethod method, final String returnValue) {
        addStatement(method, "return " + returnValue + ";");
    }

    public void addIfStatement(final PsiMethod method, final String conditional, final String then) {
        final String statementText = String.format("if (%s) { %s }", conditional, then);
        addStatement(method, statementText);
    }

    public void addStatements(final PsiMethod method, final List<PsiStatement> statements) {
        final PsiCodeBlock body = method.getBody();
        for (final PsiStatement statement : statements) {
            body.add(statement);
        }
    }

    public void addThrows(final PsiMethod method, final String throwsType) {
        final PsiClassType type = (PsiClassType)psiElementFactory.createTypeFromText(throwsType, method);
        final PsiJavaCodeReferenceElement referenceElement = psiElementFactory.createReferenceElementByType(type);
        method.getThrowsList().add(referenceElement);
    }

    public void addJavadoc(final PsiMethod method, final List<String> javadocLines) {
        final StringBuilder javadocBuilder = new StringBuilder("/**\n");
        javadocLines.stream().forEach(line -> javadocBuilder.append("\n * " + line));
        javadocBuilder.append("\n*/");

        final PsiComment comment = psiElementFactory.createCommentFromText(javadocBuilder.toString(), method);
        JavadocUtil.setMethodComment(method, comment);
    }
}
