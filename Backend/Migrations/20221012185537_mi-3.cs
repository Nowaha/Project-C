using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ChengetaBackend.Migrations
{
    public partial class mi3 : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.RenameColumn(
                name: "IdRanger",
                table: "session",
                newName: "RangerId");

            migrationBuilder.CreateIndex(
                name: "IX_session_RangerId",
                table: "session",
                column: "RangerId");

            migrationBuilder.AddForeignKey(
                name: "FK_session_accounts_RangerId",
                table: "session",
                column: "RangerId",
                principalTable: "accounts",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_session_accounts_RangerId",
                table: "session");

            migrationBuilder.DropIndex(
                name: "IX_session_RangerId",
                table: "session");

            migrationBuilder.RenameColumn(
                name: "RangerId",
                table: "session",
                newName: "IdRanger");
        }
    }
}
