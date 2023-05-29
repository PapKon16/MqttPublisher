package gr.upatras.mqtt.controller;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class controller implements MqttCallback {
    public void connectionLost(Throwable t) {
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
    }
    @PostMapping("/")
    @ResponseBody
    public ResponseEntity receivePostRequest(@RequestBody String message) {
        final String BROKER_URL = "tcp://test.mosquitto.org:1883";
        final Logger log = LoggerFactory.getLogger(controller.class);
        final String TOPIC = "grupatras/lab/message";

        MqttConnectOptions connOpt = new MqttConnectOptions();
        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(30);

        try (MqttClient myClient = new MqttClient(BROKER_URL, MqttClient.generateClientId())) {
            myClient.setCallback(this);
            myClient.connect(connOpt);

            log.info("Connected to " + BROKER_URL);
            MqttTopic topic = myClient.getTopic(TOPIC);

            publishMessage(topic, message, log);

            disconnect(myClient);
        } catch (MqttException e) {
            log.error("An error occurred while connecting to the MQTT broker", e);
        }
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    private void publishMessage(MqttTopic topic, String message, Logger log) {
        MqttMessage msg = new MqttMessage(message.getBytes());
        msg.setRetained(false);

        log.info("Publishing to topic \"" + topic + "\" value " + message);
        try {
            MqttDeliveryToken token = topic.publish(msg);
            token.waitForCompletion();
        } catch (MqttException e) {
            log.error("An error occurred while publishing the message", e);
        }
    }

    private void disconnect(MqttClient myClient) {
        try {
            if (myClient.isConnected()) {
                myClient.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}