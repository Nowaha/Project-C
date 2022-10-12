using System.Diagnostics;

namespace ChengetaBackend {
    public class AuthenticationTest {
        public static void testSessionCreationOnlyWhenPasswordValid() {
            SessionManager a = new SessionManager();

            string username = "account1";
            string password = "test123";
            string closePassword = "Test123";
            string wrongPassword = "wow!";
            string salt = "abc";
            string hashed = Utils.HashPassword(password, salt);

            a.TestAccounts.Add((username, hashed, salt));

            Debug.Assert(a.Authenticate(username, null) == null);
            Debug.Assert(a.Authenticate(username, "") == null);
            Debug.Assert(a.Authenticate(username, salt) == null);
            Debug.Assert(a.Authenticate(username, closePassword) == null);
            Debug.Assert(a.Authenticate(username, wrongPassword) == null);
            Debug.Assert(a.Authenticate(username, password) != null);
        }

        public static void testHashSaltAndPasswordUniqueness() {
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
        }
    }
}