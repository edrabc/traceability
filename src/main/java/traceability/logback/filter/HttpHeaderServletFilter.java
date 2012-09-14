package traceability.logback.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

/**
 * Servlet {@link Filter} for HTTP requests, reading the required <b>header</b> from the request and updating the Mapped
 * Diagnostic Context ({@link MDC}) of the request, so the API can be traced and audited.
 * 
 * <p>
 * In order to use the filter in your Servlet application, edit the web.xml, adding the following filter:
 * 
 * <pre>
 * {@code
 * <filter>
 *     <filter-name>Logback MDC Filter</filter-name>
 *     <filter-class>traceability.logback.filter.HttpHeaderServletFilter</filter-class>
 * </filter>
 * <filter-mapping>
 *     <filter-name>Logback MDC Filter</filter-name>
 *     <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }
 * </pre>
 * 
 * <p>
 * If you prefer to use your own MCD key or a different HTTP Header name, edit the filter with an <b>init-param</b>:
 * 
 * <pre>
 * {@code
 * <filter>
 *     <filter-name>Logback MDC Filter</filter-name>
 *     <filter-class>traceability.logback.filter.HttpHeaderServletFilter</filter-class>
 *     <init-param>
 *       <param-name>header_name</param-name>
 *       <param-value>x-transaction</param-value>
 *     </init-param>
 *     <init-param>
 *       <param-name>mdc_key</param-name>
 *       <param-value>transaction</param-value>
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
 * Finally, configure your <code>logback.xml</code> file with the default <b>mcd_key</b>:
 * 
 * <pre>
 * &lt;configuration ...&gt;
 *     &lt;appender name=...&gt;
 *         &lt;encoder&gt;
 *             &lt;pattern&gt;%X{transaction} %level...&lt;/pattern&gt;
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
public class HttpHeaderServletFilter implements Filter {

    private static final String DEFAULT_HEADER_NAME = "x-transaction";
    private static final String DEFAULT_MDC_KEY = "transaction";
    private static final String ANONYMOUS = "anonymous";

    private String headerName = DEFAULT_HEADER_NAME;
    private String mdcKey = DEFAULT_MDC_KEY;

    /**
     * {@inheritDoc}
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) {
        if (config.getInitParameter("header_name") != null) {
            headerName = config.getInitParameter("header_name");
        }

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

        String transaction = httpRequest.getHeader(headerName);
        if (transaction == null || transaction.length() == 0) {
            transaction = ANONYMOUS;
        }

        MDC.put(mdcKey, transaction);

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