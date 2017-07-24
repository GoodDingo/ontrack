package net.nemerosa.ontrack.extension.plugin;

import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LibraryScope;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ExtensionLayout implements Layout {

    private static final Set<LibraryScope> LIB_DESTINATION_SCOPES = new HashSet<>(
            Arrays.asList(LibraryScope.COMPILE, LibraryScope.RUNTIME,
                    LibraryScope.CUSTOM));

    @Override
    public String getLauncherClassName() {
        return null;
    }

    @Override
    public String getLibraryDestination(String libraryName, LibraryScope scope) {
        if (LIB_DESTINATION_SCOPES.contains(scope)) {
            return "lib/";
        }
        return null;
    }

    @Override
    public String getClassesLocation() {
        return "";
    }

    @Override
    public boolean isExecutable() {
        return false;
    }

}
