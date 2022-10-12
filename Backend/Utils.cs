using System;

namespace ChengetaBackend {

    public class Utils {

        public static DateTime UnixTimeStampToDateTime(long unixTimeStamp) {
            return new DateTime(1970, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc).AddSeconds(unixTimeStamp);
        }

    }

}