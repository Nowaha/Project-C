using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json;

namespace ChengetaBackend
{
    class AccountDeleteHandler : RequestHandler
    {
        //Path for the server so it knows where to go to call this

        public string Path => "/accounts/delete";

        public Method Method => Method.POST;

        public Response HandleRequest(string session, Dictionary<string, string> args, string bodyRaw)
        {
            AccountCreationRequest request;

            try
            {
                request = JsonSerializer.Deserialize<AccountCreationRequest>(bodyRaw);
            }
            catch (Exception ex)
            {
                return Response.generateBasicError(
                    Code.BAD_REQUEST,
                    Message.BAD_REQUEST,
                    "Invalid request structure."
                );
            }

            //Checks if the role is correctly filled in

            if (!Enum.IsDefined(typeof(Account.AccountType), request.role))
                return Response.generateBasicError(
                    Code.BAD_REQUEST,
                    Message.BAD_REQUEST,
                    "Missing or invalid \"role\" field."
                );


            string userName = request.username;
            string password = request.password;


            using (var db = new ChengetaContext())
            {
                //Checks if the account already exist or not

                if (db.accounts.Where(user => user.Username == userName).FirstOrDefault() == null)
                {
                    return Response.generateBasicError(
                        Code.BAD_REQUEST,
                        Message.BAD_REQUEST,
                        "Username is doesn't exist."
                    );
                }
                var dep = db.accounts.Where(d => d.Username == userName).First();
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
                                message = $"Account {userName} deleted succesfully",
                                accountId = db.accounts
                                    .Where(
                                        user =>
                                            user.Username == userName
                                    )
                                    .Select(user => user.Id)
                                    .FirstOrDefault()
                            }
                        )
                    )
                );
            }
        }
    }
}
