package codeexplorer.projectanalyzer;

import java.io.File;
import java.util.Set;

import static codeexplorer.projectanalyzer.JavaEntityType.FOLDER;
import static codeexplorer.projectanalyzer.JavaEntityType.PARTIAL_PACKAGE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

public class HierarchyAnalyzer {

    private final JavaContainmentEntity rootEntity;

    public HierarchyAnalyzer(File sourceRoot) {
        validateSrcFolder(sourceRoot);
        this.rootEntity = new PackageIdentifier(sourceRoot);
        removeSubfolders(rootEntity);
        cutPartialPackages(rootEntity);
    }

    private static void removeSubfolders(JavaContainmentEntity entity) {
        entity.setContainedEntities(entity.getContainedEntities().stream()
                .filter(subEntity -> !subEntity.getJavaEntityType().equals(FOLDER))
                .collect(toSet()));

        entity.getContainedEntities().stream()
                .filter(subEntity -> !subEntity.getJavaEntityType().equals(FOLDER))
                .forEach(HierarchyAnalyzer::removeSubfolders);
    }

    private static void cutPartialPackages(JavaContainmentEntity entity) {
        while (entity.getContainedEntities().stream().anyMatch(subEntity -> subEntity.getJavaEntityType().equals(PARTIAL_PACKAGE)))
            cutDirectPartialPackages(entity);
        entity.getContainedEntities().forEach(HierarchyAnalyzer::cutPartialPackages);
    }

    private static void cutDirectPartialPackages(JavaContainmentEntity entity) {
        Set<JavaContainmentEntity> partialPackages = entity.getContainedEntities().stream()
                .filter(subEntity -> subEntity.getJavaEntityType().equals(PARTIAL_PACKAGE))
                .collect(toSet());
        partialPackages.forEach(partialPackage -> entity.addContainedEntities(partialPackage.getContainedEntities()));
        partialPackages.forEach(entity::removeContainedEntity);
    }

    public JavaContainmentEntity getRootEntity() {
        return rootEntity;
    }

    private void validateSrcFolder(File srcFolder) {
        if (!srcFolder.isDirectory())
            throw new RuntimeException(format("%s is not a path to a directory", srcFolder.getAbsolutePath()));
    }
}
