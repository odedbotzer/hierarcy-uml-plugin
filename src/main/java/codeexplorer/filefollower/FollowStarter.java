package codeexplorer.filefollower;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.intellij.openapi.ui.Messages.getInformationIcon;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import static codeexplorer.filefollower.EditorTabFollower.active;

public class FollowStarter extends AnAction {
    public FollowStarter() {
        super("FollowStarter");
    }

    public void actionPerformed(AnActionEvent event) {
        active = true;
        showMessageDialog(event.getProject(), "Started following", "Follow files", getInformationIcon());
    }
}
