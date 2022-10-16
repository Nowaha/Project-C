using System.Collections.Generic;

namespace ChengetaBackend
{
    interface RequestHandler
    {

        public string Path { get; }
        public Method Method { get; }

        public Response HandleRequest(Dictionary<string, string> args);

    }
}