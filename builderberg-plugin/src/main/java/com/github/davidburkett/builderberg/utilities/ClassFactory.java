package com.github.davidburkett.builderberg.utilities;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.util.PsiUtil;

import java.util.List;

public class ClassFactory {
    private final PsiElementFactory psiElementFactory;

    public ClassFactory(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
    }

    public PsiClass createClass(final String className, final PsiTypeParameter[] typeParameters, final List<String> modifiers) {
        final PsiClass createdClass = psiElementFactory.createClass(className);

        for (int i = 0; i < typeParameters.length; i++) {
            createdClass.getTypeParameterList().add(typeParameters[i]);
        }

        for (final String modifier : modifiers) {
            PsiUtil.setModifierProperty(createdClass, modifier, true);
        }

        return createdClass;
    }
}
