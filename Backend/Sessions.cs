using System;
using System.Collections.Generic;

namespace ChengetaBackend
{
    public class Sessions
    {

        List<(string username, string password, string salt)> TestAccounts = new();
        Dictionary<Guid, string> SessionDictionary = new();

        public Guid? Authenticate(string username, string password)
        {
            foreach (var account in TestAccounts)
            {
                if (username == account.username)
                {
                    if (Utils.HashPassword(password, account.salt) == account.password)
                    {
                        var session = Guid.NewGuid();
                        SessionDictionary.Add(session, username);
                        return session;
                    }
                }
            }
            return null;
        }


    }
}