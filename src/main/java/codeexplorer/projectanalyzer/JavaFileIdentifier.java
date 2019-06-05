package codeexplorer.projectanalyzer;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;

public class JavaFileIdentifier implements JavaContainmentEntity {

    private final String fullPath;
    private final File fileObj;
    private String umlString;

    public JavaFileIdentifier(File file) throws NoSuchFileException {
        validateAsJavaFile(file);
        this.fileObj = file;
        this.fullPath = file.getAbsolutePath();
    }

    public JavaFileIdentifier(String fullPath) throws NoSuchFileException {
        this(new File(fullPath));
    }

    private static void validateAsJavaFile(File file) throws NoSuchFileException {
        String absolutePath = file.getAbsolutePath();
        if (!file.isFile())
            throw new NoSuchFileException(absolutePath + " is not a file");
        if (!absolutePath.endsWith(".java"))
            throw new NoSuchFileException(absolutePath + " is not a java file");
    }

    static Optional<JavaFileIdentifier> createOptional(File file) {
        try {
            return Optional.of(new JavaFileIdentifier(file));
        } catch (NoSuchFileException ignored) {
            return Optional.empty();
        }
    }

    static Optional<JavaFileIdentifier> createOptional(String fullPath) {
        return createOptional(new File(fullPath));
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

    public String getNameWithoutExtension() {
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
    public String getUmlContainmentString(Path parent, Path sourcesRoot, AnalyzerMode mode) {
        umlString = getUmlString(sourcesRoot, mode);
        return umlString;
    }

    @NotNull
    public String getUmlString(Path sourcesRoot, AnalyzerMode mode) {
        String base = getRegularUmlString(sourcesRoot);
        if(!mode.equals(AnalyzerMode.CLASS_FOLLOWER))
            return "hide " + base;
        return base;
    }

    @NotNull
    private String getRegularUmlString(Path sourcesRoot) {
        return "class " + getNameWithoutExtension() + " as " + getUmlNameRelativeTo(sourcesRoot);
    }

    @NotNull
    public String getUmlString(Path sourcesRoot) {
        return umlString == null ? getRegularUmlString(sourcesRoot) : umlString;
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
