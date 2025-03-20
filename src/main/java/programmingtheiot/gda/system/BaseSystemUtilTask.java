/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

 package programmingtheiot.gda.system;

 import java.util.logging.Logger;
 
 import programmingtheiot.common.ConfigConst;
 
 /**
  *
  */
 public abstract class BaseSystemUtilTask
 {
	 // static
	 
	 private static final Logger _Logger =
		 Logger.getLogger(BaseSystemUtilTask.class.getName());
	 
	 
	 // private
	 
	 private String name   = ConfigConst.NOT_SET;
	 private int    typeID = ConfigConst.DEFAULT_TYPE_ID;
	 
	 // constructors
	 
	 /**
	  * Constructor that initializes the name and typeID variables.
	  * 
	  * @param name The name of the system utilization task.
	  * @param typeID The type ID of the system utilization task.
	  */
	 public BaseSystemUtilTask(String name, int typeID)
	 {
		 super();
		 
		 if (name != null) {
			 this.name = name;
		 }
		 
		 this.typeID = typeID;
	 }
	 
	 
	 // public methods
	 
	 /**
	  * Returns the name of the system utilization task.
	  * 
	  * @return String The name of the task.
	  */
	 public String getName()
	 {
		 return this.name;
	 }
	 
	 /**
	  * Returns the type ID of the system utilization task.
	  * 
	  * @return int The type ID.
	  */
	 public int getTypeID()
	 {
		 return this.typeID;
	 }
	 
	 /**
	  * Template method definition. Sub-class will implement this to retrieve
	  * the system utilization measure.
	  * 
	  * @return float The system utilization value.
	  */
	 public abstract float getTelemetryValue();
 }
 