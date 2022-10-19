using System.Threading.Tasks;
using MQTTnet;
using System.Linq;
using System;

namespace ChengetaBackend
{

    public class Program
    {

        public static SessionManager sessionManager = new SessionManager();
        public static MqttFactory mqttFactory = new MqttFactory();

        public static void Main(string[] args)
        {
            createTestAdminAccount();
            runTests();
            Run().Wait();
        }

        private static void createTestAdminAccount()
        {
            using (var db = new ChengetaContext())
            {
                if (db.accounts.Where(a => a.Username == "admin").FirstOrDefault() == null)
                {
                    var newPassDetails = Utils.HashNewPassword("Pass123");
                    var adminPassHash = newPassDetails.hashed;
                    var adminSalt = newPassDetails.salt;

                    db.accounts.Add(new Account()
                    {
                        CreationDate = System.DateTime.UtcNow,
                        Username = "admin",
                        Password = adminPassHash,
                        Salt = adminSalt,
                        Role = Account.AccountType.ADMIN
                    });

                    db.SaveChanges();
                }
            }
        }

        public static async Task Run()
        {
            ChengetaBackend.MQTTClient client = new MQTTClient(mqttFactory);
            Parallel.Invoke(() => Server.Run(), () => client.Connect());
        }

        private static void runTests()
        {
            AuthenticationTest.testHashSaltAndPasswordUniqueness();
            //AuthenticationTest.testSessionCreationOnlyWhenPasswordValid();
        }

        public static void log(string message)
        {
            string now = DateTime.Now.ToString("HH:mm:ss");
            Console.WriteLine("[" + now + "] " + message);
        }

        public static void log(string tag, string message)
        {
            string now = DateTime.Now.ToString("HH:mm:ss");
            log("[" + tag + "] " + message);
        }

    }

}