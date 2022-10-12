using System.Collections.Generic;

namespace ChengetaBackend
{
    public class SessionManager
    {
        private static int SESSION_LENGTH_IN_BYTES = 64;

        public List<(string username, string password, string salt)> TestAccounts = new();
        Dictionary<string, string> SessionDictionary = new();

        public string Authenticate(string username, string password)
        {
            foreach (var account in TestAccounts)
            {
                if (username == account.username)
                {
                    if (Utils.HashPassword(password, account.salt) == account.password)
                    {
                        var session = Utils.GenerateSecureRandomString(SESSION_LENGTH_IN_BYTES);
                        SessionDictionary.Add(session, username);
                        return session;
                    }
                }
            }
            return null;
        }
    }
}