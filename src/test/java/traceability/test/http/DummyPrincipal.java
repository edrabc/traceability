package traceability.test.http;

import java.security.Principal;

/**
 * Convenience class to simplify HTTP requests testing, by injecting a dummy {@link Principal} to
 * <code>HttpServletRequest</code> objects.
 */
public class DummyPrincipal implements Principal {

    private String username;

    /**
     * Constructor.
     */
    public DummyPrincipal(String username) {
        this.username = username;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.security.Principal#getName()
     */
    @Override
    public String getName() {
        return username;
    }
}