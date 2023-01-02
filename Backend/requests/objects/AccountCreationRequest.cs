namespace ChengetaBackend {
    class AccountCreationRequest {
        public string username { get; set; }
        public string password { get; set; }
        public int role { get; set; }
        public string firstName { get; set; }
        public string lastName { get; set; }
    }
}