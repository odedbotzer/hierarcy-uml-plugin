package codeexplorer.projectanalyzer;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static codeexplorer.projectanalyzer.JavaFileAnalyzer.getAllImportedClassesRelativePath;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.stream.Collectors.*;

public class DependencyAnalyzer {

    private final File sourceRoot;
    private final Set<PackageIdentifier> srcPackages;
    private Map<JavaFileIdentifier, Set<JavaFileIdentifier>> fileDependencies;
    private Map<PackageIdentifier, Set<PackageIdentifier>> packageDependencies;

    public DependencyAnalyzer(JavaContainmentEntity rootEntity) {
        this.sourceRoot = rootEntity.getFile();
        validateSrcFolder(this.sourceRoot);
        this.srcPackages = rootEntity.getFlattenedPackages();
    }

    public Set<PackageIdentifier> getSrcPackages() {
        return srcPackages;
    }

    public Map<JavaFileIdentifier, Set<JavaFileIdentifier>> getFileDependencies() {
        if (fileDependencies == null)
            fileDependencies = analyzeAllFileDependencies();
        return fileDependencies;
    }

    public Set<JavaFileIdentifier> getFileDependencies(JavaFileIdentifier fromFile) {
        return getFileDependencies().get(fromFile);
    }

    @NotNull
    private Map<JavaFileIdentifier, Set<JavaFileIdentifier>> analyzeAllFileDependencies() {
        return getSrcPackages().stream()
                .flatMap(packageWithFiles -> packageWithFiles.getContainedJavaFiles().stream())
                .collect(toMap(Function.identity(), this::getAllImportedPackages, Sets::union))
                .entrySet().stream()
                .filter(map -> !map.getValue().isEmpty())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Set<JavaFileIdentifier> getAllImportedPackages(JavaFileIdentifier javaFile) {
        return getAllImportedClassesRelativePath(javaFile).stream()
                .map(this::relativeClassPathToJavaFile)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toSet());
    }

    private Optional<JavaFileIdentifier> relativeClassPathToJavaFile(String relativeClassPath) {
        return JavaFileIdentifier.createOptional(this.sourceRoot.getAbsolutePath() + File.separator + relativeClassPath + ".java");
    }

    public Set<JavaFileIdentifier> getReverseFileDependencies(JavaFileIdentifier toFile) {
        return getFileDependencies().entrySet().stream()
                .filter(entry -> entry.getValue().contains(toFile))
                .map(Map.Entry::getKey)
                .collect(toSet());
    }

    public Map<PackageIdentifier, Set<PackageIdentifier>> getPackageDependencies() {
        if (packageDependencies == null)
            packageDependencies = analyzeAllPackageDependencies();
        return packageDependencies;
    }

    private void validateSrcFolder(File srcFolder) {
        if (!srcFolder.isDirectory())
            throw new RuntimeException(format("%s is not a path to a directory", srcFolder.getAbsolutePath()));
    }

    private Map<PackageIdentifier, Set<PackageIdentifier>> analyzeAllPackageDependencies() {
        Map<PackageIdentifier, List<JavaFileIdentifier>> packageToContainedFileMap = getFileDependencies().keySet().stream()
                .collect(groupingBy(file -> file.getPackage()));
        return packageToContainedFileMap.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, map -> map.getValue().stream()
                                .map(file -> getFileDependencies().get(file))
                                .reduce(Sets::union)
                                .orElse(newHashSet()) //dependee file set
                                .stream()
                                .map(file -> file.getPackage())
                                .filter(dependeePackage -> !dependeePackage.equals(map.getKey())) //package does not depend upon itself
                                .collect(toSet()), //dependee package set
                        Sets::union))
                .entrySet().stream()
                .filter(map -> !map.getValue().isEmpty())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}