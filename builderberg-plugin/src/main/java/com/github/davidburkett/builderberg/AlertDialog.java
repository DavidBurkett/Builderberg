package com.github.davidburkett.builderberg;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.PanelWithText;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AlertDialog extends DialogWrapper {
    private final PanelWithText panelWithText = new PanelWithText();

    public AlertDialog(final Project project) {
        super(project, false);

        panelWithText.setText("Builderberg version does not meet the minimum configured for the class.");
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
