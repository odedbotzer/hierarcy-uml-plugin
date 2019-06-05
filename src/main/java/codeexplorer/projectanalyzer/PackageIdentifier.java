package codeexplorer.projectanalyzer;

import codeexplorer.plantuml.UmlRepresentation;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static codeexplorer.projectanalyzer.JavaFileAnalyzer.javaFileFilter;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

public class PackageIdentifier implements JavaContainmentEntity {
    private final String fullPath;
    private final File fileObj;
    private final HashSet<JavaContainmentEntity> children;
    private final JavaEntityType entityType;


    public PackageIdentifier(File folder) {
        this.fileObj = folder;
        this.fullPath = folder.getAbsolutePath();
        if (!fileObj.isDirectory())
            throw new RuntimeException(this.fullPath + " is not a directory");

        this.entityType = analyzeJavaEntityType(folder);
        this.children = extractAllChildren();
    }

    public static boolean noMutualContainment(PackageIdentifier p1, PackageIdentifier p2) {
        return !p1.fullPath.startsWith(p2.fullPath) && !p2.fullPath.startsWith(p1.fullPath);
    }

    @NotNull
    private static Stream<File> subFoldersStream(File folder) {
        return stream(folder.listFiles(path -> path.isDirectory()));
    }

    public Set<JavaFileIdentifier> getContainedJavaFiles() {
        return stream(fileObj.listFiles(javaFileFilter))
                .map(JavaFileIdentifier::createOptional)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }

    @Override
    public JavaEntityType getJavaEntityType() {
        return this.entityType;
    }

    @Override
    public String getUmlContainmentString(Path parent, Path sourcesRoot, AnalyzerMode mode) {
        if (!getJavaEntityType().equals(JavaEntityType.PACKAGE))
            throw new RuntimeException("package is of type " + getJavaEntityType().name() + ", but should have been of type PACKAGE");

        return "\n package " + getUmlNameRelativeTo(parent) + " as " + getUmlNameRelativeTo(sourcesRoot) + " {" +
                "\n  " + getContainedEntities().stream()
                .map(entity -> entity.getUmlContainmentString(this.fileObj.toPath(), sourcesRoot, mode))
                .reduce(UmlRepresentation::concatWithSingleNewLine)
                .orElse("")
                .replace("\n", "\n  ")
                + "\n }";
    }

    @Override
    public HashSet<JavaContainmentEntity> getContainedEntities() {
        return this.children;
    }

    @Override
    public File getFile() {
        return this.fileObj;
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

    private HashSet<JavaContainmentEntity> extractAllChildren() {
        Set<JavaContainmentEntity> subFolders = subFoldersStream(this.fileObj)
                .map(PackageIdentifier::new)
                .collect(toSet());

        HashSet<JavaContainmentEntity> combinedSet = Sets.newHashSet();
        combinedSet.addAll(getContainedJavaFiles());
        combinedSet.addAll(subFolders);

        return combinedSet;
    }

    private JavaEntityType analyzeJavaEntityType(File folder) {
        if (folder.listFiles(javaFileFilter).length > 0)
            return JavaEntityType.PACKAGE;
        if (doesRecursivelyContainJavaFiles(folder))
            return JavaEntityType.PARTIAL_PACKAGE;
        return JavaEntityType.FOLDER;
    }

    private boolean doesRecursivelyContainJavaFiles(File folder) {
        if (folder.listFiles(javaFileFilter).length > 0)
            return true;
        return subFoldersStream(folder).anyMatch(this::doesRecursivelyContainJavaFiles);
    }
}
