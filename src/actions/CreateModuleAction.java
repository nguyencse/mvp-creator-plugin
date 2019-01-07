package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import enums.PackageType;
import helpers.MvpHelper;

public class CreateModuleAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        MvpHelper mvpHelper = new MvpHelper(project);
        mvpHelper.createPattern(e, PackageType.Module);
    }
}
