package traceability.logback.spring.mvc;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Intercepts all servlet requests, reading the {@link Principal} from the request and updating the Mapped Diagnostic
 * Context ({@link MDC}) of the thread, so each invocation can be traced and audited.
 * 
 * <p>
 * In order to use the interceptor in your Spring Framework servlet, edit the application context file, adding the
 * following bean:
 * 
 * <pre>
 * {@code
 * <bean class="traceability.logback.spring.mvc.PrincipalSpringInterceptor">
 *     <property name="mdcKey" value="transaction" />
 * </bean>
 * }
 * </pre>
 * 
 * If using Spring MVC namespace support:
 * 
 * <pre>
 * {@code
 * <mvc:interceptors>
 *     <bean class="traceability.logback.spring.mvc.PrincipalSpringInterceptor" />
 * </mvc:interceptors>
 * }
 * </pre>
 * 
 * <p>
 * Finally, configure your <code>logback.xml</code> file with the configured <b>mdcKey</b> (or <b>%X{transaction}</b> by
 * default):
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
 * More info can be found at http://logback.qos.ch/manual/mdc.html.
 */
public class PrincipalSpringInterceptor extends HandlerInterceptorAdapter {

    private static final String DEFAULT_MDC_KEY = "transaction";
    private static final String ANONYMOUS = "anonymous";

    private String mdcKey = DEFAULT_MDC_KEY;

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter#preHandle(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, java.lang.Object)
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String username = ANONYMOUS;
        Principal principal = request.getUserPrincipal();
        if (principal != null && principal.getName().length() > 0) {
            username = principal.getName();
        }

        MDC.put(mdcKey, username);

        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter#afterCompletion(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(mdcKey);
    }

    /**
     * @param mdcKey
     *            The mdcKey to set.
     */
    public void setMdcKey(String mdcKey) {
        this.mdcKey = mdcKey;
    }
}