using System.Net;
using System.Collections.Generic;
using System.Net.Sockets;
using System.Text;
using System;
using System.Web;

namespace ChengetaBackend
{
    class Server
    {
        private static string listeningIP = "192.168.178.175";
        private static int listeningPort = 34100;

        private static int maxSize = 100;
        private static Dictionary<string, RequestHandler> requestHandlers = new Dictionary<string, RequestHandler>();

        private static void setupHandlers() {
            requestHandlers.Clear();
            requestHandlers.Add("/user/login", new LoginRequestHandler());
        }

        public static void Run()
        {
            System.Console.WriteLine("Server is starting...");

            setupHandlers();

            IPAddress ipAddress = IPAddress.Parse(listeningIP);
            IPEndPoint localEndPoint = new IPEndPoint(ipAddress, listeningPort);

            Socket listener = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
            listener.Bind(localEndPoint);
            listener.Listen(100);

            while (true)
            {
                Socket currentConnection = listener.Accept();

                System.Console.WriteLine("New request, reading...");

                string data = "";

                int loop = 0;
                do
                {
                    var buffer = new byte[1024];
                    int receivedData = currentConnection.Receive(buffer);
                    data += Encoding.ASCII.GetString(buffer, 0, receivedData);

                    loop++;
                    if (loop > maxSize)
                    {
                        System.Console.WriteLine("Terminated early!");
                    }
                } while (currentConnection.Available > 0);

                if (data.Length == 0 || data.Trim().Length == 0)
                {
                    currentConnection.Send(Encoding.ASCII.GetBytes("empty request"));
                    currentConnection.Disconnect(false);
                    currentConnection.Close();
                    continue;
                }

                var httpLine = data.Split("\n")[0];
                var httpRequestData = httpLine.Split(" ");
                if (httpRequestData.Length == 3)
                {
                    var requestMethod = httpRequestData[0].Trim();

                    var requestSub = httpRequestData[1].Trim();
                    var requestSplit = requestSub.Split("?");
                    var subUrl = requestSplit[0];
                    var subArgs = requestSplit[1];

                    var argsRaw = subArgs.Split("&");

                    var argsFinal = new Dictionary<string, string>();
                    foreach (string raw in argsRaw) {
                        var split = raw.Split("=");
                        if (split.Length == 2) {
                            argsFinal.Add(HttpUtility.UrlDecode(split[0]), HttpUtility.UrlDecode(split[1]));
                        } else {
                            argsFinal.Add(HttpUtility.UrlDecode(raw), null);
                        }
                    }

                    var code = Code.NOT_FOUND;
                    var codeName = Message.NOT_FOUND;
                    var res = new byte[0];

                    foreach (string key in requestHandlers.Keys) {
                        if (key.ToLower() == subUrl.ToLower()) {
                            var handler = requestHandlers[key];
                            if (Enum.GetName(handler.Method) == requestMethod) {
                                var handled = handler.HandleRequest(argsFinal);
                                code = handled.Code;
                                codeName = handled.Message;
                                res = handled.Data;
                            } else {
                                code = Code.METHOD_NOT_ALLOWED;
                                codeName = Message.METHOD_NOT_ALLOWED;
                            }
                            break;
                        }
                    }

                    var header = Encoding.UTF8.GetBytes($"HTTP/1.1 {code} {codeName} \r\n Connection: close \r\n\r\n");

                    byte[] result = new byte[header.Length + res.Length];
                    for (int i = 0; i < header.Length; i++)
                    {
                        result[i] = header[i];
                    }
                    for (int i = 0; i < res.Length; i++)
                    {
                        result[i + header.Length] = res[i];
                    }
                    currentConnection.Send(result);
                }

                currentConnection.Disconnect(false);
                currentConnection.Close();
            }
        }
    }
}