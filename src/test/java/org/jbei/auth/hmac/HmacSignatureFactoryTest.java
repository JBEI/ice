/**
 *
 */
package org.jbei.auth.hmac;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.jbei.auth.KeyTable;
import org.jbei.auth.MemoryKeyTable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Collections;

/**
 * @author wcmorrell
 */
public class HmacSignatureFactoryTest {

    private static final String encoded = "yJwU0chpercYs/R4YmCUxhbRZBHM4WqpO3ZH0ZW6+4X+/aTodSGTI2w5jeBxWgJXNN1JNQIg02Ic3ZnZtSEVYA==";
    private static final String keyId = "test.jbei.org";
    private static final String simpleSignature = "j7iHK4iYiELZlEtDWD8GJm04CWc=";
    private static final String userId = "WCMorrell";

    private static KeyTable table;
    private static HttpServletRequest simpleRequest;
    private static HttpGet simpleGet;

    /**
     * Class setup
     *
     * @throws URISyntaxException
     */
    @BeforeClass
    public static void setUp() throws URISyntaxException {
        final Header hostHeader = Mockito.mock(Header.class);
        final Key key = HmacSignatureFactory.decodeKey(encoded);
        final URI getUri = new URI("http://registry-test.jbei.org/rest/accesstoken");
        table = new MemoryKeyTable(Collections.singletonMap(keyId, key));
        simpleRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(simpleRequest.getMethod()).thenReturn("GET");
        Mockito.when(simpleRequest.getHeader("Host")).thenReturn("registry-test.jbei.org");
        Mockito.when(simpleRequest.getRequestURI()).thenReturn("/rest/accesstoken");
        Mockito.when(simpleRequest.getParameterMap()).thenReturn(
                Collections.<String, String[]>emptyMap());

        simpleGet = Mockito.mock(HttpGet.class);
        Mockito.when(simpleGet.getMethod()).thenReturn("GET");
        Mockito.when(hostHeader.getValue()).thenReturn("registry-test.jbei.org");
        Mockito.when(simpleGet.getFirstHeader("Host")).thenReturn(hostHeader);
        Mockito.when(simpleGet.getURI()).thenReturn(getUri);
    }

    /**
     * Test method for {@link HmacSignatureFactory#createKey()}.
     */
    @Test
    public final void testCreateKey() {
        try {
            final Key key = HmacSignatureFactory.createKey();
            Assert.assertNotNull(key);
        } catch (final NoSuchAlgorithmException e) {
            Assert.fail("NoSuchAlgorithmException in createKey");
        }
    }

    /**
     * Test method for {@link HmacSignatureFactory#encodeKey(Key)} and
     * {@link HmacSignatureFactory#decodeKey(String)}.
     */
    @Test
    public final void testEncodeDecodeKey() {
        final Key decoded = HmacSignatureFactory.decodeKey(encoded);
        final String recoded;
        Assert.assertNotNull(decoded);
        recoded = HmacSignatureFactory.encodeKey(decoded);
        Assert.assertEquals(encoded, recoded);
    }

    /**
     * Test method for
     * {@link HmacSignatureFactory#buildSignature(HttpServletRequest, String, String)} .
     */
    @Test
    public final void testBuildSignatureHttpServletRequestString() {
        final HmacSignatureFactory factory = new HmacSignatureFactory(table);
        try {
            final HmacSignature signature = factory.buildSignature(simpleRequest, keyId, userId);
            Assert.assertEquals(simpleSignature, signature.generateSignature());
            Assert.assertEquals(userId, signature.getUserId());
        } catch (final SignatureException e) {
            Assert.fail("Could not generate signature from HttpServletRequest simpleRequest");
        }
    }

    /**
     * Test method for {@link HmacSignatureFactory#buildSignature(HttpRequestBase, String, String)}.
     */
    @Test
    public final void testBuildSignatureHttpRequestBaseString() {
        final HmacSignatureFactory factory = new HmacSignatureFactory(table);
        try {
            final HmacSignature signature = factory.buildSignature(simpleGet, keyId, userId);
            Assert.assertEquals(simpleSignature, signature.generateSignature());
            Assert.assertEquals(userId, signature.getUserId());
        } catch (final SignatureException e) {
            Assert.fail("Could not generate signature from HttpGet simpleGet");
        }
    }

    /**
     * Tests that changing the user on an HmacSignature results in a different output string.
     */
    @Test
    public final void testSignatureWithDifferentUser() {
        final HmacSignatureFactory factory = new HmacSignatureFactory(table);
        try {
            final HmacSignature signature = factory.buildSignature(simpleRequest, keyId, "IVaino");
            Assert.assertNotEquals(simpleSignature, signature.generateSignature());
            Assert.assertNotEquals(userId, signature.getUserId());
        } catch (final SignatureException e) {
            Assert.fail("Could not generate signature from HttpServletRequest simpleRequest");
        }
    }

}
