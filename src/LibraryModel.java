import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;


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

    } catch (SQLException | ClassNotFoundException throwables) {
      throwables.printStackTrace();
    }
  }

  public String bookLookup(int isbn) {

    String lookup =
    "SELECT b.ISBN, b.Title, a.Surname, b.Edition_No, b.NumOfCop, b.NumLeft, ba.AuthorSeqNo " +
    "FROM Book AS b "+
    "LEFT JOIN Book_Author AS ba ON b.ISBN = ba.ISBN " +
    "LEFT JOIN Author AS a ON a.AuthorID = ba.AuthorID " +
    "WHERE b.ISBN = " + isbn +
    "ORDER BY ba.AuthorSeqNo;";

    StringBuilder sb = new StringBuilder();
    ArrayList<String> authors = new ArrayList<>();

    //stores attributes for appending later
    String title = "";
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

      rs.close();

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    sb.append("Book Lookup:\n");
    if(count == 0) {
      sb.append("\tNo such ISBN: '").append(isbn).append("'");
      return sb.toString();
    }
    sb.append("\t").append(isbn).append(": ").append(title);
    sb.append("\n\tEdition: ").append(edition);
    sb.append(" - Number of Copies: ").append(copies_total).append(" - Copies Left: ").append(copies_remaining).append("\n");
    if(authors.isEmpty()) sb.append("\t(no authors)");
    else {
      //checks to see if there are multiple authors and adjust text accordingly
      sb.append((authors.size() > 1) ? "\tAuthors: " : "\tAuthor: ");
      //prints out each author
      for (int i = 0; i < authors.size(); i++) {
        sb.append(authors.get(i));
        if (i != authors.size() - 1) sb.append(", ");
      }
    }
    return sb.toString();
  }

  public String showCatalogue() {

    String lookup =
    "SELECT b.ISBN, b.Title, string_agg(a.Surname, ', ') AS Surname, b.Edition_No, b.NumOfCop, b.NumLeft " +
    "FROM Book AS b "+
    "LEFT JOIN Book_Author AS ba ON b.ISBN = ba.ISBN " +
    "LEFT JOIN Author AS a ON a.AuthorID = ba.AuthorID " +
    "GROUP BY b.ISBN, b.Title, b.Edition_No, b.NumOfCop, b.NumLeft " +
    "ORDER BY b.isbn;";

    StringBuilder sb = new StringBuilder();

    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);
      sb.append("Show Catalogue:\n");
      //everything is in the while loop so the printout is built for each tuple
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


        sb.append("\t").append(isbn).append(": ").append(title);
        sb.append("\n\t\tEdition: ").append(edition);
        sb.append(" - Number of Copies: ").append(copies_total).append(" - Copies Left: ").append(copies_remaining).append("\n");
        if(author.equals("")) sb.append("\t\t(no authors)");
        //checks to see if there are multiple authors and adjusts the text accordingly
        else sb.append((author.contains(",")) ? "\t\tAuthors: " : "\t\tAuthor: ");
        sb.append(author).append("\n");
      }

      rs.close();

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return sb.toString();
  }

  public String showLoanedBooks() {

    String lookup =
    "SELECT b.ISBN, b.Title, string_agg(DISTINCT a.Surname, ', ') AS Surname, b.Edition_No, b.NumOfCop, b.NumLeft " +
    "FROM Book AS b, cust_book AS cb " +
    "NATURAL JOIN Book_Author AS ba " +
    "NATURAL JOIN Author as a " +
    "WHERE b.ISBN = cb.Isbn " +
    "GROUP BY b.ISBN, b.Title, b.Edition_No, b.NumOfCop, b.NumLeft " +
    "ORDER BY b.isbn;";

    String cust_lookup =
    "SELECT cb.ISBN, c.Customerid, c.f_name, c.l_name, c.city " +
    "FROM Cust_Book AS cb " +
    "LEFT JOIN Customer AS c ON cb.CustomerID = c.CustomerID " +
    "GROUP BY cb.ISBN, c.Customerid, c.city " +
    "ORDER BY cb.isbn;";

    //stores the customers and isbns at a 1-1 ratio
    ArrayList<String> customers = new ArrayList<>();
    ArrayList<Integer> isbns = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    sb.append("Show Loaned Books:\n");
    int isbn;

    try {
      Statement stmt = conn.createStatement();

      ResultSet rs = stmt.executeQuery(cust_lookup);
      StringBuilder sb1 = new StringBuilder();
      int count = 0;
      //as per above, stores at 1-1 ratio
      while(rs.next()){
        sb1.append("\t\t\t").append(rs.getInt("CustomerID")).append(": ").append(rs.getString("l_name").trim()).append(", ").append(rs.getString("f_name").trim()).append(" - ").append(rs.getString("City").trim()).append("\n");
        customers.add(sb1.toString());
        isbns.add(rs.getInt("isbn"));
        sb1 = new StringBuilder();
        count++;
      }
      System.out.println(count);

      ResultSet rs1 = stmt.executeQuery(lookup);
      while(rs1.next()){
        isbn = rs1.getInt("ISBN");
        sb.append(isbn).append(": ").append(rs1.getString("Title")).append("\n");
        sb.append("\t");
        sb.append("Edition: ").append(rs1.getInt("Edition_No"));
        sb.append(" - Number of copies: ").append(rs1.getInt("NumOfCop")).append(" - Copies left: ").append(rs1.getInt("NumLeft")).append("\n");
        sb.append("\t");
        String author = rs1.getString("Surname");
        if(author.contains(",")) sb.append("Authors: ").append(author);
        else sb.append("Author: ").append(author);
        sb.append("\n\t\t");

        if (count > 1) sb.append("Borrowers: \n");
        else sb.append("Borrower: \n");

        //prints out the customers that have a 'matching' isbn (as per the two arraylists)
        for (int i = 0; i < count; i++){
          if(isbns.get(i) == isbn){
            sb.append(customers.get(i));
          }
        }

      }
      sb.append("\n");

      rs.close();
      rs1.close();

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    //overrides everything above if there are no loaned books and displays
    if (isbns.size() == 0){
      sb = new StringBuilder();
      sb.append("Show Loaned Books:\n(No loaned books).");
    }

    return sb.toString();
  }

  public String showAuthor(int authorID) {
    String lookup =
    "SELECT a.AuthorID, a.Surname, a.Name, b.ISBN, b.Title " +
    "FROM Author AS a " +
    "LEFT JOIN Book_Author AS ba ON a.AuthorID = ba.AuthorID " +
    "LEFT JOIN Book AS b ON ba.ISBN = b.ISBN " +
    "WHERE a.AuthorID = " + authorID + ";";

    //stores the books the author has written
    ArrayList<String> books = new ArrayList<>();

    StringBuilder sb = new StringBuilder();
    String name = null;
    String surname = null;
    String title = null;
    int isbn = 0;
    int author_id = 0;
    int count = 0;

    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);

      while (rs.next()){
        author_id = rs.getInt("AuthorID");
        //'if' checks to ensure the string is not null - only nullable attributes need to be checked as the constraints ensure not null values
        surname = rs.getString("Surname").trim();
        if (rs.getString("Name") != null) name = rs.getString("Name").trim();
        isbn = rs.getInt("ISBN");
       title = rs.getString("Title").trim();
        if(title != null) books.add(isbn + " - " + title);
        count++;
      }

      sb.append("Show Author:\n");
      sb.append("\t");
      if(count == 0) sb.append("No such Author ID: ").append(authorID);
      else {
        sb.append(author_id).append(" - ");
        sb.append(name).append(" ").append(surname).append("\n");
        if (books.isEmpty()) {
          sb.append("\t").append("(no books written)");
        }else{
          sb.append("\tBooks Written:\n");
          //displays all books the author has written
          for (int i = 0; i < books.size(); i++) {
            sb.append("\t\t").append(books.get(i));
            if(i < books.size() -1) sb.append("\n");
          }
        }
      }

      rs.close();

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return sb.toString();
  }

  public String showAllAuthors() {

    String lookup = "SELECT * FROM Author ORDER BY AuthorID;";

    StringBuilder sb = new StringBuilder();
    sb.append("Show All Authors:\n");

    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);

      while (rs.next()){
        sb.append("\t");
        sb.append(rs.getInt("AuthorID")).append(": ");
        sb.append(rs.getString("Surname").trim()).append(", ");
        //check to see if nullable attribute is valid
        if(rs.getString("Name") != null) sb.append(rs.getString("Name").trim()).append("\n");
      }

      rs.close();

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return sb.toString();
  }

  public String showCustomer(int customerID) {

    String lookup =
    "SELECT DISTINCT c.CustomerID, c.l_name, c.f_name, c.City, b.isbn, b.title " +
    "FROM customer AS c, (SELECT customerid " +
    "FROM cust_book AS cb " +
    "GROUP BY customerid) AS cd, cust_book AS cb " +
    "LEFT JOIN book AS b ON cb.isbn = b.isbn " +
    "WHERE cb.customerid = c.customerid AND c.CustomerID = " + customerID + ";";

    String f_name = "(no first name)";
    String l_name = "";
    String city = "(no city)";

    HashSet<String> books = new HashSet<>();

    StringBuilder sb = new StringBuilder();
    sb.append("Show Customer: \n");
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);
      while (rs.next()){
        //these values are just overriding the previous ones
        l_name = rs.getString("l_name").trim(); //no need to check if null as constraint is 'not null'
        if(rs.getString("f_name") != null) f_name = rs.getString("f_name").trim();
        if(rs.getString("city") != null) city = rs.getString("city").trim();
        if(rs.getString("title") != null) {
          //adds all books for the same customer
          books.add(rs.getInt("ISBN") + ": " + rs.getString("title"));
        }
      }

      sb.append("\t").append(customerID).append(": ").append(l_name).append(", ").append(f_name).append(" - ").append(city).append("\n");

      if(books.size() > 0){
        sb.append("\tBorrowed Books:");
        for(String b : books){
          sb.append("\t\t").append(b).append("\n");
        }
      }else {
        sb.append("\t(No borrowed books).\n");
      }

      rs.close();

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return sb.toString();
  }

  public String showAllCustomers() {
    String lookup = "SELECT * FROM Customer ORDER BY CustomerID;";

    StringBuilder sb = new StringBuilder();
    sb.append("Show All Customers:\n");

    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(lookup);

      while (rs.next()){
        //both f_name and city have no not null constraint - is checked
        sb.append("\t");
        sb.append(rs.getInt("CustomerID")).append(": ");
        sb.append(rs.getString("l_name").trim()).append(", ");
        if(rs.getString("f_name") != null) sb.append(rs.getString("f_name").trim()).append(" - ");
        if(rs.getString("city") != null) sb.append(rs.getString("city").trim()).append("\n");
        else sb.append("(no city)").append("\n");
      }

      rs.close();

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return sb.toString();
  }

  public String borrowBook(int isbn, int customerID, int day, int month, int year) {

    StringBuilder sb = new StringBuilder();

    String bookQuery = "Select * FROM book WHERE ISBN = " + isbn + " FOR  UPDATE;";
    String custQuery = "Select * FROM customer WHERE CustomerID = " + customerID + " FOR UPDATE;";
    String cust_bookQuery = "Select * FROM cust_book WHERE CustomerID = " + customerID + " AND ISBN = " + isbn;

    String insert = "INSERT INTO cust_book(ISBN, DueDate, CustomerID) VALUES (?, ?, ?);";
    String update = "UPDATE book SET Numleft = Numleft - 1 WHERE ISBN = " + isbn + ";";

    String bookTitle = "";
    String custName = "";
    int books_left = 0;
    int count = 0;
    int commit_update = -1;
    int commit_insert = -1;

    try {
      conn.setAutoCommit(false);
      PreparedStatement pInsert = conn.prepareStatement(insert);

      pInsert.setInt(1, isbn);
      pInsert.setDate(2, java.sql.Date.valueOf(year + "-" + month + "-" + day));
      pInsert.setInt(3, customerID);

      Statement stmt = conn.createStatement();
      Savepoint savepoint = conn.setSavepoint();

      ResultSet rs1 = stmt.executeQuery(bookQuery);
      while(rs1.next()) {
        if(rs1.getString("title") != null){
          bookTitle = rs1.getString("title").trim();
        }
        books_left = rs1.getInt("numLeft");
      }
      ResultSet rs2 = stmt.executeQuery(custQuery);
      while(rs2.next()) {
        if(rs2.getString("f_name") != null && rs2.getString("l_name") != null) {
          custName = rs2.getString("f_name").trim() + " " + rs2.getString("l_name").trim();
        }
      }
      ResultSet rs3 = stmt.executeQuery(cust_bookQuery);
      while(rs3.next()) {
        count++;
      }

      if (count > 0) {
        sb.append("Customer ").append(customerID).append(" already has book '").append(isbn).append("' on loan.");
      }else if(bookTitle.equals("")){
        sb.append("ISBN ").append(isbn).append(" does not exist.");
      }else if(custName.equals("")){
        sb.append("Customer ID ").append(customerID).append(" does not exist.");
      }else if(books_left == 0) {
        sb.append("There are no remaining copies of ").append(bookTitle).append(".");
      }else{
        //all checks are done. If the query is valid, will show dialog and pause
        showDialog();
        commit_update = stmt.executeUpdate(update);
        commit_insert = pInsert.executeUpdate();
        conn.commit();
      }

      //checks the status of the update queries. If they don't succeed, will roll back - if they do, then print appropriate message
      if (commit_update == 0 && commit_insert == 0) {
        sb.append("Error encountered borrowing book. Rolling back.");
        conn.rollback(savepoint);
      }else if(commit_update == 1 && commit_insert == 1){
        sb.append("Borrow Book\n");
        sb.append("\tBook: ").append(isbn).append(" (").append(bookTitle).append(")\n");
        sb.append("\tLoaned to: ").append(customerID).append(" (").append(custName).append(")\n");
        sb.append("\tDue Date: ").append(day).append("/").append(month).append("/").append(year);
      }

      rs1.close();
      rs2.close();
      rs3.close();

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return sb.toString();
  }

  public String returnBook(int isbn, int customerid) {
    String checkCustomer = "SELECT * FROM cust_book WHERE ISBN = " + isbn + " AND CustomerID = " + customerid + " FOR UPDATE;";
    String checkBook = "SELECT * FROM book WHERE ISBN = " + isbn + " FOR UPDATE;";

    String delete = "DELETE FROM cust_book WHERE ISBN = " + isbn + " AND  CustomerID = " + customerid + ";";
    String update = "UPDATE book SET Numleft = Numleft + 1 WHERE ISBN = " + isbn + ";";

    StringBuilder sb = new StringBuilder();
    int cust_id = 0;
    int cust_isbn = 0;
    int book_isbn = 0;
    int commit_update = -1;
    int commit_delete = -1;

    try {
      conn.setAutoCommit(false);
      Statement stmt = conn.createStatement();
      Savepoint savepoint = conn.setSavepoint();
      ResultSet rs = stmt.executeQuery(checkCustomer);
      while(rs.next()){
        cust_id = rs.getInt("customerid");
        cust_isbn = rs.getInt("isbn");
      }
      rs = stmt.executeQuery(checkBook);
      while(rs.next()){
        book_isbn = rs.getInt("isbn");
      }

      if (cust_id == 0) {
        sb.append("Customer ID '").append(customerid).append("' could not be found matching ISBN '").append(isbn).append("'.");
      }else if(cust_isbn == 0 || book_isbn == 0){
        sb.append("ISBN '").append(isbn).append("' could not be found.");
      }else{
        //all checks are done. If the query is valid, will show dialog and pause
        showDialog();
        commit_update = stmt.executeUpdate(update);
        commit_delete = stmt.executeUpdate(delete);
        conn.commit();
      }

      //checks the status of the update queries. If they don't succeed, will roll back - if they do, then print appropriate message
      if (commit_update == 0 || commit_delete == 0){
        sb.append("Error encountered when returning book. Rolling back.");
        conn.rollback(savepoint);
      }else if (commit_update == 1 || commit_delete == 1){
        sb.append("Return Book:\n\tBook ").append(isbn).append(" returned for Customer ").append(customerid);
      }

      rs.close();

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return sb.toString();
  }

  public void closeDBConnection() {
    try {
      conn.close();
      System.out.println("Connection with Database has been terminated.");

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  public String deleteCus(int customerID) {
    String query = "SELECT * FROM customer WHERE CustomerID = " + customerID + " FOR UPDATE;";
    String borrowed = "SELECT * FROM cust_book WHERE CustomerID = " + customerID + ";";
    String delete = "DELETE FROM customer WHERE CustomerID = " + customerID +";";


    StringBuilder sb = new StringBuilder();
    int borrow_count = 0;
    int cust_count = 0;
    String l_name = "";
    String f_name = "";
    int update = -1;

    try {
      conn.setAutoCommit(false);
      Statement stmt = conn.createStatement();
      Savepoint savepoint = conn.setSavepoint();
      ResultSet rs_borrowed = stmt.executeQuery(borrowed);

      while(rs_borrowed.next()){
        borrow_count++;
      }

      ResultSet rs = stmt.executeQuery(query);


      while ( rs.next()){
        l_name = rs.getString("l_name").trim();
        f_name = rs.getString("f_name").trim();
        cust_count++;
      }

      if(borrow_count > 0){
        if(borrow_count > 1){
          sb.append("Customer '").append(customerID).append("' currently has ").append(borrow_count).append(" loaned books. Please ensure they are returned before deleting.");
        }
        else{
          sb.append("Customer '").append(customerID).append("' currently has ").append(borrow_count).append(" loaned book. Please ensure it is returned before deleting.");
        }
      }else if(cust_count == 0){
        sb.append("Customer ID '").append(customerID).append("' not found.");
      }else {
        //all checks are done. If the query is valid, will show dialog and pause
        showDialog();
        update = stmt.executeUpdate(delete);
        conn.commit();
      }

      //checks the status of the update queries. If they don't succeed, will roll back - if they do, then print appropriate message
      if(update == 0){
        sb.append("Error encountered when deleting customer. Rolling back.");
        conn.rollback(savepoint);
      }else if(update == 1){
        sb.append("Deleted Customer:\nCustomer ID: ").append(customerID).append(" - ").append(l_name).append(", ").append(f_name);
      }

      rs.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return sb.toString();
  }

  public String deleteAuthor(int authorID) {
    String query = "SELECT * FROM Author WHERE AuthorID = " + authorID + " FOR UPDATE;";
    String delete = "DELETE FROM Author WHERE AuthorID = " + authorID + ";";

    StringBuilder sb = new StringBuilder();
    int update = -1;
    int author_count = 0;
    String surname = "";
    String name = "";

    try {
      conn.setAutoCommit(false);
      Statement stmt = conn.createStatement();
      Savepoint savepoint = conn.setSavepoint();
      ResultSet rs = stmt.executeQuery(query);


      while(rs.next()){
        if(rs.getString("name") != null) name = rs.getString("name").trim();
        if(rs.getString("surname") != null) surname = rs.getString("surname").trim();
        author_count++;
      }

      if(author_count == 0){
        sb.append("Could not find Author with ID '").append(authorID).append("'.");
      }else {
        //all checks are done. If the query is valid, will show dialog and pause
        showDialog();
        update = stmt.executeUpdate(delete);
        conn.commit();
      }

      //checks the status of the update queries. If they don't succeed, will roll back - if they do, then print appropriate message
      if(update == 0) {
        sb.append("Error encountered when deleting author: Rolling back.");
        conn.rollback(savepoint);
      }else if(update == 1){
        sb.append("Delete Author:\nAuthor ID: ").append(authorID).append(" - ").append(surname).append(", ").append(name);
      }

      rs.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return sb.toString();
  }

  public String deleteBook(int isbn) {
    String query = "SELECT * FROM book WHERE isbn = " + isbn + " FOR UPDATE;";
    String delete = "DELETE FROM book WHERE isbn = " + isbn +";";

    StringBuilder sb = new StringBuilder();
    int book_count = 0;
    int count = 0;
    int update = -1;
    String title = "";

    try {
      conn.setAutoCommit(false);
      Statement stmt = conn.createStatement();
      Savepoint savePoint = conn.setSavepoint();
      ResultSet rs = stmt.executeQuery(query);


      while ( rs.next()){
        title = rs.getString("Title").trim();
        book_count = rs.getInt("NumOfCop") - rs.getInt("numleft");
        count++;
      }

      if(book_count > 0){
        if(book_count > 1){
          sb.append("Book '").append(isbn).append("' currently has ").append(book_count).append(" books loaned out. Please ensure they are returned before deleting.");
        }else{
          sb.append("Book '").append(isbn).append("' currently has ").append(book_count).append(" book loaned out. Please ensure it is returned before deleting.");
        }
      }else if(count == 0){
        sb.append("ISBN '").append(isbn).append("' not found.");
      }else {
        //all checks are done. If the query is valid, will show dialog and pause
        showDialog();
        update = stmt.executeUpdate(delete);
        conn.commit();
      }

      //checks the status of the update queries. If they don't succeed, will roll back - if they do, then print appropriate message
      if (update == 0) {
        sb.append("Error Encountered when deleting book: Rolling back");
        conn.rollback(savePoint);
      }else if(update == 1){
        sb.append("Delete Book:\nISBN: ").append(isbn).append(" - ").append(title);
      }

      rs.close();

    } catch (SQLException e) {
      e.printStackTrace();

    }

    return sb.toString();
  }

  public void showDialog(){
    JOptionPane.showMessageDialog(LibraryUI.getFrames()[0],
            "Tuple(s) have been locked and ready to update. Click OK to continue.",
            "PAUSING", JOptionPane.PLAIN_MESSAGE);
  }
}


