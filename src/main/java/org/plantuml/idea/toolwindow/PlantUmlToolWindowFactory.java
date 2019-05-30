package org.plantuml.idea.toolwindow;

import codeexplorer.plantuml.UmlBuilder;
import codeexplorer.projectanalyzer.DependencyAnalyzer;
import codeexplorer.projectanalyzer.HierarchyAnalyzer;
import codeexplorer.projectanalyzer.JavaContainmentEntity;
import codeexplorer.projectanalyzer.JavaModuleRootExtractor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlToolWindowFactory implements ToolWindowFactory, DumbAware {

    public static final String ID = "PlantUML";
    public static PlantUmlToolWindow plantUmlToolWindow = null;
    public static UmlBuilder umlBuilder = null;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        plantUmlToolWindow = new PlantUmlToolWindow(project, toolWindow);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(plantUmlToolWindow, "", false);
        toolWindow.getContentManager().addContent(content);
        initializeUmlBuilder(project);

//        if (PlantUmlSettings.getInstance().isAutoRender()) {
//            plantUmlToolWindow.renderLater(LazyApplicationPoolExecutor.Delay.POST_DELAY, RenderCommand.Reason.FILE_SWITCHED, "");
//        }
    }

    private void initializeUmlBuilder(Project project) {
        try {
            File moduleRootDir = (new JavaModuleRootExtractor(project)).getModuleRootDir();
            HierarchyAnalyzer hierarchyAnalyzer = new HierarchyAnalyzer(moduleRootDir);
            JavaContainmentEntity rootEntity = hierarchyAnalyzer.getRootEntity();
            DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(rootEntity);
            umlBuilder = new UmlBuilder(rootEntity).addPackageDependencies(dependencyAnalyzer.getPackageDependencies());
        } catch (Exception e) {
            Messages.showErrorDialog("Error initializing sources analyzer: " + e.getLocalizedMessage(), "ERROR");
        }
    }

}
