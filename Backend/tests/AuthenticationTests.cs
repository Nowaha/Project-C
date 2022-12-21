using System;
using System.Diagnostics;
using static ChengetaBackend.SessionManager;

namespace ChengetaBackend {
    public class AuthenticationTest {
        public static void testSessionCreationOnlyWhenPasswordValid() {
            Console.Write("Testing login security... ");
            SessionManager a = new SessionManager(forTesting: true);

            string username = "account1";
            string password = "test123";
            string closePassword = "Test123";
            string wrongPassword = "wow!";
            string salt = "abc";
            string hashed = Utils.HashPassword(password, salt);

            a.TestAccounts.Add(new Account {Password = hashed, Salt = salt, Username = username});

            Debug.Assert(a.Authenticate(username, null).resultCode == ResultCode.INVALID_CREDENTIALS);
            Debug.Assert(a.Authenticate(username, "").resultCode == ResultCode.INVALID_CREDENTIALS);
            Debug.Assert(a.Authenticate(username, salt).resultCode == ResultCode.INVALID_CREDENTIALS);
            Debug.Assert(a.Authenticate(username, closePassword).resultCode == ResultCode.INVALID_CREDENTIALS);
            Debug.Assert(a.Authenticate(username, wrongPassword).resultCode == ResultCode.INVALID_CREDENTIALS);
            Debug.Assert(a.Authenticate(username, password).resultCode == ResultCode.SUCCESS);

            Console.ForegroundColor = ConsoleColor.Green;
            Console.WriteLine(" Passed!");
            Console.ResetColor();
        }

        public static void testHashSaltAndPasswordUniqueness() {
            Console.Write("Testing password hashing uniqueness...");
            SessionManager a = new SessionManager();

            string password1 = "test123";
            string password2 = "Test123";
            string salt1 = "abc";
            string salt2 = "abcd";

            string hashed1_1 = Utils.HashPassword(password1, salt1);
            string hashed1_2 = Utils.HashPassword(password1, salt2);

            string hashed2_1 = Utils.HashPassword(password2, salt1);
            string hashed2_2 = Utils.HashPassword(password2, salt2);

            Debug.Assert(hashed1_1 != hashed1_2);
            Debug.Assert(hashed1_1 != hashed2_1);
            Debug.Assert(hashed1_1 != hashed2_2);
            Debug.Assert(hashed2_1 != hashed1_2);
            Debug.Assert(hashed2_1 != hashed2_2);

            Console.ForegroundColor = ConsoleColor.Green;
            Console.WriteLine(" Passed!");
            Console.ResetColor();
        }
    }
}