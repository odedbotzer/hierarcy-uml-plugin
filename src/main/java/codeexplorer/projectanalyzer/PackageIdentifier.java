package codeexplorer.projectanalyzer;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static codeexplorer.projectanalyzer.JavaFileAnalyzer.javaFileFilter;
import static codeexplorer.projectanalyzer.JavaFileAnalyzer.doesFolderContainJavaFiles;

public class PackageIdentifier {
    final String fullPath;
    private final File fileObj;

    public PackageIdentifier(File folder) {
        this.fileObj = folder;
        this.fullPath = folder.getAbsolutePath();
        if (!fileObj.isDirectory())
            throw new RuntimeException(this.fullPath + " is not a directory");
        if (!doesFolderContainJavaFiles(this.fileObj))
            throw new RuntimeException(this.fullPath + " does not contain java files");
    }

    public List<JavaFileIdentifier> getContainedJavaFiles() {
        return stream(fileObj.listFiles(javaFileFilter))
                .map(JavaFileIdentifier::new)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "PackageIdentifier{" +
                "fullPath='" + fullPath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageIdentifier that = (PackageIdentifier) o;
        return Objects.equals(fullPath, that.fullPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullPath);
    }
}
