package codeexplorer.listeners;

import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static org.plantuml.idea.toolwindow.PlantUmlToolWindowFactory.moduleAnalyzer;

public class EditorTabFollower implements EditorTabTitleProvider {
    private volatile static String lastFilePath = "";

    @Nullable
    @Override
    public String getEditorTabTitle(Project project, VirtualFile file) {
        String curPath = file.getPath();
        if (!curPath.equals(lastFilePath) && moduleAnalyzer != null) {
            lastFilePath = curPath;
            moduleAnalyzer.renderAnalysis(new File(curPath));
        }
        return null;
    }
}
