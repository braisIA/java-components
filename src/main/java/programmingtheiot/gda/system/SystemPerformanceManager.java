/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.system;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class SystemPerformanceManager implements IDataMessageListener
{
    // Static Logger instance for logging
    private static final Logger _Logger = Logger.getLogger(SystemPerformanceManager.class.getName());
    
    // Private variable for poll rate (in seconds)
    private int pollRate = ConfigConst.DEFAULT_POLL_CYCLES;

    // Scheduled executor for periodic tasks
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;

    // Optional: store listeners if needed
    private IDataMessageListener dataMsgListener;
    private IActuatorDataListener actuatorDataListener;

    // constructors

    /**
     * Default constructor.
     * This constructor sets the poll rate from the config file or uses the default value.
     */
    public SystemPerformanceManager()
    {
        // Get the poll rate from the config file or use the default
        this.pollRate = ConfigUtil.getInstance().getInteger(
            ConfigConst.GATEWAY_DEVICE,
            ConfigConst.POLL_CYCLES_KEY,
            ConfigConst.DEFAULT_POLL_CYCLES
        );

        // If the poll rate is invalid, reset to default value
        if (this.pollRate <= 0) {
            this.pollRate = ConfigConst.DEFAULT_POLL_CYCLES;
        }

        _Logger.info("SystemPerformanceManager initialized with poll rate (seconds): " + this.pollRate);
    }

    // public methods

    /**
     * Handles telemetry data processing. This is just a placeholder for handling
     * telemetry data when needed.
     */
    public void handleTelemetry()
    {
        _Logger.info("Handling telemetry data...");
        // TODO: Add logic to process telemetry data
    }

    /**
     * Sets the listener for data messages.
     * 
     * @param listener The data message listener to set.
     */
    public void setDataMessageListener(IDataMessageListener listener)
    {
        _Logger.info("Setting data message listener.");
        this.dataMsgListener = listener;
    }

    /**
     * Starts the system performance manager and schedules periodic tasks.
     * 
     * @return true if started successfully.
     */
    public boolean startManager()
    {
        _Logger.info("SystemPerformanceManager is starting...");

        // Create a new scheduled executor service
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Schedule a task that runs periodically based on the poll rate (in seconds)
        scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            handleTelemetry(); // Handle telemetry at each poll cycle
        }, 0, this.pollRate, TimeUnit.SECONDS);

        _Logger.info("SystemPerformanceManager started successfully.");
        return true;
    }

    /**
     * Stops the system performance manager and cancels any ongoing tasks.
     * 
     * @return true if stopped successfully.
     */
    public boolean stopManager()
    {
        _Logger.info("SystemPerformanceManager is stopping...");

        // Cancel the scheduled task if it's running
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
        }

        // Shutdown the scheduler
        if (scheduler != null) {
            scheduler.shutdown();
        }

        _Logger.info("SystemPerformanceManager is stopped.");
        return true;
    }

    @Override
    public boolean handleIncomingMessage(String msg)
    {
        _Logger.info("Received CoAP message: " + msg);

        // Procesamiento real aquí
        return true;
    }

    @Override
    public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)
    {
        _Logger.info("Received sensor data for [" + resourceName + "]: " + data);
        // TODO: Add logic to process sensor data
        return true;
    }

    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data)
    {
        _Logger.info("Received actuator command request for [" + resourceName + "]: " + data);
        // TODO: Forward or handle actuator request
        if (this.actuatorDataListener != null) {
            this.actuatorDataListener.handleActuatorCommand(data);
        }
        return true;
    }

    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
    {
        _Logger.info("Received actuator command response for [" + resourceName + "]: " + data);
        // TODO: Process actuator command response if needed
        return true;
    }

    @Override
    public void setActuatorDataListener(String name, IActuatorDataListener listener)
    {
        _Logger.info("Setting actuator data listener: " + name);
        this.actuatorDataListener = listener;
    }

    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
    {
        _Logger.info("Received system performance data for [" + resourceName + "]: " + data);
        // TODO: Add logic to process system performance data
        return true;
    }
}
