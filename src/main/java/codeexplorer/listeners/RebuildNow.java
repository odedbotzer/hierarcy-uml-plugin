package codeexplorer.listeners;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import static org.plantuml.idea.toolwindow.PlantUmlToolWindowFactory.moduleAnalyzer;

public class RebuildNow extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        moduleAnalyzer.rebuildAnalysis();
        Messages.showInfoMessage("Analyzer has been rebuilt.\nEnter a java file to start", "REBUILD DONE");
    }
}
