FROM mcr.microsoft.com/dotnet/runtime:6.0

# Copy built files to appropiate directories
COPY ./bin/Debug/net6.0/linux-x64 .
COPY ./ ./src
RUN rm -r ./src/bin

# Install .NET SDK (6.0) with the dotnet Entity Framework tools
RUN apt-get update
RUN apt-get -y install wget
RUN wget https://packages.microsoft.com/config/debian/11/packages-microsoft-prod.deb -O packages-microsoft-prod.deb
RUN dpkg -i packages-microsoft-prod.deb
RUN rm packages-microsoft-prod.deb
RUN apt-get update
RUN apt-get install -y dotnet-sdk-6.0
RUN dotnet tool install --global dotnet-ef
ENV PATH $PATH:/root/.dotnet/tools

# Update database & run server
ENTRYPOINT ["/bin/sh", "-c", "(cd /src/; dotnet ef database update) && dotnet ChengetaBackend.dll"]
