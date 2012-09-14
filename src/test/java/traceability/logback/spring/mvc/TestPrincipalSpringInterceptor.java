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

import traceability.test.http.DummyPrincipal;

/**
 * Unit test for {@link PrincipalSpringInterceptor}.
 */
public class TestPrincipalSpringInterceptor {

    private PrincipalSpringInterceptor interceptor;

    @Before
    public void setUp() throws Exception {
        interceptor = new PrincipalSpringInterceptor();
    }

    @After
    public void tearDown() throws Exception {
        MDC.clear(); // Clean the MDC after each test execution...
    }

    @Test
    public void testPreHandle_ShouldSetPrincipalUsernameInMDC() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new DummyPrincipal("user1"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        assertEquals("user1", MDC.get("transaction"));
    }

    @Test
    public void testPreHandle_ShouldSetAnonymousTagIfPrincipalNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        assertEquals("anonymous", MDC.get("transaction"));
    }

    @Test
    public void testPreHandle_ShouldSetAnonymousTagIfPrincipalIsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new DummyPrincipal(""));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        assertEquals("anonymous", MDC.get("transaction"));
    }

    @Test
    public void testPreHandle_ShouldUserAlternateTransactionKeyIfConfigured() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new DummyPrincipal("user1"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.setMdcKey("another_key");
        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);

        assertNull(MDC.get("transaction"));
        assertEquals("user1", MDC.get("another_key"));
    }

    @Test
    public void testAfterCompletion_ShouldClearMDCTransaction() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        MDC.put("transaction", "user1");
        interceptor.afterCompletion(request, response, new Object(), new Exception());
        assertNull(MDC.get("transaction"));
    }
}