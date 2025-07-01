package programmingtheiot.gda.app;

import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.CoapClientConnector;
import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.connection.ICloudClient;
import programmingtheiot.gda.connection.IPersistenceClient;
import programmingtheiot.gda.connection.IRequestResponseClient;
import programmingtheiot.gda.connection.MqttClientConnector;

public class DeviceDataManager implements IDataMessageListener
{
	private static final Logger _Logger =
		Logger.getLogger(DeviceDataManager.class.getName());

	private boolean enableMqttClient = false;
	private boolean enableCoapServer = false;
	private boolean enableCloudClient = false;
	private boolean enableSmtpClient = false;
	private boolean enablePersistenceClient = false;
	private boolean enableCoapClient = false;

	private IActuatorDataListener actuatorDataListener = null;
	private MqttClientConnector mqttClient = null;
	private ICloudClient cloudClient = null;
	private IPersistenceClient persistenceClient = null;
	private IRequestResponseClient smtpClient = null;
	private CoapServerGateway coapServer = null;
	private CoapClientConnector coapClient = null;

	public DeviceDataManager()
	{
		super();
		initConnections();
	}

	public DeviceDataManager(
		boolean enableMqttClient,
		boolean enableCoapClient,
		boolean enableCloudClient,
		boolean enableSmtpClient,
		boolean enablePersistenceClient)
	{
		super();

		this.enableMqttClient = enableMqttClient;
		this.enableCoapServer = enableCoapClient;
		this.enableCloudClient = enableCloudClient;
		this.enableSmtpClient = enableSmtpClient;
		this.enablePersistenceClient = enablePersistenceClient;

		initConnections();
	}

	@Override
	public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
	{
		return false;
	}

	@Override
	public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data)
	{
		handleIncomingDataAnalysis(resourceName, data);
		return true;
	}

	@Override
	public boolean handleIncomingMessage(String msg)
	{
		return false;
	}

	@Override
	public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)
	{
		if (data != null) {
			_Logger.info("Procesando datos de sensor: " + data.getName());

			if (this.enableCloudClient && this.cloudClient != null) {
				this.cloudClient.sendEdgeDataToCloud(resourceName, data);
			}

			return true;
		}
		return false;
	}

	@Override
	public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
	{
		if (data != null) {
			_Logger.info("Procesando datos de rendimiento del sistema: " + data.getName());

			if (this.enableCloudClient && this.cloudClient != null) {
				this.cloudClient.sendEdgeDataToCloud(resourceName, data);
			}

			return true;
		}
		return false;
	}

	public void setActuatorDataListener(String name, IActuatorDataListener listener)
	{
		if (listener != null) {
			this.actuatorDataListener = listener;
		}
	}

	public void startManager()
	{
		if (this.mqttClient != null) {
			if (this.mqttClient.connectClient()) {
				_Logger.info("Cliente MQTT conectado exitosamente al broker.");

				int qos = ConfigConst.DEFAULT_QOS;

				this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos);
				this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
				this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
				this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
			} else {
				_Logger.severe("Fallo al conectar el cliente MQTT al broker.");
			}
		}

		if (this.enableCoapServer && this.coapServer != null) {
			if (this.coapServer.startServer()) {
				_Logger.info("Servidor CoAP iniciado.");
			} else {
				_Logger.severe("Fallo al iniciar el servidor CoAP. Verifica el archivo de registro.");
			}
		}

		if (this.enableCloudClient && this.cloudClient != null) {
			if (this.cloudClient.connectClient()) {
				this.cloudClient.subscribeToCloudEvents(ResourceNameEnum.GDA_ACTUATOR_CMD_RESOURCE);
				_Logger.info("Cliente Cloud conectado y suscrito a eventos.");
			} else {
				_Logger.warning("Fallo al conectar el cliente Cloud.");
			}
		}
	}

	public void stopManager()
	{
		if (this.mqttClient != null) {
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);

			if (this.mqttClient.disconnectClient()) {
				_Logger.info("Cliente MQTT desconectado exitosamente del broker.");
			} else {
				_Logger.severe("Fallo al desconectar el cliente MQTT del broker.");
			}
		}

		if (this.enableCoapServer && this.coapServer != null) {
			if (this.coapServer.stopServer()) {
				_Logger.info("Servidor CoAP detenido.");
			} else {
				_Logger.severe("Fallo al detener el servidor CoAP. Verifica el archivo de registro.");
			}
		}

		if (this.enableCloudClient && this.cloudClient != null) {
			this.cloudClient.unsubscribeFromCloudEvents(ResourceNameEnum.GDA_ACTUATOR_CMD_RESOURCE);

			if (this.cloudClient.disconnectClient()) {
				_Logger.info("Cliente Cloud desconectado exitosamente.");
			} else {
				_Logger.warning("Fallo al desconectar el cliente Cloud.");
			}
		}
	}

	private void initConnections()
	{
		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.enableMqttClient = configUtil.getBoolean(
			ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);

		this.enableCoapServer = configUtil.getBoolean(
			ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);

		this.enableCloudClient = configUtil.getBoolean(
			ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);

		if (this.enableMqttClient) {
			this.mqttClient = new MqttClientConnector();
			this.mqttClient.setDataMessageListener(this);
		}

		if (this.enableCoapServer) {
			this.coapServer = new CoapServerGateway(this);
			_Logger.info("Servidor CoAP habilitado e inicializado.");
		}

		if (this.enableCloudClient) {
			this.cloudClient = new CloudClientConnector();
			this.cloudClient.setDataMessageListener(this);
			_Logger.info("Cliente Cloud habilitado e inicializado.");
		}
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resource, ActuatorData data)
	{
		_Logger.info("Analizando datos del actuador entrantes: " + data.getName());

		if (data.isResponseFlagEnabled()) {
			// TODO: manejar lógica de respuesta
		} else {
			if (this.actuatorDataListener != null) {
				this.actuatorDataListener.onActuatorDataUpdate(data);
			}
		}
	}

}
