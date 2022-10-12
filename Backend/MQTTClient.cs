using System;
using System.Threading;
using System.Threading.Tasks;
using MQTTnet;
using MQTTnet.Client;

namespace ChengetaBackend
{

    public class MQTTClient {
        
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
            Console.WriteLine("Attempting to connect to the MQTT server...");
            Console.ResetColor();
            MqttClientConnectResult response = await mqttClient.ConnectAsync(options, CancellationToken.None);
            if (response.ResultCode == MqttClientConnectResultCode.Success) {
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine("Successfully connected to the MQTT server.");
                Console.ResetColor();

                mqttClient.ApplicationMessageReceivedAsync += e =>
                {
                    string now = DateTime.Now.ToString("HH:mm:ss");
                    Console.WriteLine("\n[" + now + "] New message:");
                    Console.WriteLine("[" + now + "] " + e.ApplicationMessage.ConvertPayloadToString());
                    return Task.CompletedTask;
                };

                var mqttSubscribeOptions = factory.CreateSubscribeOptionsBuilder()
                    .WithTopicFilter(f => { f.WithTopic("chengeta/notifications"); })
                    .Build();

                await mqttClient.SubscribeAsync(mqttSubscribeOptions, CancellationToken.None);
                Console.WriteLine("\nSubscribed to topic, now waiting for broadcasts...");
                Console.WriteLine("Press enter at any time to close the application.\n");
                Console.ReadLine();
                await Disconnect();
            } else {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("Connection failed with code " + response.ResultCode);
                Console.ResetColor();
            }
        }

        public async Task Disconnect() {
            var mqttClientDisconnectOptions = factory.CreateClientDisconnectOptionsBuilder().Build();
            await mqttClient.DisconnectAsync(mqttClientDisconnectOptions, CancellationToken.None);
            Console.ForegroundColor = ConsoleColor.Red;
            Console.WriteLine("Disconnected from broker.");
            Console.ResetColor();
        }

    }

}