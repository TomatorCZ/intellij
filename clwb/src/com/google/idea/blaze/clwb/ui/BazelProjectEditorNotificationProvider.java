package com.google.idea.blaze.clwb.ui;

import com.google.idea.sdkcompat.clion.ui.OpenFolderHelper;

public class BazelProjectEditorNotificationProvider extends OpenFolderHelper.CLionEditorNotificationProviderWrapperBase {
    public BazelProjectEditorNotificationProvider() {
        this(new BazelProjectNotificationAndFixesProvider());
    }

    private BazelProjectEditorNotificationProvider(BazelProjectNotificationAndFixesProvider provider) {
        super(provider, provider);
    }
}
