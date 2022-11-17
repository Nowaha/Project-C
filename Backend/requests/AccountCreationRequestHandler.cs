using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json;

namespace ChengetaBackend
{
    class AccountCreationRequestHandler : RequestHandler
    {

        //Path for the server so it knows where to go to call this

        public string Path => "/accounts/create";

        public Method Method => Method.POST;

        public Response HandleRequest(string session, Dictionary<string, string> args)
        {
            //Checks if the Username and password field are filled in correctly

            if (!Program.sessionManager.SessionDictionary.ContainsKey(session)) return Response.generateBasicError(Code.UNAUTHORIZED, Message.UNAUTHORIZED, "Invalid session");
            if ((!args.ContainsKey("username") || args["username"] == null)) return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, "Missing \"username\" field.");
            if ((!args.ContainsKey("password") || args["password"] == null)) return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, "Missing \"password\" field.");

            Account.AccountType accountType;

            //Checks if the role is correctly filled in

            try
            {
                accountType = (Account.AccountType)int.Parse(args["role"]);
            }
            catch (Exception ex)
            {
                return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, "Missing or invalid role.");
            }
            string userName = args["username"];
            string password = args["password"];


            //Hashes the entered password and generates a salt for the hashed password

            using(var db = new ChengetaContext())
            {
                var newPassDetails = Utils.HashNewPassword(password);
                var userPassHash = newPassDetails.hashed;
                var userSalt = newPassDetails.salt;

                //Creates a new account

                Account newAccount = new Account()
                {
                    CreationDate = DateTime.UtcNow,
                    Username = userName,
                    Password = userPassHash,
                    Salt = userSalt,
                    Role = accountType
                };

                //Checks whether the entered username is already in use or not

                if (db.accounts.Where(user => user.Username == userName).FirstOrDefault() != null)
                {
                    return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, "Username is already in use.");
                }
                db.accounts.Add(newAccount);
                db.SaveChanges();

                //When it gets created succesfully a success message gets sent back

                return new Response(Code.SUCCESS, Message.SUCCESS, Encoding.UTF8.GetBytes(JsonSerializer.Serialize(new
                {
                    success = true,
                    message = $"Account {userName} created succesfully",
                    accountId = db.accounts.Where(user => user.Username == userName && user.Password == password).Select(user => user.Id)
                })));
            }
            //Dit moeilijk maar ik doe mn best
         


        }
    }




}