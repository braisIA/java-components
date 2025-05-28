package programmingtheiot.part01.integration.app;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.gda.app.GatewayDeviceApp;

/**
 * This test case class contains very basic integration tests for
 * GatewayDeviceApp. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 *
 */
public class GatewayDeviceAppTest
{
    private static final Logger _Logger =
        Logger.getLogger(GatewayDeviceAppTest.class.getName());

    private GatewayDeviceApp gda = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        // Optional: Log start of tests
        System.out.println("Starting GatewayDeviceAppTest...");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        // Optional: Log end of tests
        System.out.println("Finished GatewayDeviceAppTest.");
    }

    @Before
    public void setUp() throws Exception
    {
        gda = new GatewayDeviceApp((String[]) null);
    }

    @After
    public void tearDown() throws Exception
    {
        if (gda != null) {
            gda.stopApp(0);  // Ensure clean stop after each test
        }
    }

    /**
     * Convenience test method for starting and stopping the GDA. This will invoke both
     * {@link programmingtheiot.gda.app.GatewayDeviceApp#startApp()} and
     * {@link programmingtheiot.gda.app.GatewayDeviceApp#stopApp(int)} in sequence.
     * <p>
     * Validation is via log output and the expectation that no exception will be
     * thrown during execution.
     */
    @Test
    public void testStartAndStopGatewayApp()
    {
        this.gda.startApp();

        try {
            // Sleep shorter to reduce chances of fork VM timeout/crash
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            // ignore
        }

        this.gda.stopApp(0);
    }
}
