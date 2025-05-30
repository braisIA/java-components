/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * You may find it more helpful to your design to adjust the
 * functionality, constants and interfaces (if there are any)
 * provided within in order to meet the needs of your specific
 * Programming the Internet of Things project.
 */ 

package programmingtheiot.gda.app;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.system.SystemPerformanceManager;

/**
 * Main GDA application.
 * 
 */
public class GatewayDeviceApp
{
    // static
    private static final Logger _Logger =
        Logger.getLogger(GatewayDeviceApp.class.getName());

    public static final long DEFAULT_TEST_RUNTIME = 7000L; // Cambiado de 60000L a 65000L

    // private var's
    private SystemPerformanceManager sysPerfMgr = null;
    private CoapServerGateway coapServerGateway = null;

    // Flag para evitar System.exit() en modo test
    private boolean isTestMode = false;

    // constructors

    /**
     * Constructor.
     * 
     * @param args
     */
    public GatewayDeviceApp(String[] args)
    {
        super();

        _Logger.info("Initializing GDA...");

        // Crear instancia de SystemPerformanceManager
        this.sysPerfMgr = new SystemPerformanceManager();

        parseArgs(args);
    }

    // Método para activar/desactivar modo test
    public void setTestMode(boolean testMode) {
        this.isTestMode = testMode;
    }

    // static

    /**
     * Main application entry point.
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        GatewayDeviceApp gwApp = new GatewayDeviceApp(args);

        gwApp.startApp();

        System.out.println("GDA is running. Press ENTER to exit...");

        try {
            System.in.read(); // espera a que el usuario presione ENTER
        } catch (IOException e) {
            e.printStackTrace();
        }

        gwApp.stopApp(0);
    }

    // public methods

    /**
     * Initializes and starts the application.
     * 
     */
    public void startApp()
    {
        _Logger.info("Starting GDA...");

        try {
            // Inicializar y arrancar el servidor CoAP, usando el SystemPerformanceManager como listener
            this.coapServerGateway = new CoapServerGateway(this.sysPerfMgr);

            if (!this.coapServerGateway.startServer()) {
                _Logger.warning("Failed to start CoAP server!");
                stopApp(-1);
                return;
            }

            // Iniciar el SystemPerformanceManager
            if (this.sysPerfMgr.startManager()) {
                _Logger.info("GDA started successfully.");
            } else {
                _Logger.warning("Failed to start system performance manager!");
                stopApp(-1);
            }
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to start GDA. Exiting.", e);
            stopApp(-1); // Asegurar que la aplicación se detiene en caso de fallo
        }
    }

    /**
     * Stops the application.
     * 
     * @param code The exit code to pass to {@link System.exit()}
     */
    public void stopApp(int code)
    {
        _Logger.info("Stopping GDA...");

        try {
            if (this.coapServerGateway != null) {
                this.coapServerGateway.stopServer();
            }
            if (this.sysPerfMgr != null) {
                this.sysPerfMgr.stopManager();
            }
            _Logger.log(Level.INFO, "GDA stopped successfully with exit code {0}.", code);
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to cleanly stop GDA.", e);
        }

        // Llama a System.exit() solo si NO estamos en modo test
        if (!isTestMode) {
            System.exit(code);
        }
    }

    // private methods

    /**
     * Load the config file.
     * 
     * NOTE: This will be added later.
     * 
     * @param configFile The name of the config file to load.
     */
    private void initConfig(String configFile)
    {
        _Logger.log(Level.INFO, "Attempting to load configuration: {0}", (configFile != null ? configFile : "Default."));
    }

    /**
     * Parse any arguments passed in on app startup.
     * <p>
     * This method should be written to check if any valid command line args are provided,
     * including the name of the config file. Once parsed, call {@link #initConfig(String)}
     * with the name of the config file, or null if the default should be used.
     * <p>
     * If any command line args conflict with the config file, the config file
     * in-memory content should be overridden with the command line argument(s).
     * 
     * @param args The non-null and non-empty args array.
     */
    private void parseArgs(String[] args)
    {
        String configFile = null;

        if (args != null) {
            _Logger.log(Level.INFO, "Parsing {0} command line args.", args.length);

            for (String arg : args) {
                if (arg != null) {
                    arg = arg.trim();

                    // TODO: Your code here
                }
            }
        } else {
            _Logger.info("No command line args to parse.");
        }

        initConfig(configFile);
    }
}
