package programmingtheiot.gda.connection;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

public class CloudClientConnector implements ICloudClient {
	// static
	private static final Logger _Logger = Logger.getLogger(CloudClientConnector.class.getName());

	// private vars
	private String topicPrefix = "";
	private MqttClientConnector mqttClient = null;
	private IDataMessageListener dataMsgListener = null;
	private int qosLevel = 1; // puedes cambiar a 0 si se requiere

	// constructor
	public CloudClientConnector() {
		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.topicPrefix = configUtil.getProperty(
			ConfigConst.CLOUD_GATEWAY_SERVICE,
			ConfigConst.BASE_TOPIC_KEY
		);

		if (this.topicPrefix == null) {
			this.topicPrefix = "/";
		} else if (!this.topicPrefix.endsWith("/")) {
			this.topicPrefix += "/";
		}
	}

	@Override
	public boolean connectClient() {
		if (this.mqttClient == null) {
			this.mqttClient = new MqttClientConnector(ConfigConst.CLOUD_GATEWAY_SERVICE);
		}
		return this.mqttClient.connectClient();
	}

	@Override
	public boolean disconnectClient() {
		if (this.mqttClient != null && this.mqttClient.isConnected()) {
			return this.mqttClient.disconnectClient();
		}
		return false;
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener) {
		if (listener != null) {
			this.dataMsgListener = listener;
			return true;
		}
		return false;
	}

	@Override
	public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SensorData data) {
		if (resource != null && data != null) {
			String payload = DataUtil.getInstance().sensorDataToJson(data);
			return publishMessageToCloud(resource, data.getName(), payload);
		}
		return false;
	}

	@Override
	public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SystemPerformanceData data) {
		if (resource != null && data != null) {
			SensorData cpuData = new SensorData();
			cpuData.updateData(data);
			cpuData.setName(ConfigConst.CPU_UTIL_NAME);
			cpuData.setValue(data.getCpuUtilization());

			boolean cpuSent = sendEdgeDataToCloud(resource, cpuData);
			if (!cpuSent) {
				_Logger.warning("Fallo al enviar datos de CPU al servicio en la nube.");
			}

			SensorData memData = new SensorData();
			memData.updateData(data);
			memData.setName(ConfigConst.MEM_UTIL_NAME);
			memData.setValue(data.getMemoryUtilization());

			boolean memSent = sendEdgeDataToCloud(resource, memData);
			if (!memSent) {
				_Logger.warning("Fallo al enviar datos de memoria al servicio en la nube.");
			}

			return (cpuSent && memSent);
		}
		return false;
	}

	@Override
	public boolean subscribeToCloudEvents(ResourceNameEnum resource) {
		String topicName = null;

		if (this.mqttClient != null && this.mqttClient.isConnected()) {
			topicName = createTopicName(resource);
			this.mqttClient.subscribeToTopic(topicName, this.qosLevel);
			return true;
		} else {
			_Logger.warning("No hay conexión MQTT al broker. Suscripción ignorada. Tema: " + topicName);
		}
		return false;
	}

	@Override
	public boolean unsubscribeFromCloudEvents(ResourceNameEnum resource) {
		String topicName = null;

		if (this.mqttClient != null && this.mqttClient.isConnected()) {
			topicName = createTopicName(resource);
			this.mqttClient.unsubscribeFromTopic(topicName);
			return true;
		} else {
			_Logger.warning("No hay conexión MQTT al broker. Desuscripción ignorada. Tema: " + topicName);
		}
		return false;
	}

	// métodos privados

	private String createTopicName(ResourceNameEnum resource) {
		return createTopicName(resource.getDeviceName(), resource.getResourceType());
	}

	private String createTopicName(String deviceName, String resourceTypeName) {
		return this.topicPrefix + deviceName + "/" + resourceTypeName;
	}

	private boolean publishMessageToCloud(ResourceNameEnum resource, String itemName, String payload) {
		String topicName = createTopicName(resource) + "-" + itemName;
		return publishMessageToCloud(topicName, payload);
	}

	private boolean publishMessageToCloud(String topicName, String payload) {
		try {
			_Logger.finest("Publicando datos al CSP: " + topicName);
			this.mqttClient.publishMessage(topicName, payload.getBytes(), this.qosLevel);
			return true;
		} catch (Exception e) {
			_Logger.log(Level.WARNING, "No se pudo publicar en el tema: " + topicName, e);
		}
		return false;
	}
}
