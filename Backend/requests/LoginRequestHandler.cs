using System.Collections.Generic;

namespace ChengetaBackend
{
    class LoginRequestHandler : RequestHandler
    {
        public string Path => "/user/login";
        public Method Method => Method.GET;

        public Response HandleRequest(Dictionary<string, string> args)
        {
            if ((!args.ContainsKey("username") || args["username"] == null)) return DefaultResponses.BAD_REQUEST;
            if ((!args.ContainsKey("password") || args["password"] == null)) return DefaultResponses.BAD_REQUEST;

            string username = args["username"];
            string password = args["password"];

            Program.sessionManager.Authenticate(username, password);

            return new(200, "OK", new byte[0]);
        }
    }
}