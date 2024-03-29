using System;
using System.Collections.Generic;
using System.Text;
using System.Text.Json;
using System.Linq;

namespace ChengetaBackend
{
    class SessionValidationRequestHandler : RequestHandler
    {
        public string Path => "/accounts/session/validate";
        public Method Method => Method.GET;

        public Response HandleRequest(
            string session,
            Dictionary<string, string> args,
            string bodyRaw
        )
        {
            if (session == "" || !(Program.sessionManager.SessionDictionary.ContainsKey(session)))
            {
                return new Response(
                    Code.SUCCESS,
                    Message.SUCCESS,
                    Encoding.UTF8.GetBytes(
                        JsonSerializer.Serialize(
                            new { success = true, message = "Session is invalid", valid = false }
                        )
                    )
                );
            }

            var sessionUsername = Program.sessionManager.SessionDictionary[session];

            Account.AccountType type = Account.AccountType.RANGER;
            using (var db = new ChengetaContext())
            {
                Account.AccountType? res = db.accounts
                    .Where(x => x.Username == sessionUsername)
                    .Select(x => x.Role)
                    .FirstOrDefault();

                if (res.HasValue)
                {
                    type = res.Value;
                }
            }
            return new Response(
                Code.SUCCESS,
                Message.SUCCESS,
                Encoding.UTF8.GetBytes(
                    JsonSerializer.Serialize(
                        new
                        {
                            success = true,
                            message = "Session is valid",
                            isAdmin = type == Account.AccountType.ADMIN ? true : false,
                            valid = true,
                            username = sessionUsername
                        }
                    )
                )
            );
        }
    }
}
