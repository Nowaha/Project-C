using System;

namespace ChengetaBackend {
    class DatabaseLogin {
        public static string USERNAME {
            get {
                return getOrDefault("PSQL_USER", "postgres");
            }
        }

        public static string PASSWORD {
            get {
                return getOrDefault("PSQL_PASS", "");
            }
        }

        public static string HOST {
            get {
                return getOrDefault("PSQL_HOST", "localhost");
            }
        }

        public static string PORT {
            get {
                return getOrDefault("PSQL_PORT", "5432");
            }
        }

        public static string DATABASE {
            get {
                return getOrDefault("PSQL_DB", "ChengetaApp");
            }
        }

        public static string generateConnectionString() {
            string res = $"UserID={USERNAME};";
            if (PASSWORD != null && PASSWORD != "") {
                res += $"Password={PASSWORD};";
            }
            res += $"Host={HOST};port={PORT};Database={DATABASE};Pooling=true;";
            return res;
        }

        private static string getOrDefault(string key, string defaultValue) {
            string val = Environment.GetEnvironmentVariable(key);
            if (val == null) return defaultValue;
            return val;
        }
    }
}