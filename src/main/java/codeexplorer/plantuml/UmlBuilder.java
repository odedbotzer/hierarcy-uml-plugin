package codeexplorer.plantuml;

import codeexplorer.projectanalyzer.JavaContainmentEntity;
import codeexplorer.projectanalyzer.JavaFileIdentifier;
import codeexplorer.projectanalyzer.PackageIdentifier;
import com.google.common.collect.Sets;

import java.nio.file.Path;
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

    public UmlBuilder addFiles(Set<JavaFileIdentifier> additionalFiles) {
        this.additionalFiles = Sets.union(this.additionalFiles, additionalFiles);
        return this;
    }

    public UmlBuilder addFileDependencies(Map<JavaFileIdentifier, Set<JavaFileIdentifier>> fileDependencies) {
        fileDependencies.forEach((key, value) -> this.fileDependencies.merge(key, value, Sets::union));
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

    public UmlBuilder releaseFocusedDile() {
        this.focusedFile = Optional.empty();
        return this;
    }

    public UmlRepresentation build() {
        return new UmlRepresentation(this.packageDependencies, this.fileDependencies, this.focusedFile, this.rootEntity, this.additionalFiles);
    }

}
