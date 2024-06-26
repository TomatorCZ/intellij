package com.google.idea.sdkcompat.clion.ui;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.jetbrains.cidr.lang.OCLanguageUtilsBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.function.Function;

// #api242
public class OpenFolderHelper {
    // EditorNotificationProvider
    public static abstract class CLionEditorNotificationProviderWrapperBase implements EditorNotificationProvider {
        private final ProjectNotificationProvider provider;
        private final ProjectFixesProvider fixesProvider;

        public CLionEditorNotificationProviderWrapperBase(ProjectNotificationProvider provider, ProjectFixesProvider fixesProvider) {
            this.provider = provider;
            this.fixesProvider = fixesProvider;
        }

        @Override
        public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile virtualFile) {
            var notification = provider.collectNotification(project, virtualFile);
            if (notification == null || notification.type() != NotificationType.WARNING) {
                return null;
            }

            var fixes = fixesProvider.collectFixes(project, virtualFile);

            return fileEditor -> {
                EditorNotificationPanel panel = new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning);

                panel.setText(notification.message());

                for (var fix : fixes) {
                    panel.createActionLabel(
                            fix.getTemplateText(),
                            () -> fix.actionPerformed(AnActionEvent.createFromAnAction(fix, null, ActionPlaces.UNKNOWN, DataContext.EMPTY_CONTEXT))
                    );
                }

                return panel;
            };
        }
    }

    // PSW
    public static void registerProjectNotificationAndFixesProvider(ProjectNotificationProvider notificationProvider, ProjectFixesProvider fixesProvider) {}

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
