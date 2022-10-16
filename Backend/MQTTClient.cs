using System;
using System.Threading;
using System.Threading.Tasks;
using MQTTnet;
using MQTTnet.Client;
using Newtonsoft.Json;

namespace ChengetaBackend
{

    public class MQTTClient {
        private static string LOG_TAG = "MQTTClient";
        private MqttFactory factory = null;
        private IMqttClient mqttClient = null;
        private static MqttClientOptions options = 
        new MqttClientOptionsBuilder()
            .WithTcpServer("65.108.249.175", 1883)
            .WithCredentials("chengeta2022", "chengetaALTENHR2022")
            .Build();

        public MQTTClient(MqttFactory factory) {
            this.factory = factory;

            this.mqttClient = factory.CreateMqttClient();
        }

        public async Task Connect() {
            Console.ForegroundColor = ConsoleColor.Yellow;
            Program.log(LOG_TAG, "Attempting to connect to the MQTT server...");
            Console.ResetColor();
            MqttClientConnectResult response = await mqttClient.ConnectAsync(options, CancellationToken.None);
            if (response.ResultCode == MqttClientConnectResultCode.Success) {
                Console.ForegroundColor = ConsoleColor.Green;
                Program.log(LOG_TAG, "Successfully connected to the MQTT server.");
                Console.ResetColor();

                mqttClient.ApplicationMessageReceivedAsync += e =>
                {
                    Program.log(LOG_TAG, " ");
                    Program.log(LOG_TAG, "New message:");
                    Program.log(LOG_TAG, e.ApplicationMessage.ConvertPayloadToString());

                    // Convert the details we got from the broker into a JSON object.
                    dynamic json = JsonConvert.DeserializeObject(e.ApplicationMessage.ConvertPayloadToString());

                    using (var context = new ChengetaContext()) {
                        try {
                            context.events.Add(new Event() {
                                NodeId = json.nodeId,
                                Date = Utils.UnixTimeStampToDateTime((long)json.time),
                                Latitude = json.latitude,
                                Longitude = json.longitude,
                                SoundLabel = json.sound_type,
                                Probability = json.probability,
                                SoundURL = json.sound
                            });
                            context.SaveChanges();
                        } catch (Exception ex) {
                            Console.WriteLine(ex);
                        }
                    }

                    return Task.CompletedTask;
                };

                var mqttSubscribeOptions = factory.CreateSubscribeOptionsBuilder()
                    .WithTopicFilter(f => { f.WithTopic("chengeta/notifications"); })
                    .Build();

                await mqttClient.SubscribeAsync(mqttSubscribeOptions, CancellationToken.None);
                Program.log(LOG_TAG, "Subscribed to topic, now waiting for broadcasts...");
                Program.log(LOG_TAG, "Press enter at any time to close the application.");
                Console.ReadLine();
                await Disconnect();
            } else {
                Console.ForegroundColor = ConsoleColor.Red;
                Program.log(LOG_TAG, "Connection failed with code " + response.ResultCode);
                Console.ResetColor();
            }
        }

        public async Task Disconnect() {
            var mqttClientDisconnectOptions = factory.CreateClientDisconnectOptionsBuilder().Build();
            await mqttClient.DisconnectAsync(mqttClientDisconnectOptions, CancellationToken.None);
            Console.ForegroundColor = ConsoleColor.Red;
            Program.log(LOG_TAG, "Disconnected from broker.");
            Console.ResetColor();
        }

    }

}