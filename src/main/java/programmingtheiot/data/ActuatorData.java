package programmingtheiot.data;

import java.io.Serializable;

import programmingtheiot.common.ConfigConst;

/**
 * Implementation of ActuatorData based on BaseIotData.
 * Supports command, value, response flag, and state data.
 */
public class ActuatorData extends BaseIotData implements Serializable
{
	private static final long serialVersionUID = 1L;

	// Private variables
	private int command = ConfigConst.DEFAULT_COMMAND;
	private float value = ConfigConst.DEFAULT_VAL;
	private boolean isResponse = false;
	private String stateData = "";

	// Constructors
	public ActuatorData()
	{
		super();
	}

	// Public methods
	public int getCommand()
	{
		return this.command;
	}

	public void setCommand(int command)
	{
		this.command = command;
		super.updateTimeStamp();
	}

	public float getValue()
	{
		return this.value;
	}

	public void setValue(float val)
	{
		this.value = val;
		super.updateTimeStamp();
	}

	public boolean isResponseFlagEnabled()
	{
		return this.isResponse;
	}

	public void setAsResponse()
	{
		this.isResponse = true;
		super.updateTimeStamp();
	}

	public String getStateData()
	{
		return this.stateData;
	}

	public void setStateData(String stateData)
	{
		if (stateData != null) {
			this.stateData = stateData;
			super.updateTimeStamp();
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());

		sb.append(',');
		sb.append(ConfigConst.COMMAND_PROP).append('=').append(this.getCommand()).append(',');
		sb.append(ConfigConst.IS_RESPONSE_PROP).append('=').append(this.isResponseFlagEnabled()).append(',');
		sb.append(ConfigConst.VALUE_PROP).append('=').append(this.getValue()).append(',');
		sb.append(ConfigConst.STATE_DATA_PROP).append('=').append(this.getStateData());

		return sb.toString();
	}

	// Protected method to support updateData() in base class
	@Override
	protected void handleUpdateData(BaseIotData data)
	{
		if (data instanceof ActuatorData) {
			ActuatorData aData = (ActuatorData) data;
			this.setCommand(aData.getCommand());
			this.setValue(aData.getValue());
			this.setStateData(aData.getStateData());

			if (aData.isResponseFlagEnabled()) {
				this.isResponse = true;
			}
		}
	}
}
