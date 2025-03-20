/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

 package programmingtheiot.gda.system;

 import java.lang.management.ManagementFactory;
 import java.lang.management.MemoryUsage;
 import java.util.logging.Logger;

 import programmingtheiot.common.ConfigConst;
 
 /**
  * Shell representation of class for student implementation.
  * 
  */
 public class SystemMemUtilTask extends BaseSystemUtilTask
 {
	 // constructors
	 
	 /**
	  * Default.
	  * 
	  */
	 public SystemMemUtilTask()
	 {
		 super(ConfigConst.NOT_SET, ConfigConst.DEFAULT_TYPE_ID);
	 }
	 
	 // public methods
	 
	 @Override
	 public float getTelemetryValue()
	 {
		 // Obtener el uso de memoria del heap de la JVM
		 MemoryUsage memUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		 
		 // Calcular la utilización de memoria en porcentaje
		 double memUtil = ((double) memUsage.getUsed() / (double) memUsage.getMax()) * 100.0d;
		 
		 // Registrar el valor (si es necesario)
		 Logger.getLogger(SystemMemUtilTask.class.getName()).info("Memory Utilization: " + memUtil + "%");
		 
		 // Retornar el valor calculado como float
		 return (float) memUtil;
	 }
 }
 
 
