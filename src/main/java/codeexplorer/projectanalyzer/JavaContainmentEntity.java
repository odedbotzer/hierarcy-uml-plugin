package codeexplorer.projectanalyzer;

import com.google.common.collect.Sets;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static codeexplorer.projectanalyzer.JavaEntityType.PACKAGE;
import static java.io.File.separator;
import static java.util.stream.Collectors.toSet;

public interface JavaContainmentEntity {

    default Set<PackageIdentifier> getFlattenedPackages() {
        return Sets.union(getContainedEntities().stream()
                        .flatMap(subEntity -> subEntity.getFlattenedPackages().stream())
                        .collect(toSet()),
                getContainedEntities().stream()
                        .filter(subEntity -> subEntity.getJavaEntityType().equals(PACKAGE))
                        .map(subEntity -> ((PackageIdentifier) subEntity))
                        .collect(toSet()));
    }

    HashSet<JavaContainmentEntity> getContainedEntities();

    default void setContainedEntities(Set<JavaContainmentEntity> children) {
        getContainedEntities().clear();
        getContainedEntities().addAll(children);
    }

    default void addContainedEntities(Set<JavaContainmentEntity> containmentEntities) {
        getContainedEntities().addAll(containmentEntities);
    }

    JavaEntityType getJavaEntityType();

    default String getUmlNameRelativeTo(Path parent) {
        return parent.relativize(getFile().toPath()).toString().replace(separator, ".");
    }

    String getUmlContainmentString(Path parent, Path sourcesRoot, AnalyzerMode mode, Set<JavaContainmentEntity> entitiesToDisplay, Optional<JavaFileIdentifier> focusedFile);

    boolean isAnEntityToDisplay(Set<JavaContainmentEntity> entitiesToDisplay);

    File getFile();

    default void removeContainedEntity(JavaContainmentEntity containmentEntity) {
        getContainedEntities().remove(containmentEntity);
    }
}
