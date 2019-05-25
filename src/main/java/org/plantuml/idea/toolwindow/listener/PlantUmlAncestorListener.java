package org.plantuml.idea.toolwindow.listener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.rendering.RenderCommand;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class PlantUmlAncestorListener implements AncestorListener {
    private static Logger logger = Logger.getInstance(PlantUmlAncestorListener.class);
    private final PlantUmlSettings settings;

    private PlantUmlToolWindow plantUmlToolWindow;
    private Project project;

    public PlantUmlAncestorListener(PlantUmlToolWindow plantUmlToolWindow, Project project) {
        this.plantUmlToolWindow = plantUmlToolWindow;
        this.project = project;
        settings = PlantUmlSettings.getInstance();
    }

    @Override
    public void ancestorAdded(AncestorEvent ancestorEvent) {
        logger.debug("ancestorAdded ", project.getName());
        if (settings.isAutoRender()) {
            plantUmlToolWindow.renderLater(LazyApplicationPoolExecutor.Delay.POST_DELAY, RenderCommand.Reason.FILE_SWITCHED, "");
        }
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {

    }

    @Override
    public void ancestorMoved(AncestorEvent event) {

    }

}
