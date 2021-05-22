import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class LibraryModel {

  // For use in creating dialogs and making them modal
  private JFrame dialogParent;


  Connection conn;
  String url = "jdbc:postgresql://db.ecs.vuw.ac.nz/nicholowen_jdbc";

  public LibraryModel(JFrame parent, String userid, String password) {
    dialogParent = parent;

    try {
      Class.forName("org.postgresql.Driver");
      conn = DriverManager.getConnection(url, userid, password);
      System.out.println(conn.getMetaData());

    } catch (SQLException | ClassNotFoundException throwables) {
      throwables.printStackTrace();
    }


  }

  public String bookLookup(int isbn) {

    String lookup = "SELECT b.ISBN, b.Title, a.Surname, b.Edition_No, b.NumOfCop, b.NumLeft " +
                    "FROM Book AS b "+
                    "LEFT JOIN Book_Author AS ba ON b.ISBN = ba.ISBN " +
                    "LEFT JOIN Author AS a ON a.AuthorID = ba.AuthorID " +
                    "WHERE b.ISBN = " + isbn + ";";

    String title = "";
    ArrayList<String> authors = new ArrayList<>();
    int edition = 0;
    int copies_total = 0;
    int copies_remaining = 0;
    int count = 0;

    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);
      while (rs.next()){
        count++;
        if(rs.getString("Title") != null) title = rs.getString("Title").trim();
        if(rs.getString("Surname") != null) authors.add(rs.getString("Surname").trim());
        edition = rs.getInt("Edition_No");
        copies_total = rs.getInt("NumOfCop");
        copies_remaining = rs.getInt("NumLeft");

      }

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    StringBuilder sb = new StringBuilder();
    sb.append("Book Lookup:\n");
    if(count == 0) {
      sb.append("\tNo such ISBN: '" + isbn + "'");
      return sb.toString();
    }
    sb.append("\t" + isbn + ": ").append(title);
    sb.append("\n\tEdition: ").append(edition);
    sb.append(" - Number of Copies: ").append(copies_remaining + " - Copies Left: " + copies_total + "\n");
    if(authors.isEmpty()) sb.append("\t(no authors)");
    else {
      sb.append((authors.size() > 1) ? "\tAuthors: " : "\tAuthor: ");
      for (int i = 0; i < authors.size(); i++) {
        sb.append(authors.get(i));
        if (i != authors.size() - 1) sb.append(", ");
      }
    }
    return sb.toString();
  }

  public String showCatalogue() {

    String lookup = "SELECT b.ISBN, b.Title, string_agg(a.Surname, ', ') AS Surname, b.Edition_No, b.NumOfCop, b.NumLeft " +
                    "FROM Book AS b "+
                    "LEFT JOIN Book_Author AS ba ON b.ISBN = ba.ISBN " +
                    "LEFT JOIN Author AS a ON a.AuthorID = ba.AuthorID " +
                    "GROUP BY b.ISBN, b.Title, b.Edition_No, b.NumOfCop, b.NumLeft " +
                    "ORDER BY b.isbn;";

    StringBuilder sb = new StringBuilder();

    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);
      while (rs.next()) {

        int isbn;
        String title = "";
        String author = "";
        int edition;
        int copies_total;
        int copies_remaining;

        isbn = rs.getInt("ISBN");
        if (rs.getString("Title") != null) title = rs.getString("Title").trim();
        if (rs.getString("Surname") != null) author = rs.getString("Surname").trim();
        edition = rs.getInt("Edition_No");
        copies_total = rs.getInt("NumOfCop");
        copies_remaining = rs.getInt("NumLeft");


        sb.append("Book Lookup:\n");
        sb.append("\t" + isbn + ": ").append(title);
        sb.append("\n\tEdition: ").append(edition);
        sb.append(" - Number of Copies: ").append(copies_remaining + " - Copies Left: " + copies_total + "\n");
        if(author.equals("")) sb.append("\t(no authors)");
        else sb.append((author.contains(",")) ? "\tAuthors: " : "\tAuthor: ");
        sb.append(author + "\n");
      }

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return sb.toString();
  }

  public String showLoanedBooks() {
    return "Show Loaned Books Stub";
  }

  public String showAuthor(int authorID) {
    String lookup = "SELECT * FROM Author ORDER BY AuthorID;";

    StringBuilder sb = new StringBuilder();
    sb.append("Show All Authors:\n");
    Statement stmt = null;

    try {
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);

      while (rs.next()){
        sb.append("\t");
        sb.append(rs.getInt("AuthorID")).append(": ");
        sb.append(rs.getString("Surname").trim()).append(", ");
        sb.append(rs.getString("Name").trim()).append("\n");
      }

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return sb.toString();
  }

  public String showAllAuthors() {

    String lookup = "SELECT * FROM Author ORDER BY AuthorID;";

    StringBuilder sb = new StringBuilder();
    sb.append("Show All Authors:\n");
    Statement stmt = null;

    try {
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);

      while (rs.next()){
        sb.append("\t");
        sb.append(rs.getInt("AuthorID")).append(": ");
        sb.append(rs.getString("Surname").trim()).append(", ");
        sb.append(rs.getString("Name").trim()).append("\n");
      }

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return sb.toString();
  }

  public String showCustomer(int customerID) {
    return "Show Customer Stub";
  }

  public String showAllCustomers() {
    String lookup = "SELECT * FROM Customer ORDER BY CustomerID;";

    StringBuilder sb = new StringBuilder();
    sb.append("Show All Customers:\n");
    Statement stmt = null;

    try {
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);

      while (rs.next()){
        sb.append("\t");
        sb.append(rs.getInt("CustomerID")).append(": ");
        sb.append(rs.getString("l_name").trim()).append(", ");
        sb.append(rs.getString("f_name").trim()).append(" - ");
        if(rs.getString("city") != null) sb.append(rs.getString("city").trim()).append("\n");
        else sb.append("(no city)").append("\n");
      }

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return sb.toString();
  }

  public String borrowBook(int isbn, int customerID,
                           int day, int month, int year) {
    return "Borrow Book Stub";
  }

  public String returnBook(int isbn, int customerid) {
    return "Return Book Stub";
  }

  public void closeDBConnection() {
  }

  public String deleteCus(int customerID) {
    return "Delete Customer";
  }

  public String deleteAuthor(int authorID) {
    return "Delete Author";
  }

  public String deleteBook(int isbn) {
    return "Delete Book";
  }
}


