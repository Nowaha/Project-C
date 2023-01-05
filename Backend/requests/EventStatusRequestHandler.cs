using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json;

namespace ChengetaBackend
{
    class EventStatusRequestHandler : RequestHandler

    {
        public string Path => "/events/status";

        public Method Method => Method.POST;
    }


    public Response HandleRequest(string session, Dictionary<string, string> args, string bodyRaw)
    {

        if (!Program.sessionManager.SessionDictionary.ContainsKey(session))
        {
            return Response.generateBasicError(Code.UNAUTHORIZED, Message.UNAUTHORIZED, "Invalid session");
        }




        EventStatusEditRequest request;

        try
        {
            request = JsonSerializer.Deserialize<EventStatusEditRequest>(bodyRaw);
        }
        catch (Exception)
        {
            return Response.generateBasicError(Code.BAD_REQUEST, Message.BAD_REQUEST, "Invalid request structure.");
        }


        int eventId = request.eventId;
        int status = request.status;




        using (var db = new ChengetaContext())
        {

            var currentEvent = db.events.Where(currEvent => currEvent.Id == eventId).FirstOrDefault();
            if (currentEvent == null)
            {
                return Response.generateBasicError(
                        Code.BAD_REQUEST,
                        Message.BAD_REQUEST,
                        "Event doesn't exist.");
            }
            currentEvent.Status = status;
            db.SaveChanges();


            return new Response(
                Code.SUCCESS,
                Message.SUCCESS,
                Encoding.UTF8.GetBytes(
                    JsonSerializer.Serialize(
                        new
                        {
                            success = true,
                            message = $"Event status of event {eventId} updated successfully"
                        }

                ))
            );
        }


    }


}