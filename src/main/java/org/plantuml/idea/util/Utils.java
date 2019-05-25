package org.plantuml.idea.util;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import net.sourceforge.plantuml.FileSystem;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.preproc.Defines;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantIUmlFileType;
import org.plantuml.idea.lang.PlantUmlFileType;
import org.plantuml.idea.lang.settings.PlantUmlSettings;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class Utils {
    public static int asInt(String renderDelay, int defaultValue) {
        int i = defaultValue;
        //noinspection EmptyCatchBlock
        try {
            i = Integer.parseInt(renderDelay);
        } catch (NumberFormatException e) {
        }
        return i;
    }

    public static void setPlantUmlDir(@NotNull File baseDir) {
        FileSystem.getInstance().setCurrentDir(baseDir);
        System.setProperty("plantuml.include.path", baseDir.getAbsolutePath());
    }

    @NotNull
    public static SourceStringReader newSourceStringReader(String source, boolean useSettings) {
        List<String> configAsList;
        String encoding;
        if (useSettings) {
            PlantUmlSettings settings = PlantUmlSettings.getInstance();
            encoding = settings.getEncoding();
            configAsList = settings.getConfigAsList();
        } else {
            encoding = "UTF-8";
            configAsList = Collections.emptyList();
        }

        return new SourceStringReader(Defines.createEmpty(), source, encoding, configAsList);
    }

    public static boolean isPlantUmlFileType(@NotNull PsiFile file) {
        FileType fileType = file.getFileType();
        return fileType.equals(PlantUmlFileType.PLANTUML_FILE_TYPE) || fileType.equals(PlantIUmlFileType.PLANTUML_FILE_TYPE);
    }
}
