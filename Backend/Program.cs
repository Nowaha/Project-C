using System.Threading.Tasks;
using MQTTnet;
using System.Linq;

namespace ChengetaBackend
{

    public class Program
    {

        public static MqttFactory mqttFactory = new MqttFactory();

        public static void Main(string[] args)
        {
            runTests();

            // Should probably be removed or replaced in production.
            createTestAdminAccount();

            Run().Wait();
        }

        private static void createTestAdminAccount() {
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
            await client.Connect();
        }

        private static void runTests()
        {
            AuthenticationTest.testHashSaltAndPasswordUniqueness();
            //AuthenticationTest.testSessionCreationOnlyWhenPasswordValid();
        }

    }

}