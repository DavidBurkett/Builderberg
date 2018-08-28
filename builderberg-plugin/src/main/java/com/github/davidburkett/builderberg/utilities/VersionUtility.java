package com.github.davidburkett.builderberg.utilities;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.psi.PsiClass;

public class VersionUtility {
    public static boolean doesMeetMinimumVersion(final PsiClass topLevelClass) {
        final String minimumBuilderbergVersion = BuilderOptionUtility.minimumPluginVersion(topLevelClass);
        final String builderbergVersion = getBuilderbergVersion();

        if (getMajorVersion(minimumBuilderbergVersion) != getMajorVersion(builderbergVersion)) {
            return getMajorVersion(minimumBuilderbergVersion) < getMajorVersion(builderbergVersion);
        }

        if (getMinorVersion(minimumBuilderbergVersion) != getMinorVersion(builderbergVersion)) {
            return getMinorVersion(minimumBuilderbergVersion) < getMinorVersion(builderbergVersion);
        }

        return getBuildVersion(minimumBuilderbergVersion) <= getBuildVersion(builderbergVersion);
    }

    public static String getBuilderbergVersion() {
        final PluginId pluginId = PluginId.getId("com.burkett.builderberg");
        final IdeaPluginDescriptor ideaPluginDescriptor = PluginManager.getPlugin(pluginId);
        return ideaPluginDescriptor.getVersion().trim();
    }

    private static int getMajorVersion(final String version) {
        final String[] versionParts = version.split("\\.");
        return Integer.parseInt(versionParts[0]);
    }

    private static int getMinorVersion(final String version) {
        final String[] versionParts = version.split("\\.");
        return Integer.parseInt(versionParts[1]);
    }

    private static int getBuildVersion(final String version) {
        final String[] versionParts = version.split("\\.");
        return Integer.parseInt(versionParts[2]);
    }
}
