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

/**
 * Unit test for {@link HttpHeaderServletFilter}.
 */
public class TestHttpHeaderServletFilter {

    private HttpHeaderServletFilter filter;

    @Before
    public void setUp() throws Exception {
        filter = new HttpHeaderServletFilter();
    }

    @After
    public void tearDown() throws Exception {
        MDC.clear(); // Clean the MDC after each test execution...
    }

    @Test
    public void testDoFilter_ShouldSetHttpHeaderInMDC() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-transaction", "transaction_id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new VerifyMDCFilterChain("transaction", "transaction_id"));

        // After the filter is run, the MDC should be cleared
        assertNull(MDC.get("transaction"));
    }

    @Test
    public void testDoFilter_ShouldUseAnonymousIfNotHttpHeaderFound() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new VerifyMDCFilterChain("transaction", "anonymous"));

        // After the filter is run, the MDC should be cleared
        assertNull(MDC.get("transaction"));
    }

    @Test
    public void testDoFilter_ShouldUseAnonymousIfHttpHeaderIsEmpty() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-transaction", "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new VerifyMDCFilterChain("transaction", "anonymous"));

        // After the filter is run, the MDC should be cleared
        assertNull(MDC.get("transaction"));
    }

    @Test
    public void testDoFilter_ShouldUseMDCKeyFromConfig() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-transaction", "transaction_id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Create an explicit init-param of the servlet filter
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("mdc_key", "different_key");

        filter.init(config);
        filter.doFilter(request, response, new VerifyMDCFilterChain("different_key", "transaction_id"));

        // After the filter is run, the MDC should be cleared
        assertNull(MDC.get("transaction"));
    }

    @Test
    public void testDoFilter_ShouldUseDefaultMDCKeyIfInitParamNotFoundInConfig() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-transaction", "transaction_id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Create an explicit init-param of the servlet filter
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("another_init_param", "test");

        filter.init(config);
        filter.doFilter(request, response, new VerifyMDCFilterChain("transaction", "transaction_id"));

        // After the filter is run, the MDC should be cleared
        assertNull(MDC.get("transaction"));
    }

    @Test
    public void testDoFilter_ShouldUseHeaderNameFromConfig() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-another-transaction", "transaction_value");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Create an explicit init-param of the servlet filter
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("header_name", "x-another-transaction");

        filter.init(config);
        filter.doFilter(request, response, new VerifyMDCFilterChain("transaction", "transaction_value"));

        // After the filter is run, the MDC should be cleared
        assertNull(MDC.get("transaction"));
    }

    @Test
    public void testDoFilter_ShouldUseDefaultHeaderNameIfInitParamNotFoundInConfig() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-transaction", "transaction_value");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Create an explicit init-param of the servlet filter
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("another_init_param", "test");

        filter.init(config);
        filter.doFilter(request, response, new VerifyMDCFilterChain("transaction", "transaction_value"));

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