package codeexplorer.projectanalyzer;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyImportElement;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class IntelliJFileAnalyzer {
    public String getEditorTabTitle2(Project project, VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        JSFile jsFile = null;
        PsiJavaFile psiJavaFile = null;
        PyFile psiPythonFile = null;
        if(psiFile instanceof PsiJavaFile)
            psiJavaFile = (PsiJavaFile) psiFile;
        else if(psiFile instanceof PyFile)
            psiPythonFile = (PyFile) psiFile;
        else if(psiFile instanceof JSFile)
            jsFile = ((JSFile) psiFile);

        //js
        Set<String> referencedPaths = jsFile.getReferencedPaths();

        //python
        PyImportElement importTargetElement = psiPythonFile.getImportTargets().get(0);
        QualifiedName qualifiedName = importTargetElement.getImportedQName();

        //java
        PsiImportList importList = psiJavaFile.getImportList();
        PsiImportStatementBase[] importStatements = importList.getAllImportStatements();
        Set<String> importsQualifiedNames = stream(importStatements)
                .map(statement -> statement.getImportReference().getQualifiedName())
                .collect(Collectors.toSet());

        //what do we do with qualified names?



        PsiDirectory psiDir = PsiManager.getInstance(project).findDirectory();
        PsiJavaModule mo = JavaPsiFacade.getInstance(project).findModule("", GlobalSearchScope.allScope(project));
        ModuleRootManager.getInstance(mo.getChildren())

        Messages.showInfoMessage(psiJavaFile.getFileType().getName(),"file type name");
        Messages.showInfoMessage(psiJavaFile.getFileType().getDefaultExtension(),"file type default extension");

    }
}
