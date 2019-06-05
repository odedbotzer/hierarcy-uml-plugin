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

import static codeexplorer.projectanalyzer.JavaEntityType.PACKAGE;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class UmlRepresentation {
    private final Path sourcesRoot;
    private final AnalyzerMode mode;
    private final Optional<JavaFileIdentifier> focusedFile;
    private final JavaContainmentEntity rootEntity;
    private final Map<PackageIdentifier, Set<JavaFileIdentifier>> containmentMap;
    private final Map<PackageIdentifier, Set<PackageIdentifier>> packageDependencies;
    private final Map<JavaFileIdentifier, Set<JavaFileIdentifier>> fileDependencies;
    private final String FOCUS_COLOR_STR = " #GreenYellow/LightGoldenRodYellow ";

    public UmlRepresentation(Map<PackageIdentifier, Set<PackageIdentifier>> packageDependencies,
                             Map<JavaFileIdentifier, Set<JavaFileIdentifier>> fileDependencies,
                             Optional<JavaFileIdentifier> focusedFile,
                             JavaContainmentEntity rootEntity,
                             Set<JavaFileIdentifier> additionalFiles,
                             AnalyzerMode mode) {

        //old part
        focusedFile.ifPresent(file -> additionalFiles.add(file));
        Set<JavaFileIdentifier> allFiles = getAllFiles(packageDependencies, fileDependencies, additionalFiles);
        this.containmentMap = allFiles.stream().collect(toMap(JavaFileIdentifier::getPackage, Sets::newHashSet, Sets::union));

        //new part
        this.rootEntity = rootEntity;
        this.fileDependencies = fileDependencies;
        this.packageDependencies = packageDependencies;
        this.sourcesRoot = rootEntity.getFile().toPath();
        this.focusedFile = focusedFile;
        this.mode = mode;
    }

    private static Set<JavaFileIdentifier> getAllFiles(Map<PackageIdentifier, Set<PackageIdentifier>> packageDependencies,
                                                       Map<JavaFileIdentifier, Set<JavaFileIdentifier>> fileDependencies,
                                                       Set<JavaFileIdentifier> additionalFiles) {
        Set<JavaFileIdentifier> allFiles = flattenEntitiesFromDependencyMap(fileDependencies);
        allFiles = Sets.union(allFiles, additionalFiles);
//        Set<PackageIdentifier> allPackages = flattenEntitiesFromDependencyMap(packageDependencies);
//        allFiles = addFilesOfEmptyPackages(allFiles, allPackages);
        return allFiles;
    }

    private static Set<JavaFileIdentifier> addFilesOfEmptyPackages(Set<JavaFileIdentifier> allFiles, Set<PackageIdentifier> allPackages) {
        Set<PackageIdentifier> nonEmptyPackages = allFiles.stream().map(JavaFileIdentifier::getPackage).collect(toSet());
        Set<PackageIdentifier> emptyPackages = allPackages.stream()
                .filter(p -> !nonEmptyPackages.contains(p))
                .collect(toSet());
        return Sets.union(allFiles,
                emptyPackages.stream()
                        .filter(p -> p.getJavaEntityType().equals(PACKAGE))
                        .map(p -> p.getContainedJavaFiles().iterator().next()) //there will be at least one because entity is of type PACKAGE
                        .collect(toSet()));
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

    private String getPackageDependenciesUml() {
        return this.packageDependencies.entrySet().stream()
                .map(this::getSinglePackageDependencyUml)
                .reduce(UmlRepresentation::concatWithDoubleNewLine)
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
        uml += getPackageDependenciesUml();
        uml += "\n";
        uml += getUmlClosing();
        return colorFocusedFile(uml);
    }

    @NotNull
    private String getContainmentUml() {
        return this.rootEntity.getContainedEntities().stream()
                .map(subEntity -> subEntity.getUmlContainmentString(this.sourcesRoot, this.sourcesRoot, this.mode))
                .reduce(UmlRepresentation::concatWithDoubleNewLine)
                .orElse("");
    }

    private String colorFocusedFile(String umlSoFar) {
        String unfocused = umlSoFar.replace(FOCUS_COLOR_STR, " ");
        return this.focusedFile
                .map(focused -> unfocused.replace(focused.getUmlString(sourcesRoot), focused.getUmlString(sourcesRoot) + FOCUS_COLOR_STR))
                .orElse(unfocused);
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
