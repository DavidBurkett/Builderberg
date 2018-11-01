package com.github.davidburkett.builderberg.utilities;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaDocumentedElement;
import com.intellij.psi.javadoc.PsiDocComment;
import org.apache.commons.lang.StringUtils;

public class JavadocUtil {
    public static void setComment(final PsiJavaDocumentedElement element, final PsiComment comment) {
        final PsiDocComment docComment = element.getDocComment();
        if (docComment != null) {
            docComment.replace(comment);
        } else {
            element.addBefore(comment, element.getFirstChild());
        }
    }

    public static String getCommentText(final PsiJavaDocumentedElement element) {
        // TODO: Also check for line comments, if no javadoc exists.
        final PsiDocComment fieldComment = element.getDocComment();
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
