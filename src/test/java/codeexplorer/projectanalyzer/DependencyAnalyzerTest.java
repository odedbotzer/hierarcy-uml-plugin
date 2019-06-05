package codeexplorer.projectanalyzer;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

@Ignore
public class DependencyAnalyzerTest {

    public static final String SRC_FOLDER = "C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java";
    private final JavaFileIdentifier sourceAnalyzerFile = new JavaFileIdentifier("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\upper\\projectanalyzer\\SourcesAnalyzer.java");
    private final JavaFileIdentifier javaFileAnalyzerFile = new JavaFileIdentifier("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\upper\\projectanalyzer\\JavaFileAnalyzer.java");
    private final JavaFileIdentifier editorTabFollowerFile = new JavaFileIdentifier("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\upper\\filefollower\\EditorTabFollower.java");
    private final JavaFileIdentifier followStarterFile = new JavaFileIdentifier("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\upper\\filefollower\\FollowStarter.java");
    private final JavaFileIdentifier followStopperrFile = new JavaFileIdentifier("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\upper\\testpackage\\FollowStopper.java");
    private final JavaFileIdentifier packageIdentifierFile = new JavaFileIdentifier("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\upper\\projectanalyzer\\PackageIdentifier.java");
    private final JavaFileIdentifier javaIdentifierFile = new JavaFileIdentifier("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\upper\\projectanalyzer\\JavaFileIdentifier.java");
    private final JavaFileIdentifier umlRepresentationFile = new JavaFileIdentifier("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\plantuml\\UmlRepresentation.java");
    private final JavaFileIdentifier umlBuilderFile = new JavaFileIdentifier("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\plantuml\\UmlBuilder.java");
    private final PackageIdentifier p1 = new PackageIdentifier(new File("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\upper\\filefollower"));
    private final PackageIdentifier p2 = new PackageIdentifier(new File("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\upper\\testpackage"));
    private final PackageIdentifier p3 = new PackageIdentifier(new File("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\plantuml"));
    private final PackageIdentifier p4 = new PackageIdentifier(new File("C:\\Users\\odedbotzer\\IdeaProjects\\code-explorer\\src\\test\\resources\\java\\upper\\projectanalyzer"));
    private DependencyAnalyzer dependencyAnalyzer;
    private Set<PackageIdentifier> packages;

    public DependencyAnalyzerTest() throws NoSuchFileException {
    }

    @Before
    public void calcPackages() {
        File sourceRootFile = new File(SRC_FOLDER);
        JavaContainmentEntity rootEntity = new PackageIdentifier(sourceRootFile);
        dependencyAnalyzer = new DependencyAnalyzer(rootEntity);
        packages = dependencyAnalyzer.getSrcPackages();
        packages.forEach(System.out::println);
    }

    @Test
    public void packagesCorrectTest() {
        assertThat(packages.size(), equalTo(4));
        assertTrue(packages.containsAll(newHashSet(p1, p2, p3, p4)));
    }

    @Test
    public void getFileDependenciesCorrectTest() {
        Map<JavaFileIdentifier, Set<JavaFileIdentifier>> dependencies = dependencyAnalyzer.getFileDependencies();
        Map<JavaFileIdentifier, Set<JavaFileIdentifier>> actualDependencies = ImmutableMap.<JavaFileIdentifier, Set<JavaFileIdentifier>>builder()
                .put(umlBuilderFile, newHashSet(javaIdentifierFile, packageIdentifierFile))
                .put(umlRepresentationFile, newHashSet(javaIdentifierFile, packageIdentifierFile))
                .put(followStarterFile, newHashSet(editorTabFollowerFile))
                .put(followStopperrFile, newHashSet(editorTabFollowerFile))
                .put(packageIdentifierFile, newHashSet(javaFileAnalyzerFile))
                .put(sourceAnalyzerFile, newHashSet(javaFileAnalyzerFile))
                .build();
        assertThat(dependencies.entrySet(), equalTo(actualDependencies.entrySet()));
    }

    @Test
    public void getPackageDependenciesNotEmptyTest() {
        Map<PackageIdentifier, Set<PackageIdentifier>> dependencies = dependencyAnalyzer.getPackageDependencies();
        assertFalse(dependencies.isEmpty());
    }

    @Test
    public void getReverseFileDependenciesNotEmptyTest() {
        Set<JavaFileIdentifier> dependingOnFile = dependencyAnalyzer.getReverseFileDependencies(javaFileAnalyzerFile);
        Set<JavaFileIdentifier> actualDependingOnFile = newHashSet(packageIdentifierFile, sourceAnalyzerFile);
        assertThat(actualDependingOnFile, equalTo(dependingOnFile));
    }

}