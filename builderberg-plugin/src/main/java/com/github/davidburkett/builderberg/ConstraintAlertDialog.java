package com.github.davidburkett.builderberg;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.PanelWithText;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConstraintAlertDialog extends DialogWrapper {
    private final PanelWithText panelWithText = new PanelWithText();

    public ConstraintAlertDialog(final Project project, final PsiField field, final String constraint) {
        super(project, false);

        panelWithText.setText(String.format("Constraint (%s) not valid for field (%s).", constraint, field.getName()));
        setTitle("Builderberg Error");
        setResizable(false);
        init();
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction()};
    }
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panelWithText;
    }
}
