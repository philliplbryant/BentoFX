package software.coley.bentofx.persistence.core.impl;

import org.junit.jupiter.api.Test;
import software.coley.bentofx.persistence.core.impl.provider.DefaultBentoProvider;
import software.coley.bentofx.persistence.testfixtures.codec.InMemoryLayoutCodec;
import software.coley.bentofx.persistence.testfixtures.storage.InMemoryLayoutStorage;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbstractAutoCloseableLayoutSaverTest {

    @Test
    void enableAutoSaveRejectsANonPositiveInterval() {
        try (DockingLayoutSaver saver = new DockingLayoutSaver(
                new InMemoryLayoutCodec(),
                new InMemoryLayoutStorage(),
                new DefaultBentoProvider()
        )) {
            assertThatThrownBy(() -> saver.enableAutoSave(0, TimeUnit.SECONDS))
                    .describedAs("enableAutoSave(0, ...)")
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> saver.enableAutoSave(-1, TimeUnit.SECONDS))
                    .describedAs("enableAutoSave(-1, ...)")
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
