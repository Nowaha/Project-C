using System.Diagnostics.Tracing;
using System;
using System.Collections.Generic;
using System.Text;
using System.Text.Json;
using System.Linq;

namespace ChengetaBackend
{
    class LatestEventsRequestHandler : RequestHandler
    {
        public string Path => "/events/latest";
        public Method Method => Method.GET;

        private static int minimumRowCount = 1;
        private static int defaultRowCount = 10;
        private static int maximumRowCount = 100;

        private static int minimumOffset = 0;
        private static int defaultOffset = 0;
        private static int maximumOffset = int.MaxValue;

        public Response HandleRequest(string session, Dictionary<string, string> args, string bodyRaw)
        {
            if (!Program.sessionManager.SessionDictionary.ContainsKey(session)) return Response.generateBasicError(Code.UNAUTHORIZED, Message.UNAUTHORIZED, "Invalid session");

            int rows = defaultRowCount;
            int offset = defaultOffset;

            if (args.ContainsKey("rows"))
            {
                int rowsTemp;
                if (int.TryParse(args["rows"], out rowsTemp))
                {
                    if (rowsTemp < minimumRowCount) return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, $"Minimum row count of {minimumRowCount}.");
                    if (rowsTemp > maximumRowCount) return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, $"Maximum row count of {maximumRowCount}.");
                    rows = rowsTemp;
                }
            }

            if (args.ContainsKey("offset"))
            {
                int offsetTemp;
                if (int.TryParse(args["offset"], out offsetTemp))
                {
                    if (offsetTemp < minimumOffset) return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, $"Minimum offset count of {minimumOffset}.");
                    if (offsetTemp > maximumOffset) return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, $"Maximum offset count of {maximumOffset}.");
                    offset = offsetTemp;
                }
            }

            var events = new List<Object>();

            using (var db = new ChengetaContext())
            {
                events.AddRange(
                    (from evt in db.events
                     orderby evt.Date descending
                     select new
                     {
                         Id = evt.Id,
                         NodeId = evt.NodeId,
                         Date = (long)((DateTime)(evt.Date)).ToUniversalTime().Subtract(new DateTime(1970, 1, 1)).TotalMilliseconds,
                         Latitude = evt.Latitude,
                         Longitude = evt.Longitude,
                         SoundLabel = evt.SoundLabel,
                         Probability = evt.Probability,
                         SoundURL = evt.SoundURL,
                         Status = evt.Status
                     }
                     ).Skip(offset).Take(rows).ToList());
            }

            var res = new
            {
                success = true,
                message = $"Retrieved {events.Count} rows.",
                data = events
            };

            return new Response(Code.SUCCESS, Message.SUCCESS, Encoding.UTF8.GetBytes(JsonSerializer.Serialize(res)));
        }
    }
}