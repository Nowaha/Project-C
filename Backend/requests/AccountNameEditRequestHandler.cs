using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json;

namespace ChengetaBackend
{
    class AccountNameEditRequestHandler : RequestHandler
    {
        public string Path => "/accounts/edit/name";

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

            AccountNameEditRequest request;
            try
            {
                request = JsonSerializer.Deserialize<AccountNameEditRequest>(bodyRaw);
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
            string firstname = request.firstname.Trim();
            string lastname = request.lastname.Trim();

            using (var db = new ChengetaContext())
            {
                // Checks if the account exists or not
                var dep = db.accounts.Where(user => user.Username.ToLower() == userName.ToLower()).FirstOrDefault();
                if (dep == null)
                {
                    return Response.generateBasicError(
                        Code.BAD_REQUEST,
                        Message.BAD_REQUEST,
                        "Username doesn't exist.");
                }
                dep.FirstName = firstname;
                dep.LastName = lastname;
                db.SaveChanges();

                return new Response(
                    Code.SUCCESS,
                    Message.SUCCESS,
                    Encoding.UTF8.GetBytes(
                        JsonSerializer.Serialize(
                            new
                            {
                                success = true,
                                message = $"Account {userName} with {firstname} and {lastname} edited succesfully"
                            }
                        )
                    )
                );
            }
        }
    }
}