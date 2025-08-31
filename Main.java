import java.time.LocalDate;
import java.util.*;

// ---------- MODEL ----------
class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private int copies;

    public Book(int id, String title, String author, String isbn, int copies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.copies = copies;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public int getCopies() { return copies; }

    public void setCopies(int copies) { this.copies = copies; }

    @Override
    public String toString() {
        return String.format("ID:%d | %s by %s | ISBN:%s | Copies:%d", 
                             id, title, author, isbn, copies);
    }
}

class User {
    private int id;
    private String username;
    private String role;

    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }

    @Override
    public String toString() {
        return String.format("ID:%d | %s (%s)", id, username, role);
    }
}

class Transaction {
    private int id;
    private int userId;
    private int bookId;
    private LocalDate borrowDate;
    private LocalDate returnDate;

    public Transaction(int id, int userId, int bookId, LocalDate borrowDate) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getBookId() { return bookId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getReturnDate() { return returnDate; }

    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    @Override
    public String toString() {
        return String.format("TxID:%d | User:%d | Book:%d | Borrowed:%s | Returned:%s",
                id, userId, bookId, borrowDate, 
                (returnDate != null ? returnDate : "Not returned yet"));
    }
}

// ---------- DAO ----------
class BookDAO {
    private Map<Integer, Book> books = new HashMap<>();
    private int idCounter = 1;

    public Book addBook(String title, String author, String isbn, int copies) {
        Book book = new Book(idCounter++, title, author, isbn, copies);
        books.put(book.getId(), book);
        return book;
    }

    public List<Book> getAllBooks() { return new ArrayList<>(books.values()); }
    public Book getBookById(int id) { return books.get(id); }

    public List<Book> searchBooks(String keyword) {
        List<Book> result = new ArrayList<>();
        for (Book b : books.values()) {
            if (b.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                b.getAuthor().toLowerCase().contains(keyword.toLowerCase()) ||
                b.getIsbn().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(b);
            }
        }
        return result;
    }
}

class UserDAO {
    private Map<Integer, User> users = new HashMap<>();
    private int idCounter = 1;

    public User addUser(String username, String role) {
        User user = new User(idCounter++, username, role);
        users.put(user.getId(), user);
        return user;
    }

    public User getUserById(int id) { return users.get(id); }
    public List<User> getAllUsers() { return new ArrayList<>(users.values()); }
}

class TransactionDAO {
    private List<Transaction> transactions = new ArrayList<>();
    private int idCounter = 1;

    public Transaction addTransaction(int userId, int bookId) {
        Transaction tx = new Transaction(idCounter++, userId, bookId, LocalDate.now());
        transactions.add(tx);
        return tx;
    }

    public void returnTransaction(int txId) {
        for (Transaction tx : transactions) {
            if (tx.getId() == txId && tx.getReturnDate() == null) {
                tx.setReturnDate(LocalDate.now());
                break;
            }
        }
    }

    public List<Transaction> getAllTransactions() { return transactions; }
}

// ---------- SERVICE ----------
class LibraryService {
    private BookDAO bookDAO;
    private UserDAO userDAO;
    private TransactionDAO transactionDAO;

    public LibraryService(BookDAO bookDAO, UserDAO userDAO, TransactionDAO transactionDAO) {
        this.bookDAO = bookDAO;
        this.userDAO = userDAO;
        this.transactionDAO = transactionDAO;
    }

    public Book addBook(String title, String author, String isbn, int copies) {
        return bookDAO.addBook(title, author, isbn, copies);
    }

    public List<Book> getAllBooks() { return bookDAO.getAllBooks(); }
    public List<Book> searchBooks(String keyword) { return bookDAO.searchBooks(keyword); }

    public boolean borrowBook(int userId, int bookId) {
        Book book = bookDAO.getBookById(bookId);
        if (book != null && book.getCopies() > 0 && userDAO.getUserById(userId) != null) {
            book.setCopies(book.getCopies() - 1);
            transactionDAO.addTransaction(userId, bookId);
            return true;
        }
        return false;
    }

    public boolean returnBook(int bookId, int txId) {
        Book book = bookDAO.getBookById(bookId);
        if (book != null) {
            book.setCopies(book.getCopies() + 1);
            transactionDAO.returnTransaction(txId);
            return true;
        }
        return false;
    }

    public User addUser(String username, String role) {
        return userDAO.addUser(username, role);
    }

    public List<User> getAllUsers() { return userDAO.getAllUsers(); }
    public List<Transaction> getAllTransactions() { return transactionDAO.getAllTransactions(); }
}

// ---------- UI ----------
class LibraryMenu {
    private LibraryService service;
    private Scanner scanner = new Scanner(System.in);

    public LibraryMenu(LibraryService service) {
        this.service = service;
    }

    public void start() {
        System.out.println("=== Library Management System ===");
        while (true) {
            System.out.println("\n1. Add Book");
            System.out.println("2. List Books");
            System.out.println("3. Search Books");
            System.out.println("4. Borrow Book");
            System.out.println("5. Return Book");
            System.out.println("6. Add User");
            System.out.println("7. List Users");
            System.out.println("8. List Transactions");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> addBook();
                case 2 -> listBooks();
                case 3 -> searchBooks();
                case 4 -> borrowBook();
                case 5 -> returnBook();
                case 6 -> addUser();
                case 7 -> listUsers();
                case 8 -> listTransactions();
                case 0 -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void addBook() {
        System.out.print("Title: ");
        String title = scanner.nextLine();
        System.out.print("Author: ");
        String author = scanner.nextLine();
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine();
        System.out.print("Copies: ");
        int copies = Integer.parseInt(scanner.nextLine());
        System.out.println("Added: " + service.addBook(title, author, isbn, copies));
    }

    private void listBooks() {
        var books = service.getAllBooks();
        if (books.isEmpty()) System.out.println("No books available.");
        else books.forEach(System.out::println);
    }

    private void searchBooks() {
        System.out.print("Enter keyword: ");
        var results = service.searchBooks(scanner.nextLine());
        if (results.isEmpty()) System.out.println("No books found.");
        else results.forEach(System.out::println);
    }

    private void borrowBook() {
        System.out.print("Enter user ID: ");
        int userId = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter book ID to borrow: ");
        int bookId = Integer.parseInt(scanner.nextLine());
        if (service.borrowBook(userId, bookId))
            System.out.println("Book borrowed successfully.");
        else
            System.out.println("Borrow failed (invalid user/book or no copies).");
    }

    private void returnBook() {
        System.out.print("Enter book ID to return: ");
        int bookId = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter transaction ID: ");
        int txId = Integer.parseInt(scanner.nextLine());
        if (service.returnBook(bookId, txId))
            System.out.println("Book returned successfully.");
        else
            System.out.println("Invalid return.");
    }

    private void addUser() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Role (ADMIN/USER): ");
        String role = scanner.nextLine();
        System.out.println("Added: " + service.addUser(username, role));
    }

    private void listUsers() {
        service.getAllUsers().forEach(System.out::println);
    }

    private void listTransactions() {
        var txs = service.getAllTransactions();
        if (txs.isEmpty()) System.out.println("No transactions yet.");
        else txs.forEach(System.out::println);
    }
}

// ---------- MAIN ----------
public class Main {
    public static void main(String[] args) {
        BookDAO bookDAO = new BookDAO();
        UserDAO userDAO = new UserDAO();
        TransactionDAO transactionDAO = new TransactionDAO();
        LibraryService service = new LibraryService(bookDAO, userDAO, transactionDAO);

        // preload some data
        service.addBook("Java Programming", "James Gosling", "12345", 3);
        service.addBook("Data Structures", "Mark Allen", "67890", 2);
        service.addBook("Operating Systems", "Silberschatz", "11223", 4);

        service.addUser("Alice", "USER");
        service.addUser("Bob", "ADMIN");

        new LibraryMenu(service).start();
    }
}
