using System.Net;
using System.Collections.Generic;
using System.Net.Sockets;
using System.Text;
using System;
using System.Web;
using System.Threading.Tasks;

namespace ChengetaBackend
{
    class Server
    {
        private static string LOG_TAG = "HTTP";

        private static int DATA_READ_TIMEOUT = 100;
        private static Dictionary<string, RequestHandler> requestHandlers = new Dictionary<string, RequestHandler>();


        // Any new handlers for paths should be added here
        private static void setupHandlers()
        {
            requestHandlers.Clear();
            requestHandlers.Add("/user/login", new LoginRequestHandler());
            requestHandlers.Add("/events/latest", new LatestEventsRequestHandler());
            requestHandlers.Add("/accounts/create", new AccountCreationRequestHandler());
            requestHandlers.Add("/accounts/delete", new AccountDeleteRequestHandler());
            requestHandlers.Add("/accounts/edit", new AccountEditRequestHandler());
            requestHandlers.Add("/accounts/view", new AccountSearchRequestHandler());
        }
        public static async Task Run()
        {
            Program.log(LOG_TAG, "Server is starting...");

            // Register all of the handlers
            setupHandlers();

            await Start();
        }

        public static bool run = true;

        private static Socket listener;

        public static void Stop()
        {
            run = false;
            var w = new byte[0];

            Console.ForegroundColor = ConsoleColor.Red;
            Program.log(LOG_TAG, "Shutting down socket...");
            Console.ResetColor();

            listener.Close();

            Console.ForegroundColor = ConsoleColor.Red;
            Program.log(LOG_TAG, "Socket shut down.");
            Console.ResetColor();
        }

        private static async Task Start()
        {
            IPAddress ipAddress = IPAddress.Parse(Environment.HTTP_IP);
            IPEndPoint localEndPoint = new IPEndPoint(ipAddress, Environment.HTTP_PORT);

            listener = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
            listener.Bind(localEndPoint);
            listener.Listen(100);

            Program.log(LOG_TAG, $"Restful API started ({ipAddress}:{Environment.HTTP_PORT}).");
            Program.log(LOG_TAG, "Now waiting for requests...");

            while (run)
            {
                Socket currentConnection = listener.Accept();
                currentConnection.Blocking = true;
                currentConnection.ReceiveTimeout = 2000;

                Program.log(LOG_TAG, "New request, reading...");

                string data;

                // A loop to read any and all incoming data until there is no more left to read (.Available)
                try
                {
                    var buffer = new byte[5012];
                    int receivedData = currentConnection.Receive(buffer);
                    data = Encoding.ASCII.GetString(buffer, 0, receivedData);
                } catch (Exception ex) {
                    currentConnection.Disconnect(false);
                    currentConnection.Close();
                    continue;
                }

System.Console.WriteLine(data);

                // If there is no data, we can not do anything with it, so we close it here.
                if (data.Length == 0 || data.Trim().Length == 0)
                {
                    currentConnection.Send(Encoding.ASCII.GetBytes("empty request"));
                    currentConnection.Disconnect(false);
                    currentConnection.Close();
                    continue;
                }

                // The first line should be the HTTP info header (format is <Method> <URL> <version>)
                string httpLine = data.Split("\n")[0];
                string[] httpRequestData = httpLine.Split(" ");

                // If there is 3 values (see format above), then it's most likely a valid HTTP header.
                if (httpRequestData.Length == 3)
                {
                    // 0:<Method> 1:<URL> 2:<version>
                    string requestMethod = httpRequestData[0];
                    string requestUrlFull = httpRequestData[1];
                    string requestVersion = httpRequestData[2].Replace("\r", "");

                    // We only support HTTP/1.1 requests - we are now certain it is actually a HTTP request.
                    if (requestVersion == "HTTP/1.1")
                    {
                        Program.log(LOG_TAG, httpLine);
                        Dictionary<string, string> argsFinal = new();

                        // Split the URL at the "?", if there is one.
                        string[] requestUrlArgsSplit = requestUrlFull.Split("?");
                        // Even if there is no "?", this will still contain the full URl then.
                        string requestUrlOnly = requestUrlArgsSplit[0];

                        // If the split == 2, then there's valid values at the right of the "?" - read arguments
                        // Example: login?username=User&password=Pass
                        // Split to login and username=...
                        if (requestUrlArgsSplit.Length == 2)
                        {
                            string subArgs = requestUrlArgsSplit[1];
                            string[] argsRaw = subArgs.Split("&");

                            foreach (string raw in argsRaw)
                            {
                                // Split the values, username=User -> ["username", "User"] (or if there is no =, it's just the key)
                                string[] split = raw.Split("=");
                                if (split.Length == 2)
                                    argsFinal.Add(HttpUtility.UrlDecode(split[0]), HttpUtility.UrlDecode(split[1]));
                                else
                                    argsFinal.Add(HttpUtility.UrlDecode(raw), null);
                            }
                        }

                        // Try to retrieve an Authorization header (has the session key) if there is one present
                        string auth = "";
                        if (data.Contains("Authorization: "))
                        {
                            auth = data.Split("Authorization: ")[1].Split("\n")[0].Replace("\r", "").Trim();
                        }

                        var body = data.Split("\r\n\r\n")[1];

                        int code = Code.NOT_FOUND;
                        string codeName = Message.NOT_FOUND;
                        byte[] payload = new byte[0];

                        foreach (string key in requestHandlers.Keys)
                        {
                            // Check if the handler is set up to accept this url (i.e. "/user/login")
                            // The second if is incase the request includes an extra / (/user/login/)
                            if (key.ToLower() == requestUrlOnly.ToLower() || key.ToLower() + "/" == requestUrlOnly.ToLower())
                            {
                                var handler = requestHandlers[key];

                                // Check if the handler is set up to use the requested method.
                                if (Enum.GetName(handler.Method) == requestMethod)
                                {
                                    // Let the handler handler everything, and then use its Response object's values.
                                    Response handled = handler.HandleRequest(auth, argsFinal, body);

                                    code = handled.Code;
                                    codeName = handled.Message;
                                    payload = handled.Data;
                                }
                                else
                                {
                                    // Return "405 Method Not Allowed" if not
                                    code = Code.METHOD_NOT_ALLOWED;
                                    codeName = Message.METHOD_NOT_ALLOWED;
                                }
                                break;
                            }
                        }

                        // The default start of the HTTP header, also asking the connection to be closed.
                        // Also defining the content length
                        byte[] header = Encoding.UTF8.GetBytes($"HTTP/1.1 {code} {codeName}\r\nConnection: close\r\nContent-Type: application/json\r\nContent-Length: {payload.Length}\r\n\r\n");

                        // We need to add the data to the header, so we create a new array with the right length...
                        byte[] response = new byte[header.Length + payload.Length];

                        // ...then add the header...
                        for (int i = 0; i < header.Length; i++)
                            response[i] = header[i];

                        // ...and then finally add the data.
                        for (int i = 0; i < payload.Length; i++)
                            response[i + header.Length] = payload[i];

                        // We can then finally send all this as a response.
                        await currentConnection.SendAsync(response, SocketFlags.None);
                    }
                }

                // No matter what happens, we should always end by properly closing the connection/socket.
                currentConnection.Disconnect(false);
                currentConnection.Close();
            }
        }
    }
}