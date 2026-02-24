package software.coley.bentofx.control;

import jakarta.annotation.Nonnull;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import software.coley.bentofx.Identifiable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

public class IdentifiableStage extends Stage implements Identifiable {

    private static final Map<String, WeakReference<IdentifiableStage>> ALL_STAGES =
            new HashMap<>();

    private final @Nonnull String identifier;

    public static Optional<IdentifiableStage> getIdentifiableStage(String id) {
        final WeakReference<IdentifiableStage> weakReference = ALL_STAGES.get(id);
        return weakReference == null ?
                Optional.empty() :
                Optional.ofNullable(weakReference.get());
    }

    public static @Nonnull List<IdentifiableStage> getAllIdentifiableStages() {
        return ALL_STAGES.values().stream()
                .map(WeakReference::get)
                // Filter out null (GC'd references)
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        // Collect to list
                        Collectors.toList(),
                        // Make the list immutable
                        Collections::unmodifiableList
                ));
    }

    public IdentifiableStage(@Nonnull String identifier) {
        this(identifier, StageStyle.DECORATED);
    }

    public IdentifiableStage(@Nonnull String identifier, StageStyle stageStyle) {
        super(stageStyle);
        this.identifier = identifier;

        ALL_STAGES.put(
                identifier,
                new WeakReference<>(this)
        );
    }

    @Override
    public @Nonnull String getIdentifier() {
        return identifier;
    }
}
