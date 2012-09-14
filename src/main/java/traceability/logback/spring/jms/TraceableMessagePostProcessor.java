package traceability.logback.spring.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.MDC;
import org.springframework.jms.core.MessagePostProcessor;

/**
 * A thread-safe Spring JMS {@link MessagePostProcessor}, that reads a transaction field from the Mapped Diagnostic
 * Context ({@link MDC}) of the request and injects it in the {@link Message} headers.
 * 
 * <p>
 * In order to use the post-processor, inject an instance of this class in the <code>JmsTemplate</code>:
 * 
 * <pre>
 * {@code
 *   ...
 *   jmsTemplate.convertAndSend(destination, body, new TraceableMessagePostProcessor());
 *   ...
 * }
 * </pre>
 * 
 * <p>
 * As the {@link TraceableMessagePostProcessor} implementation is thread-safe, may be declared and created (or
 * <b>Autowired / Injected</b>) as an instance field, to be shared by all threads:
 * 
 * <pre>
 * {@code
 *   private TraceableMessagePostProcessor messagePostProcessor = new TraceableMessagePostProcessor();
 *   ...
 *   public void mySendMethod() {
 *       ...
 *       jmsTemplate.convertAndSend(destination, body, messagePostProcessor);
 *       ...
 * }
 * </pre>
 * 
 * <p>
 * For a finer control of the field name inside the MDC or the header name injected in the JMS message, the
 * {@link TraceableMessagePostProcessor} has a constructor that accepts both String fields
 * 
 * <pre>
 * {@code
 *   ... = new TraceableMessagePostProcessor(mdcKey, jmsKey);
 * }
 * </pre>
 * 
 * <p>
 * or if using Dependency Injection:
 * 
 * <pre>
 * {@code
 * <bean class="traceability.logback.spring.jms.TraceableMessagePostProcessor">
 *     <property name="mdcKey" value="transaction" />
 *     <property name="jmsKey" value="transaction" />
 * </bean>
 * }
 * </pre>
 */
public class TraceableMessagePostProcessor implements MessagePostProcessor {

    private static final String DEFAULT_MDC_KEY = "transaction";

    private String mdcKey = DEFAULT_MDC_KEY;
    private String jmsKey = DEFAULT_MDC_KEY;

    /**
     * Constructor.
     */
    public TraceableMessagePostProcessor() {
        super();
    }

    /**
     * Constructor with all available configurations.
     */
    public TraceableMessagePostProcessor(String mdcKey, String jmsKey) {
        super();
        this.mdcKey = mdcKey;
        this.jmsKey = jmsKey;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.jms.core.MessagePostProcessor#postProcessMessage(javax.jms.Message)
     */
    @Override
    public Message postProcessMessage(Message message) throws JMSException {
        String transaction = MDC.get(mdcKey);

        if (transaction != null && transaction.length() > 0) {
            message.setStringProperty(jmsKey, transaction);
        }

        return message;
    }

    /**
     * @param mdcKey
     *            The mdcKey to set.
     */
    public void setMdcKey(String mdcKey) {
        this.mdcKey = mdcKey;
    }

    /**
     * @param jmsKey
     *            The jmsKey to set.
     */
    public void setJmsKey(String jmsKey) {
        this.jmsKey = jmsKey;
    }
}