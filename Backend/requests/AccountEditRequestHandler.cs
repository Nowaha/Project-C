using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json;

namespace ChengetaBackend
{
    class AccountEditRequestHandler : RequestHandler
    {
        //Path for the server so it knows where to go to call this

        public string Path => "/accounts/edit";

        public Method Method => Method.POST;

        public Response HandleRequest(string session, Dictionary<string, string> args, string bodyRaw)
        {
            AccountEditRequest request;

            try
            {
                request = JsonSerializer.Deserialize<AccountEditRequest>(bodyRaw);
            }
            catch (Exception)
            {
                return Response.generateBasicError(
                    Code.BAD_REQUEST,
                    Message.BAD_REQUEST,
                    "Invalid request structure."
                );
            }
            string userName = request.username;
            string password = request.password;

            using (var db = new ChengetaContext())
            {
                var newPassDetails = Utils.HashNewPassword(password);
                var userPassHash = newPassDetails.hashed;
                var userSalt = newPassDetails.salt;

                //Checks if the account already exist or not
                var dep = db.accounts.Where(user => user.Username == userName).FirstOrDefault();
                if (dep == null)
                {
                    return Response.generateBasicError(
                        Code.BAD_REQUEST,
                        Message.BAD_REQUEST,
                        "Username doesn't exist.");
                }
                dep.Password = userPassHash;
                dep.Salt = userSalt;
                db.SaveChanges();

                return new Response(
                    Code.SUCCESS,
                    Message.SUCCESS,
                    Encoding.UTF8.GetBytes(
                        JsonSerializer.Serialize(
                            new
                            {
                                success = true,
                                message = $"Account {userName} edited succesfully"
                            }
                        )
                    )
                );
            }
        }
    }
}