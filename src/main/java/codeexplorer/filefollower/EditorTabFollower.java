package codeexplorer.filefollower;

import codeexplorer.projectanalyzer.JavaFileIdentifier;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import static org.plantuml.idea.toolwindow.PlantUmlToolWindowFactory.plantUmlToolWindow;
import static org.plantuml.idea.toolwindow.PlantUmlToolWindowFactory.umlBuilder;

public class EditorTabFollower implements EditorTabTitleProvider {
    public static boolean active = false;
    private static String lastFile = "";

    @Nullable
    @Override
    public String getEditorTabTitle(Project project, VirtualFile file) {
        String curPath = file.getPath();
        if (!curPath.equals(lastFile) && active) {
            try {
                File curEditedFile = new File(curPath);
                JavaFileIdentifier focusedFile = new JavaFileIdentifier(curEditedFile);
                rerenderUml(focusedFile);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getLocalizedMessage());
            }
//            String name = file.getNameWithoutExtension();
//            showMessageDialog(project, "File name: " + name, "File Follower", getInformationIcon());
            lastFile = curPath;
        }
        return null;
    }

    private void rerenderUml(JavaFileIdentifier focusedFile) {
        if (plantUmlToolWindow != null) {
            plantUmlToolWindow.renderLater(
                    LazyApplicationPoolExecutor.Delay.NOW,
                    RenderCommand.Reason.REFRESH,
                    umlBuilder.setFocusedFile(focusedFile).build().writeUml());
        }
    }
}
