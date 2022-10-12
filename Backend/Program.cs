using System.Threading.Tasks;
using MQTTnet;

namespace ChengetaBackend {

    public class Program {

        public static MqttFactory mqttFactory = new MqttFactory();

        public static void Main(string[] args) {
            runTests();
            
            Run().Wait();
        }

        public static async Task Run() {
            ChengetaBackend.MQTTClient client = new MQTTClient(mqttFactory);
            await client.Connect();
        }

        private static void runTests() {
            AuthenticationTest.testHashSaltAndPasswordUniqueness();
            AuthenticationTest.testSessionCreationOnlyWhenPasswordValid();
        }

    }

}