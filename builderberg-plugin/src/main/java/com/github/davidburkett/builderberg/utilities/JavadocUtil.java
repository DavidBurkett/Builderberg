package com.github.davidburkett.builderberg.utilities;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import org.apache.commons.lang.StringUtils;

public class JavadocUtil {
    public static void setMethodComment(final PsiMethod method, final PsiComment comment) {
        final PsiDocComment docComment = method.getDocComment();
        if (docComment != null) {
            docComment.replace(comment);
        } else {
            method.addBefore(comment, method.getFirstChild());
        }
    }

    public static String getFieldCommentText(final PsiField field) {
        // TODO: Also check for line comments, if no javadoc exists.
        final PsiDocComment fieldComment = field.getDocComment();
        if (fieldComment != null) {
            final PsiElement[] descriptionElements = fieldComment.getDescriptionElements();

            String fieldCommentText = "";
            for (final PsiElement descriptionElement : descriptionElements) {
                final String descriptionText = descriptionElement.getText().replace("\n", "").replace("\r", "");
                if (!StringUtils.isWhitespace(descriptionText)) {
                    fieldCommentText += "\n" + descriptionText;
                }
            }

            return fieldCommentText;
        }

        return null;
    }
}
