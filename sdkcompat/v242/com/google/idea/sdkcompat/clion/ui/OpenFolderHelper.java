package com.google.idea.sdkcompat.clion.ui;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationProvider;
import com.jetbrains.cidr.lang.OCLanguageUtilsBase;
import com.jetbrains.cidr.project.ui.notifications.EditorNotificationWarningProvider;
import com.jetbrains.cidr.project.ui.widget.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.function.Function;

// #api242
public class OpenFolderHelper {
    // EditorNotificationProvider
    public static abstract class CLionEditorNotificationProviderWrapperBase implements EditorNotificationProvider {
        public CLionEditorNotificationProviderWrapperBase(ProjectNotificationProvider provider, ProjectFixesProvider fixesProvider) {}

        @Override
        public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile virtualFile) {
            return null;
        }
    }

    // PSW
    public static void registerProjectNotificationAndFixesProvider(ProjectNotificationProvider notificationProvider, ProjectFixesProvider fixesProvider) {
        var providerAdapter = new ProjectNotificationAndFixesProviderAdapter(notificationProvider, fixesProvider);
        EditorNotificationWarningProvider.Companion.getEP_NAME().getPoint().registerExtension(providerAdapter);
        com.jetbrains.cidr.project.ui.popup.ProjectFixesProvider.Companion.getEP_NAME().getPoint().registerExtension(providerAdapter);
        WidgetStatusProvider.Companion.getEP_NAME().getPoint().registerExtension(providerAdapter);
    }

    // psw API adapter
    private static class ProjectNotificationAndFixesProviderAdapter implements EditorNotificationWarningProvider, com.jetbrains.cidr.project.ui.popup.ProjectFixesProvider, WidgetStatusProvider {

        private final ProjectNotificationProvider notificationProvider;
        private final ProjectFixesProvider fixesProvider;

        private ProjectNotificationAndFixesProviderAdapter(ProjectNotificationProvider notificationProvider, ProjectFixesProvider fixesProvider) {
            this.notificationProvider = notificationProvider;
            this.fixesProvider = fixesProvider;
        }

        @Nullable
        @Override
        public com.jetbrains.cidr.project.ui.notifications.ProjectNotification getProjectNotification(@NotNull Project project, @NotNull VirtualFile virtualFile) {
            var temp = notificationProvider.collectNotification(project, virtualFile);
            if (temp == null) {
                return null;
            }

            if (temp.type == NotificationType.OK) {
                return null;
            }

            return new com.jetbrains.cidr.project.ui.notifications.ProjectNotification(com.jetbrains.cidr.project.ui.notifications.NotificationType.Warning, temp.message);
        }

        @NotNull
        @Override
        public List<AnAction> collectFixes(@NotNull Project project, @Nullable VirtualFile virtualFile, @NotNull DataContext dataContext) {
            return fixesProvider.collectFixes(project, virtualFile);
        }

        @Nullable
        @Override
        public WidgetStatus getWidgetStatus(@NotNull Project project, @Nullable VirtualFile virtualFile) {
            var temp = notificationProvider.collectNotification(project, virtualFile);
            if (temp == null) {
                return null;
            }

            var status = temp.type == NotificationType.OK ? Status.OK : Status.Warning;
            var scope = temp.scope == NotificationScope.File ? Scope.File : Scope.Project;
            return new DefaultWidgetStatus(status, scope, temp.message);
        }
    }

    public static Boolean isProjectAwareFile(Project project, VirtualFile file) {
        return OCLanguageUtilsBase.isSupported(file);
    }

    public interface ProjectFixesProvider {
        List<AnAction> collectFixes(@NotNull Project project, VirtualFile file);
    }
    public interface ProjectNotificationProvider {
        ProjectNotification collectNotification(@NotNull Project project, VirtualFile file);
    }
    public record ProjectNotification(NotificationType type, String message, NotificationScope scope) {}
    public enum NotificationType {OK, WARNING}
    public enum NotificationScope {File, Project}
}
