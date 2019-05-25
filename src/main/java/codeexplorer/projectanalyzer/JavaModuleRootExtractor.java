package codeexplorer.projectanalyzer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static com.intellij.openapi.ui.Messages.getQuestionIcon;

public class JavaModuleRootExtractor {
    private final Project project;
    private final File moduleImlFile;
    private final String moduleName;
    private final File moduleRootDir;

    public JavaModuleRootExtractor(Project project) {
        this.project = project;
        this.moduleImlFile = chooseModuleImlFile();
        this.moduleName = this.moduleImlFile.getName();
        this.moduleRootDir = extractModuleRootDirectory();
    }

    private File chooseModuleImlFile() {
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
            Messages.showInfoMessage("No source module to analyze", "NOTICE");
            throw new RuntimeException();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            Messages.showErrorDialog("Could not analyze project structure: modules.xml file should be in the same directory as misc.xml", "ERROR");
            throw new RuntimeException();
        }
    }

    private File extractModuleRootDirectory() {
        try {
            InputStream imlFileInputStream = new FileInputStream(moduleImlFile);
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(imlFileInputStream);
            NodeList folders = doc.getElementsByTagName("sourceFolder");
            for (int i = 0; i < folders.getLength(); i++) {
                NamedNodeMap attrs = folders.item(i).getAttributes();
                Node isTestSourceNode = attrs.getNamedItem("isTestSource");
                if (isTestSourceNode != null && isTestSourceNode.getNodeValue().equals("false"))
                    return new File(parseModuleDirPath(attrs));
            }
            throw new RuntimeException();
        } catch (Exception e) {
            Messages.showErrorDialog("Error finding module source folder for module " + this.moduleName, "ERROR");
            throw new RuntimeException();
        }
    }

    private String parseModuleDirPath(NamedNodeMap attrs) {
        String prefix = "file://$MODULE_DIR$/";

        String url = attrs.getNamedItem("url").getNodeValue();
        int numFoldersUp = getNumFoldersUp(url);
        int offset = prefix.length() + (numFoldersUp * 3);
        File startFolder = this.moduleImlFile.getParentFile();
        for (int i = 0; i < numFoldersUp; i++) {
            startFolder = startFolder.getParentFile();
        }
        return startFolder.getAbsolutePath() + File.separator + url.substring(offset);
    }

    private int getNumFoldersUp(String url) {
        int last = url.lastIndexOf("..");
        if (last == -1)
            return 0;
        int first = url.indexOf("..");
        return (last - first) / 3 + 1;
    }

    public File getModuleRootDir() {
        return moduleRootDir;
    }
}
