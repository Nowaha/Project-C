using System;
using System.Net;
using System.Net.Sockets;

namespace ChengetaBackend
{
    class Environment
    {

        public static string HTTP_IP
        {
            get
            {
                var env = System.Environment.GetEnvironmentVariable("HTTP_IP");
                if (env != null) return env;

                try
                {
                    using (Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, 0))
                    {
                        socket.Connect("8.8.8.8", 65530);
                        IPEndPoint endPoint = socket.LocalEndPoint as IPEndPoint;
                        return endPoint.Address.ToString();
                    }
                }
                catch (Exception)
                {
                    return "127.0.0.1";
                }

            }
        }

        public static int HTTP_PORT
        {
            get
            {
                try
                {
                    return int.Parse(getOrDefault("HTTP_PORT", "34100"));
                }
                catch (Exception ex)
                {
                    System.Console.WriteLine(ex);
                }

                return 34100;
            }
        }

        public static string PSQL_USER
        {
            get
            {
                return getOrDefault("PSQL_USER", "postgres");
            }
        }

        public static string PSQL_PASS
        {
            get
            {
                return getOrDefault("PSQL_PASS", "");
            }
        }

        public static string PSQL_HOST
        {
            get
            {
                return getOrDefault("PSQL_HOST", "localhost");
            }
        }

        public static string PSQL_PORT
        {
            get
            {
                return getOrDefault("PSQL_PORT", "5432");
            }
        }

        public static string PSQL_DB
        {
            get
            {
                return getOrDefault("PSQL_DB", "ChengetaApp");
            }
        }

        public static string generateConnectionString()
        {
            string res = $"UserID={PSQL_USER};";
            if (PSQL_PASS != null && PSQL_PASS != "")
            {
                res += $"Password={PSQL_PASS};";
            }
            res += $"Host={PSQL_HOST};port={PSQL_PORT};Database={PSQL_DB};Pooling=true;";
            return res;
        }

        private static string getOrDefault(string key, string defaultValue)
        {
            string val = System.Environment.GetEnvironmentVariable(key);
            if (val == null) return defaultValue;
            return val;
        }
    }
}