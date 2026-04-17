package software.coley.bentofx.persistence.api;

import software.coley.bentofx.persistence.api.codec.BentoStateException;

/**
 * The Application Programming Interface for outputting a BentoFX layout for
 * persistence.
 *
 * @author Phil Bryant
 */
public interface LayoutSaver {

    void saveLayout() throws BentoStateException;
}
