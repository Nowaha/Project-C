using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ChengetaBackend.Migrations
{
    public partial class mi3 : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropPrimaryKey(
                name: "PK_session",
                table: "session");

            migrationBuilder.RenameTable(
                name: "session",
                newName: "sessions");

            migrationBuilder.RenameColumn(
                name: "IdRanger",
                table: "sessions",
                newName: "RangerId");

            migrationBuilder.AddPrimaryKey(
                name: "PK_sessions",
                table: "sessions",
                column: "Id");

            migrationBuilder.CreateIndex(
                name: "IX_sessions_RangerId",
                table: "sessions",
                column: "RangerId");

            migrationBuilder.AddForeignKey(
                name: "FK_sessions_accounts_RangerId",
                table: "sessions",
                column: "RangerId",
                principalTable: "accounts",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_sessions_accounts_RangerId",
                table: "sessions");

            migrationBuilder.DropPrimaryKey(
                name: "PK_sessions",
                table: "sessions");

            migrationBuilder.DropIndex(
                name: "IX_sessions_RangerId",
                table: "sessions");

            migrationBuilder.RenameTable(
                name: "sessions",
                newName: "session");

            migrationBuilder.RenameColumn(
                name: "RangerId",
                table: "session",
                newName: "IdRanger");

            migrationBuilder.AddPrimaryKey(
                name: "PK_session",
                table: "session",
                column: "Id");
        }
    }
}
