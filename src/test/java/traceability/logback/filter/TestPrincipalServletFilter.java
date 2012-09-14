package traceability.logback.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import traceability.test.http.DummyPrincipal;

/**
 * Unit test for {@link PrincipalServletFilter}.
 */
public class TestPrincipalServletFilter {

    private PrincipalServletFilter filter;

    @Before
    public void setUp() throws Exception {
        filter = new PrincipalServletFilter();
    }

    @After
    public void tearDown() throws Exception {
        MDC.clear(); // Clean the MDC after each test execution...
    }

    @Test
    public void testDoFilter_ShouldSetPrincipalUsernameInMDC() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new DummyPrincipal("user"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new VerifyMDCFilterChain("transaction", "user"));

        // After the filter is run, the MDC should be cleared
        assertNull(MDC.get("transaction"));
    }

    @Test
    public void testDoFilter_ShouldUseAnonymousIfNotPrincipalFound() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new VerifyMDCFilterChain("transaction", "anonymous"));

        // After the filter is run, the MDC should be cleared
        assertNull(MDC.get("transaction"));
    }

    @Test
    public void testDoFilter_ShouldUseAnonymousIfPrincipalUsernameIsEmpty() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new DummyPrincipal(""));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new VerifyMDCFilterChain("transaction", "anonymous"));

        // After the filter is run, the MDC should be cleared
        assertNull(MDC.get("transaction"));
    }

    @Test
    public void testDoFilter_ShouldUseMDCKeyFromConfig() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new DummyPrincipal("user"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Create an explicit init-param of the servlet filter
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("mdc_key", "different_key");

        filter.init(config);
        filter.doFilter(request, response, new VerifyMDCFilterChain("different_key", "user"));

        // After the filter is run, the MDC should be cleared
        assertNull(MDC.get("transaction"));
    }

    @Test
    public void testDoFilter_ShouldUseDefaultMDCKeyIfInitParamNotFoundInConfig() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new DummyPrincipal("user"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Create an explicit init-param of the servlet filter
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("another_init_param", "test");

        filter.init(config);
        filter.doFilter(request, response, new VerifyMDCFilterChain("transaction", "user"));

        // After the filter is run, the MDC should be cleared
        assertNull(MDC.get("transaction"));
    }

    /**
     * Test helper to verify the content of the MDC once the Filter under test delegates on the next filter.
     */
    private class VerifyMDCFilterChain implements FilterChain {

        private String key;
        private String value;

        public VerifyMDCFilterChain(String expectedKey, String expectedValue) {
            super();
            this.key = expectedKey;
            this.value = expectedValue;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            assertEquals(value, MDC.get(key));
        }
    }
}