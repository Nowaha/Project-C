using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json;

namespace ChengetaBackend
{
    class AccountDeleteRequestHandler : RequestHandler
    {
        //Path for the server so it knows where to go to call this

        public string Path => "/accounts/delete";

        public Method Method => Method.POST;

        public Response HandleRequest(string session, Dictionary<string, string> args, string bodyRaw)
        {
            if (!Program.sessionManager.SessionDictionary.ContainsKey(session))
            {
                return Response.generateBasicError(Code.UNAUTHORIZED, Message.UNAUTHORIZED, "Server could not find session");
            }

            var logInName = Program.sessionManager.SessionDictionary[session];

            using (var db = new ChengetaContext())
            {
                var loggedInAccount = db.accounts.Where(user => user.Username == logInName).Select(currentUser => currentUser).FirstOrDefault();

                if (loggedInAccount.Username == null)
                {
                    return Response.generateBasicError(Code.SERVER_ERROR, Message.SERVER_ERROR, "Server Error.");
                }

                if (loggedInAccount.Role != Account.AccountType.ADMIN)
                {
                    return Response.generateBasicError(Code.FORBIDDEN, Message.FORBIDDEN, "You do not have the right.");
                }
            };


            AccountDeletionRequest request;

            try
            {
                request = JsonSerializer.Deserialize<AccountDeletionRequest>(bodyRaw);
            }
            catch (Exception)
            {
                return Response.generateBasicError(
                    Code.BAD_REQUEST,
                    Message.BAD_REQUEST,
                    "Invalid request structure."
                );
            }
            string userName = request.username.Trim();
            using (var db = new ChengetaContext())
            {
                // Checks if the account exists
                var acc = db.accounts.Where(user => user.Username.ToLower() == userName.ToLower()).FirstOrDefault();
                if (acc == null)
                {
                    return Response.generateBasicError(
                        Code.BAD_REQUEST,
                        Message.BAD_REQUEST,
                        "Username doesn't exist."
                    );
                }
                db.accounts.Remove(acc);
                db.SaveChanges();

                return new Response(
                    Code.SUCCESS,
                    Message.SUCCESS,
                    Encoding.UTF8.GetBytes(
                        JsonSerializer.Serialize(
                            new
                            {
                                success = true,
                                message = $"Account {userName} deleted succesfully"
                            }
                        )
                    )
                );
            }
        }
    }
}
