package programmingtheiot.gda.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import programmingtheiot.common.ConfigConst;

/**
 * Clase para obtener la utilización de la CPU en el sistema.
 * 
 * Esta clase extiende de BaseSystemUtilTask y sobrescribe el método getTelemetryValue
 * para obtener la utilización de la CPU promediada.
 */
public class SystemCpuUtilTask extends BaseSystemUtilTask
{
	// constructores
	
	/**
	 * Constructor por defecto.
	 * 
	 */
	public SystemCpuUtilTask()
	{
		super(ConfigConst.NOT_SET, ConfigConst.DEFAULT_TYPE_ID);
	}
	
	
	// métodos públicos
	
	@Override
	public float getTelemetryValue()
	{
		// Obtener el bean del sistema operativo para acceder a la información sobre la carga del sistema
		OperatingSystemMXBean mxBean = ManagementFactory.getOperatingSystemMXBean();
		
		// Obtener la carga promedio del sistema (promediado entre todos los núcleos)
		double cpuUtil = mxBean.getSystemLoadAverage();
		
		// Devolver el valor como float
		return (float) cpuUtil;
	}
	
}
