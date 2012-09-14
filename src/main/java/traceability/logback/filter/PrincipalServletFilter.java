package traceability.logback.filter;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

/**
 * Servlet {@link Filter} for HTTP requests, reading the {@link Principal} from the request and updating the Mapped
 * Diagnostic Context ({@link MDC}) of the request, so the API can be traced and audited.
 * 
 * <p>
 * In order to use the filter in your Servlet application, edit the web.xml, adding the following filter:
 * 
 * <pre>
 * {@code
 * <filter>
 *     <filter-name>Logback MDC Filter</filter-name>
 *     <filter-class>traceability.logback.filter.PrincipalServletFilter</filter-class>
 * </filter>
 * <filter-mapping>
 *     <filter-name>Logback MDC Filter</filter-name>
 *     <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }
 * </pre>
 * 
 * <p>
 * If you prefer to use your own MCD key in the logs, edit the filter with an <b>init-param</b>:
 * 
 * <pre>
 * {@code
 * <filter>
 *     <filter-name>Logback MDC Filter</filter-name>
 *     <filter-class>traceability.logback.filter.PrincipalServletFilter</filter-class>
 *     <init-param>
 *       <param-name>mdc_key</param-name>
 *       <param-value>my_own_logback_key</param-value>
 *     </init-param>
 * </filter>
 * <filter-mapping>
 *     <filter-name>Logback MDC Filter</filter-name>
 *     <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }
 * </pre>
 * 
 * <p>
 * Finally, configure your <code>logback.xml</code> file with the configured <b>mcd_key</b>:
 * 
 * <pre>
 * &lt;configuration ...&gt;
 *     &lt;appender name=...&gt;
 *         &lt;encoder&gt;
 *             &lt;pattern&gt;%X{username} %level...&lt;/pattern&gt;
 *         &lt;/encoder&gt;
 *     &lt;/appender&gt;
 *     ...
 * </pre>
 * 
 * <p>
 * Or, if using your own <b>mcd_key</b>:
 * 
 * <pre>
 * &lt;pattern&gt;%X{my_own_logback_key} %level...&lt;/pattern&gt;
 * </pre>
 * 
 * <p>
 * More info can be found at http://logback.qos.ch/manual/mdc.html.
 */
public class PrincipalServletFilter implements Filter {

    private static final String DEFAULT_MDC_KEY = "transaction";
    private static final String ANONYMOUS = "anonymous";

    private String mdcKey = DEFAULT_MDC_KEY;

    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) {
        if (config.getInitParameter("mdc_key") != null) {
            mdcKey = config.getInitParameter("mdc_key");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException,
            IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String username = ANONYMOUS;
        Principal principal = httpRequest.getUserPrincipal();
        if (principal != null && principal.getName().length() > 0) {
            username = principal.getName();
        }

        MDC.put(mdcKey, username);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(mdcKey);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // Nothing special...
    }
}