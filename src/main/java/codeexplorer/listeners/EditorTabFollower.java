package codeexplorer.listeners;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import com.intellij.model.SymbolResolveResult;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyImportElement;
import com.jetbrains.python.psi.PyReferenceExpression;

import java.io.File;

import static org.plantuml.idea.toolwindow.PlantUmlToolWindowFactory.moduleAnalyzer;

public class EditorTabFollower implements EditorTabTitleProvider {
    private volatile static String lastFilePath = "";

    @Nullable
    @Override
    public String getEditorTabTitle(Project project, VirtualFile file) {
        String curPath = file.getPath();
        if (!curPath.equals(lastFilePath) && moduleAnalyzer != null) {
            lastFilePath = curPath;
            moduleAnalyzer.renderAnalysis(new File(curPath));
        }
        return null;
    }

    public String getEditorTabTitle2(Project project, VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        JSFile jsFile;
        PsiJavaFile psiJavaFile;
        PyFile psiPythonFile;
        if(psiFile instanceof PsiJavaFile)
            psiJavaFile = (PsiJavaFile) psiFile;
        else if(psiFile instanceof PyFile)
            psiPythonFile = (PyFile) psiFile;
        else if(psiFile instanceof JSFile)
            jsFile = ((JSFile) psiFile);

        SymbolResolveResult pyRefExp = psiPythonFile.getFromImports().get(0).getImportSource().getReference().resolveReference().iterator().next();
        PyImportElement importTargetElement = psiPythonFile.getImportTargets().get(0);
        QualifiedName qualifiedName = importTargetElement.getImportedQName();
        PsiImportList importList = psiJavaFile.getImportList();
        PsiImportStatementBase[] importStatements = importList.getAllImportStatements();
        for (PsiImportStatementBase importStatement : importStatements) {
            importStatement.resolve()
        }
        PsiDirectory psiDir = PsiManager.getInstance(project).findDirectory();
        PsiJavaModule mo = JavaPsiFacade.getInstance(project).findModule("", GlobalSearchScope.allScope(project));
        ModuleRootManager.getInstance(mo.getChildren())

        Messages.showInfoMessage(psiJavaFile.getFileType().getName(),"file type name");
        Messages.showInfoMessage(psiJavaFile.getFileType().getDefaultExtension(),"file type default extension");

    }
}
