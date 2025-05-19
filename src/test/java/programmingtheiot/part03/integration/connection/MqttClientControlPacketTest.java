/**
 * 
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * Copyright (c) 2020 by Andrew D. King
 */ 

package programmingtheiot.part03.integration.connection;

import java.util.logging.Logger;

import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.MqttClientConnector;

/**
 * This test case class contains very basic integration tests for
 * MqttClientControlPacketTest. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 *
 */
public class MqttClientControlPacketTest
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(MqttClientControlPacketTest.class.getName());
	
	
	// member var's
	
	private MqttClientConnector mqttClient = null;
	
	
	// test setup methods
	
	@Before
	public void setUp() throws Exception
	{
		this.mqttClient = new MqttClientConnector();
	}
	
	@After
	public void tearDown() throws Exception
	{
    	if (this.mqttClient != null) {
        this.mqttClient.disconnectClient();
    	}
	}

	
	
	// test methods
	
	@Test
	public void testConnectAndDisconnect()
	{
		_Logger.info("Ejecutando testConnectAndDisconnect...");

		boolean isConnected = this.mqttClient.connectClient();

		assertTrue("MQTT client debería estar conectado.", isConnected);

		try {
			Thread.sleep(2000); // Espera para permitir que se complete el handshake
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		boolean isDisconnected = this.mqttClient.disconnectClient();
		assertTrue("MQTT client debería desconectarse correctamente.", isDisconnected);
	}
	
	@Test
	public void testServerPing()
	{
		_Logger.info("Ejecutando testServerPing...");

		boolean isConnected = this.mqttClient.connectClient();
		assertTrue("MQTT client debería estar conectado.", isConnected);

		try {
			_Logger.info("Esperando 10 segundos para observar PINGREQ / PINGRESP...");
			Thread.sleep(10000); // Espera suficiente para ver intercambio PING
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		boolean isDisconnected = this.mqttClient.disconnectClient();
		assertTrue("MQTT client debería desconectarse correctamente.", isDisconnected);
	}
	
	
	@Test
	public void testPubSub()
	{
		_Logger.info("Ejecutando testPubSub...");

		ResourceNameEnum testTopic = ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE; // Ejemplo de tópico válido
		String testPayload = "Mensaje de prueba";

		boolean isConnected = this.mqttClient.connectClient();
		assertTrue("Debe conectarse al broker.", isConnected);

		// Suscribirse al tópico
		boolean subResult = this.mqttClient.subscribeToTopic(testTopic, 1); // QoS 1
		assertTrue("Debe suscribirse correctamente.", subResult);

		// Publicar un mensaje con QoS 1 (PUBLISH / PUBACK)
		boolean pubResultQos1 = this.mqttClient.publishMessage(testTopic, testPayload, 1);
		assertTrue("Debe publicar con QoS 1.", pubResultQos1);

		// Publicar un mensaje con QoS 2 (PUBLISH / PUBREC / PUBREL / PUBCOMP)
		boolean pubResultQos2 = this.mqttClient.publishMessage(testTopic, testPayload, 2);
		assertTrue("Debe publicar con QoS 2.", pubResultQos2);

		// Cancelar suscripción
		boolean unsubResult = this.mqttClient.unsubscribeFromTopic(testTopic);
		assertTrue("Debe cancelar la suscripción correctamente.", unsubResult);

		try {
			Thread.sleep(2000); // Tiempo para permitir procesamiento
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		boolean isDisconnected = this.mqttClient.disconnectClient();
		assertTrue("Debe desconectarse correctamente.", isDisconnected);
	}

	
}
