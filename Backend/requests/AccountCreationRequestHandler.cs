using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json;

namespace ChengetaBackend
{
    class AccountCreationRequestHandler : RequestHandler
    {
        public string Path => "/accounts/create";

        public Method Method => Method.POST;

        public Response HandleRequest(string session, Dictionary<string, string> args, string bodyRaw)
        {
            if (!Program.sessionManager.SessionDictionary.ContainsKey(session))
            {
                return Response.generateBasicError(Code.UNAUTHORIZED, Message.UNAUTHORIZED, "Invalid session");
            }

            var logInName = Program.sessionManager.SessionDictionary[session];

            using (var db = new ChengetaContext())
            {
                var loggedInAccount = db.accounts.Where(user => user.Username == logInName).Select(currentUser => currentUser).FirstOrDefault();

                if (loggedInAccount.Username == null)
                {
                    return Response.generateBasicError(Code.SERVER_ERROR, Message.SERVER_ERROR, "Server could not find session.");
                }

                if (loggedInAccount.Role != Account.AccountType.ADMIN)
                {
                    return Response.generateBasicError(Code.FORBIDDEN, Message.FORBIDDEN, "You do not have the right.");
                }
            };

            AccountCreationRequest request;

            try
            {
                request = JsonSerializer.Deserialize<AccountCreationRequest>(bodyRaw);
            }
            catch (Exception)
            {
                return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, "Invalid request structure.");
            }

            //Checks if the role is correctly filled in

            if (!Enum.IsDefined(typeof(Account.AccountType), request.role))
                return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, "Missing or invalid \"role\" field.");

            Account.AccountType accountType = (Account.AccountType)request.role;

            string userName = request.username.Trim();
            string password = request.password.Trim();
            string firstName = request.firstName.Trim();
            string lastName = request.lastName.Trim();

            // Hashes the entered password and generates a salt for the hashed password

            using (var db = new ChengetaContext())
            {
                //Checks whether the entered username is already in use or not
                if (db.accounts.Where(user => user.Username.ToLower() == userName.ToLower()).FirstOrDefault() != null)
                {
                    return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, "Username is already in use.");
                }

                // Generate new password hash + salt
                var newPassDetails = Utils.HashNewPassword(password);

                // Creates a new account
                Account newAccount = new Account()
                {
                    CreationDate = DateTime.UtcNow,
                    Username = userName,
                    Password = newPassDetails.hashed,
                    Salt = newPassDetails.salt,
                    Role = accountType,
                    FirstName = firstName,
                    LastName = lastName
                };
                db.accounts.Add(newAccount);
                db.SaveChanges();

                // When it gets created succesfully a success message gets sent back
                return new Response(Code.SUCCESS, Message.SUCCESS, Encoding.UTF8.GetBytes(JsonSerializer.Serialize(new
                {
                    success = true,
                    message = $"Account {userName} created succesfully",
                    accountId = db.accounts.Where(user => user.Username == userName && user.Password == newPassDetails.hashed).Select(user => user.Id).FirstOrDefault()
                })));
            }
        }
    }
}