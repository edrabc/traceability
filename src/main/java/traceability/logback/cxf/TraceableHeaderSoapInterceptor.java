package traceability.logback.cxf;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * A thread-safe CXF {@link AbstractSoapInterceptor}, that reads a transaction field from the Mapped Diagnostic Context
 * ({@link MDC}) of the request and injects it in the SOAP {@link Message} headers.
 * 
 * <p>
 * In order to use the interceptor, configure it in the <b>outInterceptors</b> section of the CXF context file:
 * 
 * <pre>
 * {@code
 * <bean id="traceableInterceptor" class="traceability.logback.cxf.TraceableHeaderSoapInterceptor"/>
 * 
 * <cxf:bus>
 *     ...
 *     <cxf:outInterceptors>
 *         <ref bean="traceableInterceptor" />
 *         ...
 *     </cxf:outInterceptors>
 * </cxf:bus>
 * }
 * </pre>
 * 
 * @see http://cxf.apache.org/docs/interceptors.html
 */
public class TraceableHeaderSoapInterceptor extends AbstractSoapInterceptor {

    private static Logger logger = LoggerFactory.getLogger(TraceableHeaderSoapInterceptor.class);

    private static final String DEFAULT_MDC_KEY = "transaction";

    private String mdcKey = DEFAULT_MDC_KEY;

    private String soapKey = "SOAPTransaction";

    private String prefix = "trace";
    private String namespace = null;

    /**
     * Constructor.
     */
    public TraceableHeaderSoapInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cxf.interceptor.Interceptor#handleMessage(org.apache.cxf.message.Message)
     */
    @Override
    public void handleMessage(SoapMessage message) {
        String transaction = MDC.get(mdcKey);
        if (transaction == null || transaction.length() == 0) {
            return;
        }

        try {
            QName qname = new QName(namespace, soapKey);
            SOAPElement element = SOAPFactory.newInstance().createElement(soapKey);
            if (namespace != null) {
                element = SOAPFactory.newInstance().createElement(soapKey, prefix, namespace);
            }
            element.addTextNode(transaction);

            Header header = new Header(qname, element);
            message.getHeaders().add(header);

            logger.debug("Transaction ID {} added to SOAP Header", transaction);
        } catch (Exception e) {
            logger.warn("Unable to inject transaction ID in SOAP Header due to {}", e.getMessage());
        }
    }

    /**
     * @param mdcKey
     *            The mdcKey to set.
     */
    public void setMdcKey(String mdcKey) {
        this.mdcKey = mdcKey;
    }

    /**
     * @param soapKey
     *            The soapKey to set.
     */
    public void setSoapKey(String soapKey) {
        this.soapKey = soapKey;
    }

    /**
     * @param prefix
     *            The prefix to set.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @param namespace
     *            The namespace to set.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}