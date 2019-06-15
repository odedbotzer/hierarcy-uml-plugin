package org.plantuml.idea.toolwindow;

import codeexplorer.projectanalyzer.ModuleAnalyzer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.intellij.openapi.ui.Messages.getQuestionIcon;
import static com.intellij.openapi.ui.Messages.showYesNoCancelDialog;
import static java.util.Arrays.stream;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlToolWindowFactory implements ToolWindowFactory, DumbAware, Condition<Project> {

    public static final String ID = "PlantUML";
    public static PlantUmlToolWindow plantUmlToolWindow = null;
    public static ModuleAnalyzer moduleAnalyzer;
    //    private static File moduleImlFile;
    private static Module module;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        plantUmlToolWindow = new PlantUmlToolWindow(project, toolWindow);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(plantUmlToolWindow, "", false);
        toolWindow.getContentManager().addContent(content);

//        moduleAnalyzer = new ModuleAnalyzer(moduleImlFile,plantUmlToolWindow);
        moduleAnalyzer = new ModuleAnalyzer(module, plantUmlToolWindow);
    }

    @Override
    public boolean value(Project project) {
//        moduleImlFile = chooseModuleImlFile(project);
//        return moduleImlFile != null && moduleImlFile.isFile();
        module = chooseModule(project);
        return module != null;
    }

    private Module chooseModule(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        String[] moduleNames = stream(modules).map(Module::getName).toArray(String[]::new);
        if (modules.length == 0)
            return null;
        else if (modules.length == 1) {
            int result = showYesNoCancelDialog("Would you like to analyze module " + moduleNames[0], "Analyze Module?", getQuestionIcon());
            return result == 0 ? modules[0] : null; //0 is YES
        }

        //multiple modules to choose from
        Object selected = JOptionPane.showInputDialog(null, "Which module would you like to analyze?", "Selection", QUESTION_MESSAGE, getQuestionIcon(), moduleNames, moduleNames[0]);
        if (selected == null) //null if the user cancels.
            return null;

        return stream(modules)
                .filter(module -> selected.toString().equals(module.getName()))
                .findFirst().orElseThrow(RuntimeException::new);
    }

//    private File chooseModuleImlFile(Project project) {
//        try {
//            String miscFilePath = new File(project.getProjectFile().getPath()).getPath();
//            String modulesFilePath = miscFilePath.substring(0, miscFilePath.lastIndexOf(File.separator) + 1) + "modules.xml";
//            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//            Document doc = dBuilder.parse(new FileInputStream(modulesFilePath));
//            NodeList modules = doc.getElementsByTagName("module");
//            for (int i = 0; i < modules.getLength(); i++) {
//                NamedNodeMap attrs = modules.item(i).getAttributes();
//                File moduleFile = new File(attrs.getNamedItem("filepath").getNodeValue().replace("$PROJECT_DIR$", project.getBasePath()));
//                int result = showYesNoCancelDialog(project, "Analyze " + moduleFile.getName() + " module?", "Choose module to analyze", getQuestionIcon());
//                if (result == 0) //0 is YES
//                    return moduleFile;
//            }
//        } catch (Exception e) {
//            Messages.showErrorDialog("Could not analyze project structure: modules.xml file should be in the same directory as misc.xml", "ERROR");
//        }
//        return null;
//    }
}
