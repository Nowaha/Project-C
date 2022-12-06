using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Web;

namespace ChengetaBackend
{

    public class Response
    {
        public int Code { get; set; }
        public string Message { get; set; }
        public byte[] Data { get; set; }

        public Response(int code, string message, byte[] data)
        {
            this.Code = code;
            this.Message = message;
            this.Data = data;
        }

        public static Response generateBasicError(int code, string message, string errorMessage) {
            return new Response(code, message, Encoding.UTF8.GetBytes(JsonSerializer.Serialize(new {
                success = false,
                message = errorMessage
            })));
        }
    }

    public static class DefaultResponses
    {
        public static readonly Response BAD_REQUEST = new Response(Code.BAD_REQUEST, Message.BAD_REQUEST, new byte[0]);
        public static readonly Response NOT_FOUND = new Response(Code.NOT_FOUND, Message.NOT_FOUND, new byte[0]);
    }

    public static class Code
    {
        public static readonly int SUCCESS = 200;
        public static readonly int BAD_REQUEST = 400;
        public static readonly int UNAUTHORIZED = 401;
        public static readonly int FORBIDDEN = 403;
        public static readonly int NOT_FOUND = 404;
        public static readonly int METHOD_NOT_ALLOWED = 405;

        public static readonly int SERVER_ERROR = 500;
    }

    public static class Message
    {
        public static readonly string SUCCESS = "OK";
        public static readonly string BAD_REQUEST = "BAD REQUEST";
        public static readonly string UNAUTHORIZED = "UNAUTHORIZED";
        public static readonly string FORBIDDEN = "FORBIDDEN";
        public static readonly string NOT_FOUND = "NOT FOUND";
        public static readonly string METHOD_NOT_ALLOWED = "METHOD NOT ALLOWED";
        public static readonly string SERVER_ERROR = "SERVER_ERROR";
    }

}