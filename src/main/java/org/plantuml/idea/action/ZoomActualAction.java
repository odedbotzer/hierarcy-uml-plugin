package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author Eugene Steinberg
 */
public class ZoomActualAction extends ZoomAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        setZoom(e.getProject(), DEFAULT_ZOOM);
    }
}
