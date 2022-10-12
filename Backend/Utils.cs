using System;
using System.Security.Cryptography;
using System.Text;

namespace ChengetaBackend {

    public class Utils {

        public static DateTime UnixTimeStampToDateTime(long unixTimeStamp) {
            return new DateTime(1970, 1, 1, 0, 0, 0, 0, DateTimeKind.Utc).AddSeconds(unixTimeStamp);
        }

        public static string HashPassword(string password, string salt){
            // Add the salt to the password to add randomness to result hashes
            string newPassword = password + salt;
            // Create a new hash object to use
            using (SHA256 mySHA256 = SHA256.Create()){
                // Hash newPassword using the object we created, resulting in an array of bytes containing the hashed password
                byte[] hashValue = mySHA256.ComputeHash(Encoding.ASCII.GetBytes(newPassword));
                // Convert the byte array into a readable/storable string
                return Convert.ToHexString(hashValue);
            }
        }

    }

}