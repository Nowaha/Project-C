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
            string userName = request.username;
            using (var db = new ChengetaContext())
            {
                //Checks if the account already exist or not
                var dep = db.accounts.Where(user => user.Username == userName).FirstOrDefault();
                if (dep == null)
                {
                    return Response.generateBasicError(
                        Code.BAD_REQUEST,
                        Message.BAD_REQUEST,
                        "Username doesn't exist."
                    );
                }
                db.accounts.Remove(dep);
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
