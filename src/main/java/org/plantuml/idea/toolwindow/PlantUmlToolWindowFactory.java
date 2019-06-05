package org.plantuml.idea.toolwindow;

import codeexplorer.projectanalyzer.ModuleAnalyzer;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;

import static com.intellij.openapi.ui.Messages.getQuestionIcon;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlToolWindowFactory implements ToolWindowFactory, DumbAware, Condition<Project> {

    public static final String ID = "PlantUML";
    public static PlantUmlToolWindow plantUmlToolWindow = null;
    private static File moduleImlFile;
    public static ModuleAnalyzer moduleAnalyzer;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        plantUmlToolWindow = new PlantUmlToolWindow(project, toolWindow);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(plantUmlToolWindow, "", false);
        toolWindow.getContentManager().addContent(content);

        moduleAnalyzer = new ModuleAnalyzer(moduleImlFile, plantUmlToolWindow);
    }

    @Override
    public boolean value(Project project) {
        moduleImlFile = chooseModuleImlFile(project);
        return moduleImlFile != null && moduleImlFile.isFile();
    }

    private File chooseModuleImlFile(Project project) {
        try {
            String miscFilePath = new File(project.getProjectFile().getPath()).getPath();
            String modulesFilePath = miscFilePath.substring(0, miscFilePath.lastIndexOf(File.separator) + 1) + "modules.xml";
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new FileInputStream(modulesFilePath));
            NodeList modules = doc.getElementsByTagName("module");
            for (int i = 0; i < modules.getLength(); i++) {
                NamedNodeMap attrs = modules.item(i).getAttributes();
                File moduleFile = new File(attrs.getNamedItem("filepath").getNodeValue().replace("$PROJECT_DIR$", project.getBasePath()));
                int result = Messages.showYesNoCancelDialog(project, "Analyze " + moduleFile.getName() + " module?", "Choose module to analyze", getQuestionIcon());
                if (result == 0) //0 is YES
                    return moduleFile;
            }
        } catch (Exception e) {
            Messages.showErrorDialog("Could not analyze project structure: modules.xml file should be in the same directory as misc.xml", "ERROR");
        }
        return null;
    }
}
