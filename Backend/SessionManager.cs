using System;
using System.Collections.Generic;
using System.Linq;

namespace ChengetaBackend
{
    public class SessionManager
    {
        private static int SESSION_LENGTH_IN_BYTES = 64;

        // public List<(string username, string password, string salt)> TestAccounts = new();
        public Dictionary<string, string> SessionDictionary = new();

        public AuthResult Authenticate(string username, string password)
        {
            try
            {
                using (var db = new ChengetaContext())
                {
                    var account = db.accounts.Where(acc => acc.Username.ToLower() == username.ToLower()).FirstOrDefault();
                    if (account == null) return new AuthResult(ResultCode.INVALID_CREDENTIALS);
                    if (Utils.HashPassword(password, account.Salt) != account.Password) return new AuthResult(ResultCode.INVALID_CREDENTIALS);

                    // Username & password are correct, create new session
                    var session = Utils.GenerateSecureRandomString(SESSION_LENGTH_IN_BYTES);
                    SessionDictionary.Add(session, username);
                    return new AuthResult(ResultCode.SUCCESS, session);
                }
            } catch (Exception ex) {
                System.Console.WriteLine(ex.ToString());
                return new AuthResult(ResultCode.ERROR);
            }
        }

        public class AuthResult
        {
            public ResultCode resultCode { get; private set; }
            public string sessionKey { get; private set; } = null;

            public AuthResult(ResultCode resultCode)
            {
                this.resultCode = resultCode;
            }

            public AuthResult(ResultCode code, string sessionKey)
            {
                this.resultCode = code;
                this.sessionKey = sessionKey;
            }

        }

        public enum ResultCode
        {
            INVALID_CREDENTIALS,
            ERROR,
            SUCCESS
        }
    }
}