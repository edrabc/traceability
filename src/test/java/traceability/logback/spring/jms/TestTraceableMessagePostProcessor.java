package traceability.logback.spring.jms;

import static org.junit.Assert.assertNotNull;

import javax.jms.Message;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

/**
 * Unit test for {@link TraceableMessagePostProcessor}.
 */
public class TestTraceableMessagePostProcessor {

    private TraceableMessagePostProcessor processor;

    private Mockery mockery = new Mockery();
    private Message messageMock;

    @Before
    public void setUp() throws Exception {
        processor = new TraceableMessagePostProcessor();

        messageMock = mockery.mock(Message.class);
    }

    @After
    public void tearDown() throws Exception {
        MDC.clear(); // Clean the MDC after each test execution...

        mockery.assertIsSatisfied();
    }

    @Test
    public void testPostProcessMessage_ShouldSetMDCTransactionInMessageProperty() throws Exception {
        mockery.checking(new Expectations() {
            {
                oneOf(messageMock).setStringProperty(with("transaction"), with("unique_transaction"));
            }
        });

        MDC.put("transaction", "unique_transaction");
        Message message = processor.postProcessMessage(messageMock);
        assertNotNull(message);
    }

    @Test
    public void testPostProcessMessage_ShouldNotModifyMessageIfTransactionNotFoundInMDC() throws Exception {
        mockery.checking(new Expectations() {
            {
                never(messageMock);
            }
        });

        Message message = processor.postProcessMessage(messageMock);
        assertNotNull(message);
    }

    @Test
    public void testPostProcessMessage_ShouldNotModifyMessageIsTransactionInMDCIsEmpty() throws Exception {
        mockery.checking(new Expectations() {
            {
                never(messageMock);
            }
        });

        MDC.put("transaction", "");
        Message message = processor.postProcessMessage(messageMock);
        assertNotNull(message);
    }

    @Test
    public void testPostProcessMessage_ShouldUseAlternateMDCKeyIfConfigured() throws Exception {
        mockery.checking(new Expectations() {
            {
                oneOf(messageMock).setStringProperty(with("transaction"), with("another_key"));
            }
        });

        MDC.put("transaction", "original_key");
        MDC.put("another-transaction", "another_key");

        processor.setMdcKey("another-transaction");
        Message message = processor.postProcessMessage(messageMock);
        assertNotNull(message);
    }

    @Test
    public void testPostProcessMessage_ShouldUseAlternateJmsKeyIfConfigured() throws Exception {
        mockery.checking(new Expectations() {
            {
                oneOf(messageMock).setStringProperty(with("new_jms_key"), with("unique_transaction"));
            }
        });

        MDC.put("transaction", "unique_transaction");

        processor.setJmsKey("new_jms_key");
        Message message = processor.postProcessMessage(messageMock);
        assertNotNull(message);
    }

    @Test
    public void testPostProcessMessage_ShouldUseAlternateConfigurationFromAlternateConstructor() throws Exception {
        mockery.checking(new Expectations() {
            {
                oneOf(messageMock).setStringProperty(with("new_jms"), with("unique_transaction"));
            }
        });

        MDC.put("new_mdc", "unique_transaction");

        processor = new TraceableMessagePostProcessor("new_mdc", "new_jms");
        Message message = processor.postProcessMessage(messageMock);
        assertNotNull(message);
    }
}