/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.data;

import java.io.Serializable;

import programmingtheiot.common.ConfigConst;

/**
 * Shell representation of class for student implementation.
 *
 */
public class SensorData extends BaseIotData implements Serializable
{
	private static final long serialVersionUID = 1L;

	// Private variable
	private float value = ConfigConst.DEFAULT_VAL;

	// Constructors
	public SensorData()
	{
		super();
	}

	public SensorData(int sensorType)
	{
		super();
		this.setTypeID(sensorType);
	}

	// Public methods
	public float getValue()
	{
		return this.value;
	}

	public void setValue(float val)
	{
		this.value = val;
		super.updateTimeStamp();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());

		sb.append(',');
		sb.append(ConfigConst.VALUE_PROP).append('=').append(this.getValue());

		return sb.toString();
	}

	// Protected method to support updateData() in base class
	@Override
	protected void handleUpdateData(BaseIotData data)
	{
		if (data instanceof SensorData) {
			SensorData sData = (SensorData) data;
			this.setValue(sData.getValue());
		}
	}
}
