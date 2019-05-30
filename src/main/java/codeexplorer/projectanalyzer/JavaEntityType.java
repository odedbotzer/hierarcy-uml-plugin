package codeexplorer.projectanalyzer;

public enum JavaEntityType {
    JAVA_FILE,
    PACKAGE, //directly holds java files (and possibly other entities)
    FOLDER, //recursively other folders if anything
    PARTIAL_PACKAGE //recursively - but not directly - holds java_files
}
