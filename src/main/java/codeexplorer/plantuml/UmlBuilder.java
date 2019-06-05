package codeexplorer.plantuml;

import codeexplorer.projectanalyzer.AnalyzerMode;
import codeexplorer.projectanalyzer.JavaContainmentEntity;
import codeexplorer.projectanalyzer.JavaFileIdentifier;
import codeexplorer.projectanalyzer.PackageIdentifier;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

public class UmlBuilder {
    private final JavaContainmentEntity rootEntity;
    private Set<JavaFileIdentifier> additionalFiles = Sets.newHashSet();
    private Map<JavaFileIdentifier, Set<JavaFileIdentifier>> fileDependencies = newHashMap();
    private Map<PackageIdentifier, Set<PackageIdentifier>> packageDependencies = newHashMap();
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<JavaFileIdentifier> focusedFile = Optional.empty();

    public UmlBuilder(JavaContainmentEntity rootEntity) {
        this.rootEntity = rootEntity;
    }

    public UmlBuilder addFileDependencies(JavaFileIdentifier fromFile, Set<JavaFileIdentifier> toFiles) {
        this.fileDependencies.merge(fromFile, toFiles, Sets::union);
        return this;
    }

    public UmlBuilder addReverseFileDependencies(Set<JavaFileIdentifier> fromFiles, JavaFileIdentifier toFile) {
        Set<JavaFileIdentifier> toFileSet = newHashSet(toFile);
        fromFiles.forEach(fromFile -> this.fileDependencies.merge(fromFile, toFileSet, Sets::union));
        return this;
    }

    public UmlBuilder addPackageDependencies(Map<PackageIdentifier, Set<PackageIdentifier>> packageDependencies) {
        packageDependencies.forEach((key, value) -> this.packageDependencies.merge(key, value, Sets::union));
        return this;
    }

    public UmlBuilder setFocusedFile(JavaFileIdentifier focusedFile) {
        this.focusedFile = Optional.of(focusedFile);
        return this;
    }

    public UmlRepresentation build(AnalyzerMode mode) {
        return new UmlRepresentation(this.packageDependencies, this.fileDependencies, this.focusedFile, this.rootEntity, this.additionalFiles, mode);
    }

}
