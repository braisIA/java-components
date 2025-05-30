package programmingtheiot.gda.connection;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.common.SimpleCertManagementUtil;

public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended {

	private static final Logger _Logger = Logger.getLogger(MqttClientConnector.class.getName());

	private MqttClient mqttClient;
	private MqttConnectOptions connOpts;

	private String brokerAddr = null;
	private String brokerUrl = "tcp://localhost:1883";
	private String clientId = "GDA_Mqtt_Client";

	private String pemFileName = null;
	private boolean enableEncryption = false;
	private boolean useCleanSession = false;
	private boolean enableAutoReconnect = true;
	private boolean isConnected = false;
	private boolean useCloudGatewayConfig = false;

	private IConnectionListener connListener = null;
	private IDataMessageListener dataMsgListener = null;

	public MqttClientConnector() {
		this(false);
	}

	public MqttClientConnector(boolean useCloudGatewayConfig) {
		this(useCloudGatewayConfig ? ConfigConst.CLOUD_GATEWAY_SERVICE : null);
	}

	public MqttClientConnector(String configSectionName) {
		super();

		if (configSectionName != null && !configSectionName.isEmpty()) {
			this.useCloudGatewayConfig = true;
			initClientParameters(configSectionName);
		} else {
			this.useCloudGatewayConfig = false;
			initClientParameters(ConfigConst.MQTT_GATEWAY_SERVICE);
		}
	}

	private void initClientParameters(String configSectionName) {
		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.brokerUrl = configUtil.getProperty(configSectionName, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);
		int port = configUtil.getInteger(configSectionName, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_MQTT_PORT);
		this.enableEncryption = configUtil.getBoolean(configSectionName, ConfigConst.ENABLE_CRYPT_KEY);
		this.pemFileName = configUtil.getProperty(configSectionName, ConfigConst.CERT_FILE_KEY);
		this.clientId = configUtil.getProperty(ConfigConst.GATEWAY_DEVICE, ConfigConst.DEVICE_LOCATION_ID_KEY,
				MqttClient.generateClientId());

		try {
			this.connOpts = new MqttConnectOptions();
			this.connOpts.setCleanSession(this.useCleanSession);
			this.connOpts.setAutomaticReconnect(this.enableAutoReconnect);

			if (this.enableEncryption) {
				initSecureConnectionParameters(configSectionName);
				this.brokerUrl = "ssl://" + this.brokerUrl + ":" + port;
			} else {
				this.brokerUrl = "tcp://" + this.brokerUrl + ":" + port;
			}

			if (configUtil.hasProperty(configSectionName, ConfigConst.CRED_FILE_KEY)) {
				initCredentialConnectionParameters(configSectionName);
			}

			this.mqttClient = new MqttClient(this.brokerUrl, this.clientId, new MemoryPersistence());
			this.mqttClient.setCallback(this);

			_Logger.info("Broker URL configurado: " + this.brokerUrl);

		} catch (MqttException e) {
			_Logger.log(Level.SEVERE, "Error al inicializar el cliente MQTT.", e);
		}
	}

	private void initCredentialConnectionParameters(String configSectionName) {
		ConfigUtil configUtil = ConfigUtil.getInstance();

		try {
			Properties props = configUtil.getCredentials(configSectionName);

			if (props != null) {
				this.connOpts.setUserName(props.getProperty(ConfigConst.USER_NAME_TOKEN_KEY, ""));
				this.connOpts.setPassword(props.getProperty(ConfigConst.USER_AUTH_TOKEN_KEY, "").toCharArray());

				_Logger.info("Credenciales cargadas correctamente.");
			} else {
				_Logger.warning("Archivo de credenciales vacío o no encontrado.");
			}
		} catch (Exception e) {
			_Logger.log(Level.WARNING, "No se pudieron cargar las credenciales.", e);
		}
	}

	private void initSecureConnectionParameters(String configSectionName) {
		try {
			if (this.pemFileName != null) {
				File certFile = new File(this.pemFileName);

				if (certFile.exists()) {
					SSLSocketFactory sslFactory = SimpleCertManagementUtil.getInstance()
							.loadCertificate(this.pemFileName);
					this.connOpts.setSocketFactory(sslFactory);
					_Logger.info("Archivo PEM válido. TLS habilitado.");
				} else {
					this.enableEncryption = false;
					_Logger.warning("Archivo PEM no válido. Se usará conexión insegura.");
				}
			}
		} catch (Exception e) {
			this.enableEncryption = false;
			_Logger.log(Level.SEVERE, "No se pudo configurar TLS. Usando conexión insegura.", e);
		}
	}

	@Override
	public boolean connectClient() {
		try {
			if (!this.mqttClient.isConnected()) {
				this.mqttClient.connect(this.connOpts);
				this.isConnected = true;
				return true;
			}
		} catch (MqttException e) {
			_Logger.log(Level.SEVERE, "Error al conectar con el broker.", e);
		}

		return false;
	}

	@Override
	public boolean disconnectClient() {
		try {
			if (this.mqttClient.isConnected()) {
				this.mqttClient.disconnect();
				this.isConnected = false;
				return true;
			}
		} catch (MqttException e) {
			_Logger.log(Level.SEVERE, "Error al desconectar del broker.", e);
		}

		return false;
	}

	@Override
	public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos) {
		if (topicName == null || msg == null || msg.length() == 0)
			return false;
		return publishMessage(topicName.getResourceName(), msg.getBytes(), qos);
	}

	@Override
	public boolean subscribeToTopic(ResourceNameEnum topicName, int qos) {
		if (topicName == null)
			return false;
		return subscribeToTopic(topicName.getResourceName(), qos);
	}

	@Override
	public boolean unsubscribeFromTopic(ResourceNameEnum topicName) {
		if (topicName == null)
			return false;
		return unsubscribeFromTopic(topicName.getResourceName());
	}

	@Override
	public boolean setConnectionListener(IConnectionListener listener) {
		if (listener != null) {
			this.connListener = listener;
			return true;
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
	public void connectComplete(boolean reconnect, String serverURI) {
		_Logger.info("Conectado al broker MQTT: " + serverURI + ", reconexión: " + reconnect);

		if (this.connListener != null) {
			this.connListener.onConnect();
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		_Logger.warning("Conexión perdida con el broker MQTT: " + cause.getMessage());
		this.isConnected = false;

		if (this.connListener != null) {
			this.connListener.onDisconnect();
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// opcional
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		if (this.dataMsgListener != null) {
			this.dataMsgListener.handleIncomingMessage(topic);
		}
	}

	protected boolean publishMessage(String topicName, byte[] payload, int qos) {
		try {
			MqttMessage mqttMsg = new MqttMessage();
			mqttMsg.setQos(qos);
			mqttMsg.setPayload(payload);
			this.mqttClient.publish(topicName, mqttMsg);
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "No se pudo publicar en el tópico: " + topicName, e);
		}
		return false;
	}

	protected boolean subscribeToTopic(String topicName, int qos) {
		return subscribeToTopic(topicName, qos, null);
	}

	protected boolean subscribeToTopic(String topicName, int qos, IMqttMessageListener listener) {
		try {
			if (listener != null) {
				this.mqttClient.subscribe(topicName, qos, listener);
			} else {
				this.mqttClient.subscribe(topicName, qos);
			}
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "No se pudo suscribir al tópico: " + topicName, e);
		}
		return false;
	}

	protected boolean unsubscribeFromTopic(String topicName) {
		try {
			this.mqttClient.unsubscribe(topicName);
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "No se pudo cancelar la suscripción al tópico: " + topicName, e);
		}
		return false;
	}

	public boolean isConnected() {
		return this.isConnected;
	}

 
}
