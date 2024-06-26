package com.google.idea.blaze.clwb.ui;

import com.google.idea.blaze.base.lang.buildfile.language.BuildFileType;
import com.google.idea.blaze.base.settings.Blaze;
import com.google.idea.blaze.base.wizard2.BazelDisableImportNotification;
import com.google.idea.blaze.base.wizard2.BazelImportCurrentProjectAction;
import com.google.idea.sdkcompat.clion.ui.OpenFolderHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.google.idea.sdkcompat.clion.ui.OpenFolderHelper.isProjectAwareFile;

public final class BazelProjectNotificationAndFixesProvider implements OpenFolderHelper.ProjectNotificationProvider, OpenFolderHelper.ProjectFixesProvider {
    @Override
    public List<AnAction> collectFixes(@NotNull Project project, VirtualFile file) {
        if (file == null)
            return List.of();

        if (Blaze.isBlazeProject(project))
            return List.of();

        if (!isProjectAwareFile(project, file) && file.getFileType() != BuildFileType.INSTANCE) {
            return List.of();
        }
        if (!BazelImportCurrentProjectAction.projectCouldBeImported(project)) {
            return List.of();
        }
        if (BazelDisableImportNotification.isNotificationDisabled(project)) {
            return List.of();
        }

        String root = project.getBasePath();
        if (root == null) {
            return List.of();
        }

        return List.of(new ImportBazelAction(root), new DisableImportNotification());
    }

    private static class ImportBazelAction extends AnAction {
        private final String root;

        public ImportBazelAction(String root) {
            super("Import Bazel project");
            this.root = root;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            BazelImportCurrentProjectAction.createAction(root).run();
        }
    }

    private static class DisableImportNotification extends AnAction {
        private final AnAction action = new BazelDisableImportNotification.Action();

        public DisableImportNotification() {
            super("Dismiss import notification");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            action.actionPerformed(anActionEvent);
        }
    }

    @Override
    public OpenFolderHelper.ProjectNotification collectNotification(@NotNull Project project, VirtualFile file) {
        if (Blaze.isBlazeProject(project))
            return new OpenFolderHelper.ProjectNotification(OpenFolderHelper.NotificationType.OK, "Project is configured", OpenFolderHelper.NotificationScope.Project);

        if (file == null)
            return null;

        if (!isProjectAwareFile(project, file) && file.getFileType() != BuildFileType.INSTANCE) {
            return null;
        }
        if (!BazelImportCurrentProjectAction.projectCouldBeImported(project)) {
            return null;
        }

        String root = project.getBasePath();
        if (root == null) {
            return null;
        }

        if (BazelDisableImportNotification.isNotificationDisabled(project)) {
            return new OpenFolderHelper.ProjectNotification(OpenFolderHelper.NotificationType.OK, "Project is configured", OpenFolderHelper.NotificationScope.Project);
        }

        return new OpenFolderHelper.ProjectNotification(OpenFolderHelper.NotificationType.WARNING, "Project is not configured", OpenFolderHelper.NotificationScope.Project);
    }
}
