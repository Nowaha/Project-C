using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net.Http;
using static ChengetaBackend.Account;

namespace ChengetaBackend
{
    public class EndpointAuthLevelTests
    {
        private record EndpointForTesting(RequestHandler handler, AccountType? role);
        private static List<EndpointForTesting> endpoints = new List<EndpointForTesting>();
        private static List<string> testAccounts = new List<string>();

        public static void setupEndpoints()
        {
            if (endpoints.Count != 0) return;

            var loginHandler = new LoginRequestHandler();
            endpoints.Add(new EndpointForTesting(loginHandler, null));
            var accountCreateHandler = new AccountCreationRequestHandler();
            endpoints.Add(new EndpointForTesting(accountCreateHandler, AccountType.ADMIN));
            var accountDeleteHandler = new AccountDeleteRequestHandler();
            endpoints.Add(new EndpointForTesting(accountDeleteHandler, AccountType.ADMIN));
            var latestEventsHandler = new LatestEventsRequestHandler();
            endpoints.Add(new EndpointForTesting(latestEventsHandler, AccountType.RANGER));
            var accountListHandler = new AccountSearchRequestHandler();
            endpoints.Add(new EndpointForTesting(accountListHandler, AccountType.ADMIN));
            var sessionValidationHandler = new SessionValidationRequestHandler();
            endpoints.Add(new EndpointForTesting(sessionValidationHandler, null));
            var accountEditHandler = new AccountEditRequestHandler();
            endpoints.Add(new EndpointForTesting(accountEditHandler, AccountType.ADMIN));
        }

        public static void testEndpoints(bool verbose = false)
        {
            Console.WriteLine("Testing endpoint authentication levels...");

            setupEndpoints();
            
            Program.sessionManager = new SessionManager();

            string adminSession;
            string rangerSession;

            using (var context = new ChengetaContext())
            {
                rangerSession = createTestAccount(context, AccountType.RANGER);
                adminSession = createTestAccount(context, AccountType.ADMIN);
            }

            var notAllowedCodes = new List<int>() { Code.UNAUTHORIZED, Code.FORBIDDEN };

            foreach (EndpointForTesting endpoint in endpoints)
            {
                try
                {
                    if (verbose) { Console.Write($"- Testing endpoint {endpoint.handler.GetType().Name}..."); }
                    var allowNoSession = endpoint.role == null;
                    var allowRangerSession = endpoint.role == AccountType.RANGER || allowNoSession;
                    var allowAdminSession = endpoint.role == AccountType.ADMIN || allowRangerSession || allowNoSession;

                    var noSessionResponse = endpoint.handler.HandleRequest("", new Dictionary<string, string>(), "");
                    var rangerResponse = endpoint.handler.HandleRequest(rangerSession, new Dictionary<string, string>(), "");
                    var adminResponse = endpoint.handler.HandleRequest(adminSession, new Dictionary<string, string>(), "");
                    
                    assert(allowNoSession ? !notAllowedCodes.Contains(noSessionResponse.Code) : notAllowedCodes.Contains(noSessionResponse.Code), $"Sessionless access wrong. ({noSessionResponse.Code} {noSessionResponse.Message})");
                    assert(allowRangerSession ? !notAllowedCodes.Contains(rangerResponse.Code) : notAllowedCodes.Contains(rangerResponse.Code), $"Ranger access wrong. ({rangerResponse.Code} {rangerResponse.Message})");
                    assert(allowAdminSession ? !notAllowedCodes.Contains(adminResponse.Code) : notAllowedCodes.Contains(adminResponse.Code), $"Admin access wrong. ({adminResponse.Code} {adminResponse.Message})");
                    if (verbose) { 
                        Console.ForegroundColor = ConsoleColor.Green;
                        Console.WriteLine(" Passed!");
                    }
                    Console.ResetColor();
                } catch (Exception ex) {
                    cleanup(verbose);
                    Console.Write("\n");
                    Console.WriteLine(ex);
                    Debug.Fail("Exception occurred during execution.");
                }
            }

            cleanup(verbose);
        }

        private static void assert(bool expression, string message) {
            if (!expression) {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.Write(" FAILED!\n");
                Console.ResetColor();
                cleanup(true);
                Debug.Fail(message);
                return;
            }
            Debug.Assert(expression, message);
        }

        private static string createTestAccount(ChengetaContext db, AccountType type)
        {
            var passRaw = Guid.NewGuid().ToString();
            var newPassDetails = Utils.HashNewPassword(passRaw);
            var adminPassHash = newPassDetails.hashed;
            var adminSalt = newPassDetails.salt;

            string randomPart = (new Random().NextInt64()).ToString();
            string username = "TEMP" + randomPart.Substring(0, Math.Min(16, randomPart.Length));

            db.accounts.Add(new Account()
            {
                CreationDate = System.DateTime.UtcNow,
                Username = username,
                Password = adminPassHash,
                Salt = adminSalt,
                Role = type
            });

            db.SaveChanges();

            testAccounts.Add(username);

            return Program.sessionManager.Authenticate(username, passRaw).sessionKey;
        }

        private static void cleanup(bool verbose = false)
        {
            Program.sessionManager.SessionDictionary.Clear();
            using (var context = new ChengetaContext())
            {
                var toRemove = context.accounts.Where(it => testAccounts.Contains(it.Username));
                foreach (var obj in toRemove) {
                    if (verbose) { Console.WriteLine("Removing account " + obj.Username);}
                }
                context.accounts.RemoveRange(toRemove);
                context.SaveChanges();
            }
        }
    }
}