package codeexplorer.projectanalyzer;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaFileAnalyzer {
    @NotNull
    static FilenameFilter javaFileFilter = (dir, name) -> name.endsWith(".java");

    public static Set<String> getAllImportedClassesRelativePath(JavaFileIdentifier javaFile) {
        return readImportLines(javaFile).stream()
                .map(JavaFileAnalyzer::toImportedClassRelativePath)
                .collect(Collectors.toSet());
    }

    static boolean doesFolderContainJavaFiles(File folder) {
        return folder.list(javaFileFilter).length > 0;
    }

    private static String toImportedClassRelativePath(String importLine) {
        String import_static = "import static ";
        String regular_import = "import ";

        //trim non-path characters
        boolean isStaticImport = importLine.startsWith(import_static);
        int nameOffset = isStaticImport ? import_static.length() : regular_import.length();
        String packageStr = importLine.substring(nameOffset, importLine.indexOf(";")).trim();

        //remove function name if necessary
        if (isStaticImport)
            packageStr = packageStr.substring(0, packageStr.lastIndexOf("."));

        return packageStr.replace(".", File.separator);
    }

    private static Set<String> readImportLines(JavaFileIdentifier javaFile) {
        return new BufferedReader(javaFile.getFileReader()).lines()
                .filter(line -> line.startsWith("import "))
                .collect(Collectors.toSet());

    }
}
