package com.github.davidburkett.builderberg.generators.builder;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.siyeh.ig.psiutils.TypeUtils;

import java.util.Optional;

public class ParameterGenerator {
    private final PsiElementFactory psiElementFactory;

    public ParameterGenerator(final PsiElementFactory psiElementFactory) {
        this.psiElementFactory = psiElementFactory;
    }

    public PsiParameter buildParameter(final PsiClass psiClass) {
        return buildParameter(psiClass, true);
    }

    public PsiParameter buildParameter(final PsiClass psiClass, final boolean makeFinal) {
        final PsiClassType type = TypeUtils.getType(psiClass);
        String baselineParameterName = Optional.ofNullable(psiClass.getName()).orElse("baseline");
        baselineParameterName = baselineParameterName.substring(0, 1).toLowerCase() + baselineParameterName.substring(1);
        final PsiParameter parameter = psiElementFactory.createParameter(baselineParameterName, type);
        PsiUtil.setModifierProperty(parameter, PsiModifier.FINAL, makeFinal);

        return parameter;
    }

}
