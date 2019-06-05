package codeexplorer.projectanalyzer;

import codeexplorer.plantuml.UmlBuilder;
import com.intellij.openapi.ui.Messages;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ModuleAnalyzer {
    private final PlantUmlToolWindow plantUmlToolWindow;
    private final File moduleRootDir;
    private DependencyAnalyzer dependencyAnalyzer;
    private JavaContainmentEntity rootEntity;
    public AnalyzerMode analyzerMode = AnalyzerMode.NONE;

    public ModuleAnalyzer(File moduleImlFile, PlantUmlToolWindow plantUmlToolWindow) {
        this.plantUmlToolWindow = plantUmlToolWindow;
        moduleRootDir = (new JavaModuleRootExtractor(moduleImlFile)).getModuleRootDir();
        rebuildAnalysis();
    }

    public void rebuildAnalysis() {
        HierarchyAnalyzer hierarchyAnalyzer = new HierarchyAnalyzer(moduleRootDir);
        rootEntity = hierarchyAnalyzer.getRootEntity();
        dependencyAnalyzer = new DependencyAnalyzer(rootEntity);
    }

    public void renderAnalysis(File curEditedFile) {
        JavaFileIdentifier.createOptional(curEditedFile).ifPresent(focusedFile -> {
            UmlBuilder umlBuilder = new UmlBuilder(rootEntity);
            switch (analyzerMode) {
                case NONE:
                    return;
                case PACKAGE_FOLLOWER:
                    umlBuilder.setFocusedFile(focusedFile);
                case PACKAGE_VIEWER:
                    umlBuilder.addPackageDependencies(dependencyAnalyzer.getPackageDependencies());
                    break;
                case CLASS_FOLLOWER:
                    umlBuilder
                            .addFileDependencies(focusedFile, this.dependencyAnalyzer.getFileDependencies(focusedFile))
                            .addReverseFileDependencies(this.dependencyAnalyzer.getFileDependencies(focusedFile), focusedFile)
                            .setFocusedFile(focusedFile);
                    break;
                default:
                    throw new RuntimeException("Analysis Mode [" + analyzerMode + "] is unknown");
            }

            plantUmlToolWindow.renderLater(
                    LazyApplicationPoolExecutor.Delay.NOW,
                    RenderCommand.Reason.REFRESH,
                    umlBuilder.build(analyzerMode).writeUml());
        });
    }

    private static class JavaModuleRootExtractor {
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
}
