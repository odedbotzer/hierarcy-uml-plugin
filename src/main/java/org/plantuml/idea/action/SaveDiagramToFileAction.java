package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.toolwindow.PlantUmlImageLabel;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;

/**
 * @author Eugene Steinberg
 */
public class SaveDiagramToFileAction extends AbstractSaveDiagramAction {

    @Override
    protected int getPageNumber(AnActionEvent e) {
        PlantUmlToolWindow umlToolWindow = UIUtils.getPlantUmlToolWindow(e.getProject());
        JPanel imagesPanel = umlToolWindow.getImagesPanel();
        PlantUmlImageLabel image = (PlantUmlImageLabel) imagesPanel.getComponent(0);
        return image.getPage();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlToolWindow umlToolWindow = UIUtils.getPlantUmlToolWindow(e.getProject());
            if (umlToolWindow != null) {
                int selectedPage = umlToolWindow.getSelectedPage();
                e.getPresentation().setEnabled(umlToolWindow.getNumPages() == 1 || (umlToolWindow.getNumPages() > 1 && selectedPage != -1));
            } else {
                e.getPresentation().setEnabled(false);
            }
        }
    }
    
    
    
}
