package codeexplorer.plantuml;

import codeexplorer.projectanalyzer.AnalyzerMode;
import codeexplorer.projectanalyzer.JavaContainmentEntity;
import codeexplorer.projectanalyzer.JavaFileIdentifier;
import codeexplorer.projectanalyzer.PackageIdentifier;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.union;

public class UmlRepresentation {
    private final Path sourcesRoot;
    private final AnalyzerMode mode;
    private final Optional<JavaFileIdentifier> focusedFile;
    private final JavaContainmentEntity rootEntity;
    private final Map<PackageIdentifier, Set<PackageIdentifier>> packageDependencies;
    private final Map<JavaFileIdentifier, Set<JavaFileIdentifier>> fileDependencies;
    private final Set<JavaContainmentEntity> allEntities;

    public UmlRepresentation(Map<PackageIdentifier, Set<PackageIdentifier>> packageDependencies,
                             Map<JavaFileIdentifier, Set<JavaFileIdentifier>> fileDependencies,
                             Optional<JavaFileIdentifier> focusedFile,
                             JavaContainmentEntity rootEntity,
                             Set<JavaFileIdentifier> additionalFiles,
                             AnalyzerMode mode) {

        focusedFile.ifPresent(file -> additionalFiles.add(file));
        this.allEntities = getAllEntities(packageDependencies, fileDependencies, additionalFiles);
        this.rootEntity = rootEntity;
        this.fileDependencies = fileDependencies;
        this.packageDependencies = packageDependencies;
        this.sourcesRoot = rootEntity.getFile().toPath();
        this.focusedFile = focusedFile;
        this.mode = mode;
    }

    private static Set<JavaFileIdentifier> getAllFiles(Map<JavaFileIdentifier, Set<JavaFileIdentifier>> fileDependencies,
                                                       Set<JavaFileIdentifier> additionalFiles) {
        Set<JavaFileIdentifier> allFiles = flattenEntitiesFromDependencyMap(fileDependencies);
        return Sets.union(allFiles, additionalFiles);
    }

    private static Set<JavaContainmentEntity> getAllEntities(Map<PackageIdentifier, Set<PackageIdentifier>> packageDependencies,
                                                             Map<JavaFileIdentifier, Set<JavaFileIdentifier>> fileDependencies,
                                                             Set<JavaFileIdentifier> additionalFiles) {
        Set<JavaFileIdentifier> allFiles = getAllFiles(fileDependencies, additionalFiles);
        Set<PackageIdentifier> allPackages = flattenEntitiesFromDependencyMap(packageDependencies);
        return union(allFiles, allPackages);
    }

    private static <T> Set<T> flattenEntitiesFromDependencyMap(Map<T, Set<T>> dependencyMap) {
        Set<T> allEntities = dependencyMap.values().stream()
                .reduce(Sets::union).orElse(newHashSet());
        return Sets.union(allEntities, dependencyMap.keySet());
    }

    public static String concatWithDoubleNewLine(String s1, String s2) {
        return s1 + "\n\n" + s2;
    }

    public static String concatWithSingleNewLine(String s1, String s2) {
        return s1 + "\n" + s2;
    }

    private String getDependenciesUml() {
        switch (mode) {
            case PACKAGE_FOLLOWER:
            case PACKAGE_VIEWER:
                return this.packageDependencies.entrySet().stream()
                        .map(this::getSinglePackageDependencyUml)
                        .reduce(UmlRepresentation::concatWithSingleNewLine)
                        .orElse("");
            default:
                return this.fileDependencies.entrySet().stream()
                        .map(this::getSingleJavaFileDependencyUml)
                        .reduce(UmlRepresentation::concatWithSingleNewLine)
                        .orElse("");
        }
    }

    private String getSingleJavaFileDependencyUml(Map.Entry<JavaFileIdentifier, Set<JavaFileIdentifier>> dependency) {
        JavaFileIdentifier from = dependency.getKey();
        String javaFileUmlName = from.getUmlNameRelativeTo(this.sourcesRoot);
        return dependency.getValue().stream()
                .map(p -> p.getUmlNameRelativeTo(this.sourcesRoot))
                .map(dependeeName -> javaFileUmlName + " -down-> " + dependeeName)
                .reduce(UmlRepresentation::concatWithSingleNewLine)
                .orElse("");
    }

    private String getSinglePackageDependencyUml(Map.Entry<PackageIdentifier, Set<PackageIdentifier>> dependency) {
        PackageIdentifier from = dependency.getKey();
        String packageUmlName = from.getUmlNameRelativeTo(this.sourcesRoot);
        return dependency.getValue().stream()
                .filter(dependee -> PackageIdentifier.noMutualContainment(from, dependee))
                .map(p -> p.getUmlNameRelativeTo(this.sourcesRoot))
                .map(dependeeName -> packageUmlName + " --> " + dependeeName)
                .reduce(UmlRepresentation::concatWithSingleNewLine)
                .orElse("");
    }

    public String writeUml() {
        String uml = getUmlOpening();
        uml += "\n";
        uml += getContainmentUml();
        uml += "\n";
        uml += getDependenciesUml();
        uml += "\n";
        uml += getUmlClosing();
        return uml;
    }

    @NotNull
    private String getContainmentUml() {
        return this.rootEntity.getContainedEntities().stream()
                .map(subEntity -> subEntity.getUmlContainmentString(this.sourcesRoot, this.sourcesRoot, this.mode, this.allEntities, this.focusedFile))
                .reduce(UmlRepresentation::concatWithDoubleNewLine)
                .orElse("");
    }

    @NotNull
    private String getUmlClosing() {
        return "@enduml";
    }

    @NotNull
    private String getUmlOpening() {
        return "@startuml\n";
    }

}
