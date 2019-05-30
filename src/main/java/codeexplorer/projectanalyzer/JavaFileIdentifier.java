package codeexplorer.projectanalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class JavaFileIdentifier implements JavaContainmentEntity {

    private final String fullPath;
    private final File fileObj;

    public JavaFileIdentifier(File file) {
        validateAsJavaFile(file);
        this.fileObj = file;
        this.fullPath = file.getAbsolutePath();
    }

    public JavaFileIdentifier(String fullPath) {
        this(new File(fullPath));
    }

    private static void validateAsJavaFile(File file) {
        String absolutePath = file.getAbsolutePath();
        if (!file.isFile())
            throw new RuntimeException(absolutePath + " is not a file");
        if (!absolutePath.endsWith(".java"))
            throw new RuntimeException(absolutePath + " is not a java file");
    }

    public PackageIdentifier getPackage() {
        return new PackageIdentifier(fileObj.getParentFile());
    }

    public Reader getFileReader() {
        try {
            return new FileReader(this.fileObj);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("not really possible");
        }
    }

    public String getNameWithExtension() {
        final String extension = ".java";
        String nameWithExt = this.fileObj.getName();
        return nameWithExt.substring(0, nameWithExt.length() - extension.length());
    }

    @Override
    public HashSet<JavaContainmentEntity> getContainedEntities() {
        return newHashSet();
    }

    @Override
    public JavaEntityType getJavaEntityType() {
        return JavaEntityType.JAVA_FILE;
    }

    @Override
    public String getUmlContainmentString(Path parent) {
        return "class " + getNameWithExtension();
    }

    @Override
    public File getFile() {
        return this.fileObj;
    }

    @Override
    public String toString() {
        return this.fileObj.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaFileIdentifier that = (JavaFileIdentifier) o;
        return Objects.equals(fullPath, that.fullPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullPath);
    }
}
