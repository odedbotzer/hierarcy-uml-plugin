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

public class JavaModuleRootExtractor {
    private final File moduleImlFile;
    private final String moduleName;
    private final File moduleRootDir;

    public JavaModuleRootExtractor(File moduleImlFile) {
        this.moduleImlFile = moduleImlFile;
        this.moduleName = this.moduleImlFile.getName();
        this.moduleRootDir = extractModuleRootDirectory();
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
