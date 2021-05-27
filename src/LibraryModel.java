import javax.swing.*;
import java.sql.*;
import java.text.SimpleDateFormat;
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
    sb.append(" - Number of Copies: ").append(copies_total + " - Copies Left: " + copies_remaining + "\n");
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
        sb.append(" - Number of Copies: ").append(copies_total + " - Copies Left: " + copies_remaining + "\n");
        if(author.equals("")) sb.append("\t(no authors)");
        else sb.append((author.contains(",")) ? "\tAuthors: " : "\tAuthor: ");
        sb.append(author + "\n");
      }

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return sb.toString();
  }

  //TODO
  public String showLoanedBooks() {

    String lookup =
   "SELECT b.ISBN, b.Title, string_agg(a.Surname, ', ') AS Surname, b.Edition_No, b.NumOfCop, b.NumLeft, c.f_name, c.L_name, c.city" +
   "FROM Book AS b" +
   "LEFT JOIN Book_Author AS ba ON b.ISBN = ba.ISBN" +
   "LEFT JOIN Author AS a ON a.AuthorID = ba.AuthorID" +
   "LEFT JOIN Cust_Book AS cb ON b.ISBN = cb.ISBN" +
   "LEFT JOIN Customer AS c ON cb.CustomerID = c.CustomerID" +
   "GROUP BY b.ISBN, b.Title, b.Edition_No, b.NumOfCop, b.NumLeft, c.f_name, c.L_name, c.city" +
   "ORDER BY b.isbn;";



    ArrayList<String> customers = new ArrayList<>();

    try {
      conn.setAutoCommit(false);
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);

      while(rs.next()){



      }



      conn.setAutoCommit(true);
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }


    return "Show Loaned Books Stub";
  }

  public String showAuthor(int authorID) {
    String lookup = "SELECT a.AuthorID, a.Surname, a.Name, b.ISBN, b.Title " +
                    "FROM Author AS a " +
                    "LEFT JOIN Book_Author AS ba ON a.AuthorID = ba.AuthorID " +
                    "LEFT JOIN Book AS b ON ba.ISBN = b.ISBN " +
                    "WHERE a.AuthorID = " + authorID + ";";


    Statement stmt = null;
    ArrayList<String> books = new ArrayList<>();

    int author_id = 0;
    String name = null;
    String surname = null;
    int isbn = 0;
    String title = null;

    int count = 0;

    try {
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);

      while (rs.next()){
        author_id = rs.getInt("AuthorID");
        if (rs.getString("Surname") != null) surname = rs.getString("Surname").trim();
        if (rs.getString("Name") != null) name = rs.getString("Name").trim();
        if (rs.getInt("ISBN") != 0) isbn = rs.getInt("ISBN");
        if (rs.getString("Title") != null) title = rs.getString("Title").trim();
        if(title != null) books.add(isbn + " - " + title);
        count++;
      }

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    StringBuilder sb = new StringBuilder();
    sb.append("Show Author:\n");
    sb.append("\t");
    if(count == 0) sb.append("No such Author ID: ").append(authorID);
    else {
      sb.append(author_id).append(" - ");
      sb.append(name).append(" ").append(surname).append("\n");
      if (books.isEmpty()) sb.append("\t").append("(no books written)");
      else{
        sb.append("\tBooks Written:\n");
        for (int i = 0; i < books.size(); i++) {
          sb.append("\t\t").append(books.get(i));
          if(i < books.size() -1) sb.append("\n");
        }
      }
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


  //TODO
  public String showCustomer(int customerID) {

    //unsure if this is correct. No books have been loaned yet to check.
    String lookup = "SELECT DISTINCT c.customerid, c.l_name, c.f_name, c.city, cd.borrowed_items " +
            "FROM customer AS c, (SELECT customerid, count(customerid) AS borrowed_items" +
            "FROM cust_book AS cb" +
            "GROUP BY customerid) AS cd, cust_book AS cb" +
            "LEFT JOIN book AS b ON cb.isbn = b.isbn" +
            "WHERE cb.customerid = c.customerid AND c.customer = " + customerID + ";";

    StringBuilder sb = new StringBuilder();
    sb.append("Show Customer: \n");
    try {

      String bookTitle = "";
      int numBooksBorrowed = 0;
      int count = 0;

      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);
      while (rs.next()){
        sb.append(rs.getString("customerid")).append(": ");
        sb.append(rs.getString("l_name").trim()).append(", ");
        sb.append(rs.getString("f_name").trim()).append(" - ");
        sb.append(rs.getString("city").trim()).append("\n");

      }

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

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

  /** TODO - protect the book table from going below 0 (will throw an error anyway, but avoid).
   *  Also need to create popup to confirm (i.e. lock)
   */
  public String borrowBook(int isbn, int customerID, int day, int month, int year) {

    //update the book's quantity
    //update the customers isbn and add due date

    StringBuilder sb = new StringBuilder();

    String insert = "INSERT INTO cust_book(isbn, duedate, customerid) " +
                    "VALUES (?, ?, ?);";
    String update = "UPDATE book SET numleft = numleft - 1 " +
                    "WHERE isbn = " + isbn + ";";
    String bookQuery = "Select * FROM book WHERE isbn = " + isbn + " FOR  UPDATE;";
    String custQuery = "Select * FROM customer WHERE customerid = " + customerID + " FOR UPDATE;";

    String bookTitle = "";
    String custName = "";
    int books_left = 0;

    try {
      conn.setAutoCommit(false);

      PreparedStatement pInsert = conn.prepareStatement(insert);

      pInsert.setInt(1, isbn);
      pInsert.setDate(2, java.sql.Date.valueOf(year + "-" + month + "-" + day));
      pInsert.setInt(3, customerID);

      Statement stmt = conn.createStatement();
      ResultSet rs1 = stmt.executeQuery(bookQuery);
      while(rs1.next()){
        if(rs1.getString("title") != null) bookTitle = rs1.getString("title").trim();
        books_left = rs1.getInt("numLeft");
      }
      ResultSet rs2 = stmt.executeQuery(custQuery);
      while(rs2.next()){
        if(rs2.getString("f_name") != null && rs2.getString("l_name") != null) {
          custName = rs2.getString("f_name").trim() + " " + rs2.getString("l_name").trim();
        }
      }

      if(!bookTitle.equals("") && !custName.equals("") && books_left > 0) {
        showDialog();
        pInsert.executeUpdate();
        if (stmt.executeUpdate(update) == 0) return "No entry found.";

        conn.commit();
        conn.setAutoCommit(true);
      }

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    if(bookTitle.equals("") && custName.equals("")) {
      sb.append("Borrow Book\n");
      sb.append("\tBook: ").append(isbn).append(" (").append(bookTitle).append(")\n");
      sb.append("\tLoaned to: ").append(customerID).append(" (").append(custName).append(")\n");
      sb.append("\tDue Date: ").append(day + " " + month + " " + year);
    }else{
      sb.append("Error encountered - Either Customer or ISBN does not exist.");
    }


    return sb.toString();
  }

  //TODO
  public String returnBook(int isbn, int customerid) {

    //check the cust_book table to see if the isbn and customer id match
    //if it does, then remove the entry.
    //increment 1 to numleft in book table

    StringBuilder sb = new StringBuilder();

    String checkCustomer = "SELECT * FROM cust_book WHERE isbn = " + isbn + " AND  customerid = " + customerid + " FOR UPDATE;";
    String checkBook = "SELECT * FROM book WHERE isbn = " + isbn + " FOR UPDATE;";

    String delete = "DELETE FROM cust_book WHERE isbn = " + isbn + " AND  customerid = " + customerid + ";";
    String update = "UPDATE book SET numleft = numleft + 1 WHERE isbn = " + isbn + ";";

    int cust_id = 0;
    int cust_isbn = 0;
    int book_isbn = 0;

    try {
      conn.setAutoCommit(false);

      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(checkCustomer);
      while(rs.next()){
        cust_id = rs.getInt("customerid");
        cust_isbn = rs.getInt("isbn");
      }
      rs = stmt.executeQuery(checkBook);
      while(rs.next()){
        book_isbn = rs.getInt(isbn);
      }

      if(cust_id != 0 && cust_isbn != 0 && book_isbn != 0) {
        //shows dialog message to pause.
        showDialog();
        if (stmt.executeUpdate(delete) == 0) return "Entry not found.";
        stmt.executeUpdate(update);

        conn.commit();
      }
      conn.setAutoCommit(true);

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    sb.append("Return Book:\n");
    sb.append("\tBook ").append(isbn).append(" returned for Customer ").append(customerid);

    return sb.toString();
  }
  //TODO
  public void closeDBConnection() {
    try {
      conn.close();
      System.out.println("Connection with Database has been terminated.");

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }


  /**
   * DO THE REST WHEN YOU HAVE CONFIRMED THE ABOVE CODE IS COMPLETE
   */
  //TODO
  public String deleteCus(int customerID) {
    String delete = "DELETE FROM customer" +
            "WHERE customerid = " + customerID;

    return "Deleted customer";
  }
  //TODO
  public String deleteAuthor(int authorID) {
    return "Delete Author";
  }
  //TODO
  public String deleteBook(int isbn) {
    return "Delete Book";
  }

  public void showDialog(){
    JOptionPane.showMessageDialog(LibraryUI.getFrames()[0],
            "Tuples have been locked, click OK to continue.",
            "Pausing", JOptionPane.PLAIN_MESSAGE);
  }
}


