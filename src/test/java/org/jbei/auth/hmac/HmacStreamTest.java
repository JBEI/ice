/**
 *
 */
package org.jbei.auth.hmac;

import org.junit.*;
import org.mockito.Mockito;

import javax.crypto.Mac;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author wcmorrell
 */
public class HmacStreamTest {

    /**
     * Class setup
     */
    @BeforeClass
    public static void setUpBeforeClass() {
    }

    /**
     * Class teardown
     */
    @AfterClass
    public static void tearDownAfterClass() {
    }

    /**
     * Method setup
     */
    @Before
    public void setUp() {
    }

    /**
     * Method teardown
     */
    @After
    public void tearDown() {
    }

    /**
     * Tests that a finished stream is properly handled.
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    @Test
    public final void testEndOfStream() throws IOException, NoSuchAlgorithmException,
            InvalidKeyException {
        final Mac mac = Mac.getInstance("HmacSHA1");
        try (final InputStream stream = Mockito.mock(InputStream.class);
             final InputStream hmacStream = new HmacInputStream(stream, mac);) {
            final byte[] b = new byte[1];
            Mockito.when(stream.read(b, 0, 1023)).thenReturn(-1);
            Mockito.when(stream.read()).thenReturn(-1);
            mac.init(HmacSignatureFactory.createKey());
            Assert.assertEquals("Wrong read value at EOF", -1, hmacStream.read());
            Assert.assertEquals("Wrong read length at EOF", -1, hmacStream.read(b, 0, 1023));
        } catch (final IllegalArgumentException e) {
            Assert.fail("Exception handling stream EOF");
        }
    }
}
