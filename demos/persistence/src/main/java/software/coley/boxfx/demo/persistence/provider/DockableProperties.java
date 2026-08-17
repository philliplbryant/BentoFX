package software.coley.boxfx.demo.persistence.provider;

import org.jspecify.annotations.Nullable;

/**
 * Convenience objects for aggregating some {@code Dockable} properties.
 *
 * <p>The shape and color of a dockable's icon live here too, so that
 * {@link BoxAppDockableStateProvider} can build every dockable from one loop over
 * {@link #values()} rather than from one block per dockable.</p>
 *
 * @author Phil Bryant
 */
public enum DockableProperties {

    WORKSPACE("Workspace", "This is the Workspace tooltip text.", 1, 0),
    BOOKMARKS("Bookmarks", "This is the Bookmarks tooltip text.", 1, 1),
    MODIFICATIONS("Modifications", "This is the Modifications tooltip text.", 1, 2),
    LOGGING("Logging", "This is the Logging tooltip text.", 2, 0),
    TERMINAL("Terminal", "This is the Terminal tooltip text.", 2, 1),
    PROBLEMS("Problems", "This is the Problems tooltip text.", 2, 2),
    CLASS_1("Class 1", "This is the Class 1 tooltip text.", 0, 0),
    CLASS_2("Class 2", "This is the Class 2 tooltip text.", 0, 1),
    CLASS_3("Class 3", "This is the Class 3 tooltip text.", 0, 2),
    CLASS_4("Class 4", "This is the Class 4 tooltip text.", 0, 3),
    CLASS_5("Class 5", "This is the Class 5 tooltip text.", 0, 4),
    SOMETHING_ELSE("some-other-dockable", "This is the tooltip text for some other dockable.");

    private final String identifier;
    private final @Nullable String tooltipText;
    private final int shapeMode;
    private final int colorIndex;

    /**
     * A dockable built without an icon or a context menu, standing in for one a
     * saved layout can name but this application does not decorate.
     *
     * @param identifier identifies the {@code Dockable}.
     * @param tooltipText the {@code Dockable}'s tooltip text.
     */
    DockableProperties(
            final String identifier,
            final @Nullable String tooltipText
    ) {
        // A negative shape mode rather than a named constant: an enum's static
        // fields are not initialized until after its constants, so a constant read
        // from here would still be zero.
        this(identifier, tooltipText, -1, 0);
    }

    /**
     * A dockable built with an icon and a context menu.
     *
     * @param identifier identifies the {@code Dockable}.
     * @param tooltipText the {@code Dockable}'s tooltip text.
     * @param shapeMode which shape the icon is.
     * @param colorIndex which color the icon is.
     */
    DockableProperties(
            final String identifier,
            final @Nullable String tooltipText,
            final int shapeMode,
            final int colorIndex
    ) {
        this.identifier = identifier;
        this.tooltipText = tooltipText;
        this.shapeMode = shapeMode;
        this.colorIndex = colorIndex;
    }

    public String getIdentifier() {
        return identifier;
    }

    public @Nullable String getTooltipText() {
        return tooltipText;
    }

    /**
     * {@return {@code true} when this dockable is built with an icon and a context
     * menu; otherwise, {@code false}.}
     */
    public boolean isDecorated() {
        return shapeMode >= 0;
    }

    /**
     * {@return which shape this dockable's icon is, meaningful only when
     * {@link #isDecorated()}.}
     */
    public int getShapeMode() {
        return shapeMode;
    }

    /**
     * {@return which color this dockable's icon is, meaningful only when
     * {@link #isDecorated()}.}
     */
    public int getColorIndex() {
        return colorIndex;
    }
}
