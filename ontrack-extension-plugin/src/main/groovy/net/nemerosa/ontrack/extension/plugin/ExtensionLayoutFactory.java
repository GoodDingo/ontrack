package net.nemerosa.ontrack.extension.plugin;

import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LayoutFactory;

import java.io.File;

@SuppressWarnings("unused")
public class ExtensionLayoutFactory implements LayoutFactory {
    @Override
    public Layout getLayout(File source) {
        return new ExtensionLayout();
    }
}
