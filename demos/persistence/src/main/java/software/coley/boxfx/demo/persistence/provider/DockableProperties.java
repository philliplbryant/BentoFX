package software.coley.boxfx.demo.persistence.provider;

import org.jspecify.annotations.Nullable;

/**
 * Convenience objects for aggregating some {@code Dockable} properties.
 *
 * @author Phil Bryant
 */
public enum DockableProperties {

    WORKSPACE("Workspace","This is the Workspace tooltip text."),
    BOOKMARKS("Bookmarks","This is the Bookmarks tooltip text."),
    MODIFICATIONS("Modifications","This is the Modifications tooltip text."),
    LOGGING("Logging", "This is the Logging tooltip text."),
    TERMINAL("Terminal", "This is the Terminal tooltip text."),
    PROBLEMS("Problems","This is the Problems tooltip text."),
    CLASS_1("Class 1", "This is the Class 1 tooltip text."),
    CLASS_2("Class 2", "This is the Class 2 tooltip text."),
    CLASS_3("Class 3", "This is the Class 3 tooltip text."),
    CLASS_4("Class 4", "This is the Class 4 tooltip text."),
    CLASS_5("Class 5", "This is the Class 5 tooltip text."),
    SOMETHING_ELSE("some-other-dockable", "This is the tooltip text for some other dockable.");

    private final String identifier;
    private final @Nullable String tooltipText;

    DockableProperties(
            String identifier,
            @Nullable String tooltipText
    ) {
        this.identifier = identifier;
        this.tooltipText = tooltipText;
    }

    public String getIdentifier() {
        return identifier;
    }

    public @Nullable String getTooltipText() {
        return tooltipText;
    }
}
