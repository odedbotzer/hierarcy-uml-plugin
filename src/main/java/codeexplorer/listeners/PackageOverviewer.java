package codeexplorer.listeners;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import static codeexplorer.projectanalyzer.AnalyzerMode.PACKAGE_VIEWER;
import static org.plantuml.idea.toolwindow.PlantUmlToolWindowFactory.moduleAnalyzer;

public class PackageOverviewer extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        moduleAnalyzer.analyzerMode = PACKAGE_VIEWER;
        Messages.showInfoMessage("Started static PACKAGE VIEWER mode.\nEnter a java file to start", "STATIC VIEWER ON");
    }
}
