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
 import programmingtheiot.common.IDataMessageListener;
 
 /**
  * Shell representation of class for student implementation.
  * 
  */
 public class SystemPerformanceManager
 {
	 // Static Logger instance for logging
	 private static final Logger _Logger = Logger.getLogger(SystemPerformanceManager.class.getName());
	 
	 // Private variable for poll rate
	 private int pollRate = ConfigConst.DEFAULT_POLL_CYCLES;
 
	 // Scheduled executor for periodic tasks
	 private ScheduledExecutorService scheduler;
	 private ScheduledFuture<?> scheduledTask;
	 
	 // constructors
	 
	 /**
	  * Default constructor.
	  * This constructor sets the poll rate from the config file or uses the default value.
	  * 
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
 
		 _Logger.info("SystemPerformanceManager initialized with poll rate: " + this.pollRate);
	 }
	 
	 
	 // public methods
	 
	 /**
	  * Handles telemetry data processing. This is just a placeholder for handling
	  * telemetry data when needed.
	  * 
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
		 
		 // TODO: Add logic to set the listener for data messages
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
		 
		 // Schedule a task that runs periodically based on the poll rate
		 scheduledTask = scheduler.scheduleAtFixedRate(() -> {
			 handleTelemetry(); // Handle telemetry at each poll cycle
		 }, 0, this.pollRate, TimeUnit.MILLISECONDS);
 
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
 }
 