package codeexplorer.listeners;

import codeexplorer.projectanalyzer.AnalyzerMode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import static org.plantuml.idea.toolwindow.PlantUmlToolWindowFactory.moduleAnalyzer;

public class PackageExploreStarter extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        moduleAnalyzer.analyzerMode = AnalyzerMode.PACKAGE_FOLLOWER;
        Messages.showInfoMessage("Started PACKAGE EXPLORE mode.\nEnter a java file to start", "EXPLORER ON");
    }
}
