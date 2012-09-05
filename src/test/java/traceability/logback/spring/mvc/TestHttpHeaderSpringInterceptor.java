package traceability.logback.spring.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test for {@link HttpHeaderSpringInterceptor}.
 */
public class TestHttpHeaderSpringInterceptor {

    private HttpHeaderSpringInterceptor interceptor;

    @Before
    public void setUp() throws Exception {
        interceptor = new HttpHeaderSpringInterceptor();
    }

    @After
    public void tearDown() throws Exception {
        MDC.clear(); // Clean the MDC after each test execution...
    }

    @Test
    public void testPreHandle_ShouldSetDefaultHeaderInMDC() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-transaction", "test-header");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        assertEquals("test-header", MDC.get("transaction"));
    }

    @Test
    public void testPreHandle_ShouldSetAnonymousTagIfDefaultHeaderNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        assertEquals("anonymous", MDC.get("transaction"));
    }

    @Test
    public void testPreHandle_ShouldSetAnonymousTagIfDefaultHeaderIsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-transaction", "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        assertEquals("anonymous", MDC.get("transaction"));
    }

    @Test
    public void testPreHandle_ShouldUseAlternateHeaderNameIfConfigured() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-another-header", "test-header");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.setHeaderName("x-another-header");
        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals("test-header", MDC.get("transaction"));
    }

    @Test
    public void testPreHandle_ShouldUseAlternateTransactionKeyIfConfigured() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-transaction", "test-header");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.setMdcKey("another_key");
        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);

        assertNull(MDC.get("transaction"));
        assertEquals("test-header", MDC.get("another_key"));
    }

    @Test
    public void testAfterCompletion_ShouldClearMDCTransaction() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        MDC.put("transaction", "test-header");
        interceptor.afterCompletion(request, response, new Object(), new Exception());
        assertNull(MDC.get("transaction"));
    }
}