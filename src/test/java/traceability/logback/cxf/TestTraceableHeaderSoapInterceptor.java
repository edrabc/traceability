package traceability.logback.cxf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.SOAPElement;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

public class TestTraceableHeaderSoapInterceptor {

    private TraceableHeaderSoapInterceptor interceptor;

    private Mockery mockery = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private SoapMessage messageMock;
    private List<Header> soapHeaders;

    @Before
    public void setUp() throws Exception {
        interceptor = new TraceableHeaderSoapInterceptor();

        // Create an empty list of headers to be used by the mock Message
        soapHeaders = new ArrayList<Header>();

        messageMock = mockery.mock(SoapMessage.class);
        mockery.checking(new Expectations() {
            {
                allowing(messageMock).getHeaders();
                will(returnValue(soapHeaders));
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        MDC.clear(); // Clean the MDC after each test execution...

        soapHeaders.clear(); // Clear the Headers after each test execution...

        mockery.assertIsSatisfied();
    }

    @Test
    public void testHandleMessage_ShouldNotModifyHeaderIfTransactionNotFoundInMDC() throws Exception {
        assertEquals(0, soapHeaders.size());

        interceptor.handleMessage(messageMock);

        assertEquals(0, soapHeaders.size());
    }

    @Test
    public void testHandleMessage_ShouldNotModifyHeaderIfTransactionIsEmptyInMDC() throws Exception {
        assertEquals(0, soapHeaders.size());

        MDC.put("transaction", "");
        interceptor.handleMessage(messageMock);

        assertEquals(0, soapHeaders.size());
    }

    @Test
    public void testHandleMessage_ShouldSetMDCTransactionInSoapHeader() throws Exception {
        assertEquals(0, soapHeaders.size());

        MDC.put("transaction", "unique_transaction");
        interceptor.handleMessage(messageMock);

        assertEquals(1, soapHeaders.size());
        assertTrue(soapHeaders.get(0).getObject() instanceof SOAPElement);

        SOAPElement element = (SOAPElement) soapHeaders.get(0).getObject();
        assertNull(element.getNamespaceURI());
        assertEquals("SOAPTransaction", element.getNodeName());
        assertEquals("unique_transaction", element.getTextContent());
    }

    @Test
    public void testHandleMessage_ShouldUseAlternateMDCKeyIfConfigured() throws Exception {
        MDC.put("transaction", "original_key");
        MDC.put("another-transaction", "another_key");

        interceptor.setMdcKey("another-transaction");
        interceptor.handleMessage(messageMock);

        assertEquals(1, soapHeaders.size());
        assertTrue(soapHeaders.get(0).getObject() instanceof SOAPElement);

        SOAPElement element = (SOAPElement) soapHeaders.get(0).getObject();
        assertNull(element.getNamespaceURI());
        assertEquals("SOAPTransaction", element.getNodeName());
        assertEquals("another_key", element.getTextContent());
    }

    @Test
    public void testHandleMessage_ShouldUseAlternateSoapKeyIfConfigured() throws Exception {
        MDC.put("transaction", "unique_transaction");

        interceptor.setSoapKey("DifferentElementName");
        interceptor.handleMessage(messageMock);

        assertEquals(1, soapHeaders.size());
        assertTrue(soapHeaders.get(0).getObject() instanceof SOAPElement);

        SOAPElement element = (SOAPElement) soapHeaders.get(0).getObject();
        assertNull(element.getNamespaceURI());
        assertEquals("DifferentElementName", element.getNodeName());
        assertEquals("unique_transaction", element.getTextContent());
    }

    @Test
    public void testHandleMessage_ShouldUseNamespaceAndPrefixIfConfigured() throws Exception {
        MDC.put("transaction", "unique_transaction");

        interceptor.setNamespace("http://localdomain/core_1");
        interceptor.handleMessage(messageMock);

        assertEquals(1, soapHeaders.size());
        assertTrue(soapHeaders.get(0).getObject() instanceof SOAPElement);

        SOAPElement element = (SOAPElement) soapHeaders.get(0).getObject();
        assertEquals("http://localdomain/core_1", element.getNamespaceURI());
        assertEquals("trace:SOAPTransaction", element.getNodeName());
        assertEquals("unique_transaction", element.getTextContent());
    }

    @Test
    public void testHandleMessage_ShouldUseDifferentPrefixIfConfiguredAndNamespaceNotNull() throws Exception {
        MDC.put("transaction", "unique_transaction");

        interceptor.setNamespace("http://localdomain/core_1");
        interceptor.setPrefix("core");
        interceptor.handleMessage(messageMock);

        assertEquals(1, soapHeaders.size());
        assertTrue(soapHeaders.get(0).getObject() instanceof SOAPElement);

        SOAPElement element = (SOAPElement) soapHeaders.get(0).getObject();
        assertEquals("http://localdomain/core_1", element.getNamespaceURI());
        assertEquals("core:SOAPTransaction", element.getNodeName());
        assertEquals("unique_transaction", element.getTextContent());
    }

    @Test
    public void testHandleMessage_ShouldNotRethrowAnyException() throws Exception {
        MDC.put("transaction", "unique_transaction");

        // Force exception due to an invalid prefix format...
        interceptor.setNamespace("http://localdomain/core_1");
        interceptor.setPrefix("http://invalid_prefix");
        interceptor.handleMessage(messageMock);

        assertEquals(0, soapHeaders.size());
    }
}