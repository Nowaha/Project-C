using System;
using System.Collections.Generic;
using System.Text;
using System.Text.Json;

namespace ChengetaBackend
{
    class LoginRequestHandler : RequestHandler
    {
        public string Path => "/user/login";
        public Method Method => Method.GET;

        public Response HandleRequest(string session, Dictionary<string, string> args)
        {
            if ((!args.ContainsKey("username") || args["username"] == null)) return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, "Missing \"username\" field.");
            if ((!args.ContainsKey("password") || args["password"] == null)) return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, "Missing \"password\" field.");
            if (Program.sessionManager.SessionDictionary.ContainsKey(session)) return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, "Error: ALREADY_LOGGED_IN");

            string username = args["username"];
            string password = args["password"];

            SessionManager.AuthResult authResult = Program.sessionManager.Authenticate(username, password);
            if (authResult.resultCode == SessionManager.ResultCode.SUCCESS) {
                return new Response(Code.SUCCESS, Message.SUCCESS, Encoding.UTF8.GetBytes(JsonSerializer.Serialize(new {
                    success = true,
                    message = "Logged in successfully",
                    sessionKey = authResult.sessionKey
                })));
            } else {
                return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, "Error: " + Enum.GetName(authResult.resultCode));
            }
        }
    }
}