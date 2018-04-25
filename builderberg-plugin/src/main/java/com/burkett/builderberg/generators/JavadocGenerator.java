package com.burkett.builderberg.generators;

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import org.apache.commons.lang.StringUtils;


public class JavadocGenerator {
    private final PsiElementFactory psiElementFactory;

    public JavadocGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
    }

    /**
     * Generates the javadoc comment for the given getter methods using information from the {@link PsiField field}.
     */
    // TODO: This is broken and results in unnecessary new lines in generated javadoc.
    public void generateCommentForGetterMethod(final PsiMethod getterMethod, final PsiField field) {
        final PsiDocComment fieldComment = field.getDocComment();
        if (fieldComment != null) {
            final StringBuilder fieldJavaDoc = new StringBuilder("/**");
            final PsiElement[] descriptionElements = fieldComment.getDescriptionElements();

            boolean returnAnnotationAdded = false;
            for (PsiElement descriptionElement : descriptionElements) {
                final String descriptionText = descriptionElement.getText().replace("\n", "").replace("\r", "");
                if (!StringUtils.isWhitespace(descriptionText)) {
                    fieldJavaDoc.append("\n* " + (returnAnnotationAdded ? "" : "@return ") + descriptionText.trim());
                    returnAnnotationAdded = true;
                }
            }

            fieldJavaDoc.append("\n*/");
            setMethodComment(getterMethod, fieldJavaDoc.toString());
        } else {
            final String javaDoc = "/**\n* @return " + field.getName() + "\n*/";
            setMethodComment(getterMethod, javaDoc);
        }
    }

    private void setMethodComment(final PsiMethod method, final String commentText) {
        final PsiComment comment = psiElementFactory.createCommentFromText(commentText, null);

        final PsiDocComment docComment = method.getDocComment();
        if (docComment != null) {
            docComment.replace(comment);
        } else {
            method.addBefore(comment, method.getFirstChild());
        }
    }
}
