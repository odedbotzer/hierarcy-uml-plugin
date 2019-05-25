package codeexplorer.filefollower;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.intellij.openapi.ui.Messages.getInformationIcon;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import static codeexplorer.filefollower.EditorTabFollower.active;

public class FollowStopper extends AnAction {
    public FollowStopper() {
        super("FollowStopper");
    }

    public void actionPerformed(AnActionEvent event) {
        active = false;
        showMessageDialog(event.getProject(), "Stopped following", "Follow files", getInformationIcon());
    }
}
