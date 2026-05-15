package tribhuwansingh_2341019538;

import tribhuwansingh_2341019538.db.ConnectionManager;
import tribhuwansingh_2341019538.db.DatabaseSetup;
import tribhuwansingh_2341019538.model.Book;
import tribhuwansingh_2341019538.model.Loan;
import tribhuwansingh_2341019538.model.Member;
import tribhuwansingh_2341019538.service.LibraryService;
import tribhuwansingh_2341019538.service.PerformanceEvaluator;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class MainApp {
    private final Scanner scanner;
    private final LibraryService libraryService;
    private final PerformanceEvaluator performanceEvaluator;

    public MainApp(Scanner scanner) {
        this.scanner = scanner;
        this.libraryService = new LibraryService();
        this.performanceEvaluator = new PerformanceEvaluator();
    }

    public static void main(String[] args) {
        try {
            new DatabaseSetup().initializeDatabase();
            if (args.length > 0 && "--self-test".equals(args[0])) {
                runSelfTest();
                return;
            }
            new MainApp(new Scanner(System.in)).run();
        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
        } finally {
            ConnectionManager.shutdownDatabase();
        }
    }

    private static void runSelfTest() throws Exception {
        LibraryService service = new LibraryService();
        System.out.println(service.registerMember("Sample Member", "sample" + System.nanoTime() + "@example.com"));
        System.out.println(service.addBook("Sample Book", "Sample Author", "ISBN-" + System.nanoTime()));
        System.out.println(service.demonstrateRollback());
        new PerformanceEvaluator().runBenchmarks(Path.of("performance_report.csv"));
        System.out.println("Self-test completed and performance_report.csv generated.");
    }

    private void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose option: ");
            switch (choice) {
                case 1 -> registerMember();
                case 2 -> addBook();
                case 3 -> processLoan();
                case 4 -> returnBook();
                case 5 -> viewMembers();
                case 6 -> viewBooks();
                case 7 -> viewActiveLoans();
                case 8 -> viewOverdueBooks();
                case 9 -> runBenchmarks();
                case 10 -> running = false;
                default -> System.out.println("Invalid menu option.");
            }
        }
        System.out.println("Exited Succesfully.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("========== Library Loan Management System ==========");
        System.out.println("1. Register Member");
        System.out.println("2. Add Book");
        System.out.println("3. Process Loan");
        System.out.println("4. Return Book");
        System.out.println("5. View Members");
        System.out.println("6. View Books");
        System.out.println("7. View Active Loans");
        System.out.println("8. View Overdue Books");
        System.out.println("9. Run Benchmarks");
        System.out.println("10. Exit");
    }

    private void registerMember() {
        String name = readLine("Name: ");
        String email = readLine("Email: ");
        System.out.println(libraryService.registerMember(name, email));
    }

    private void addBook() {
        String title = readLine("Title: ");
        String author = readLine("Author: ");
        String isbn = readLine("ISBN: ");
        System.out.println(libraryService.addBook(title, author, isbn));
    }

    private void processLoan() {
        int memberId = readInt("Member ID: ");
        int bookId = readInt("Book ID: ");
        System.out.println(libraryService.processLoan(memberId, bookId));
    }

    private void returnBook() {
        int loanId = readInt("Loan ID: ");
        System.out.println(libraryService.returnBook(loanId));
    }

    private void viewMembers() {
        try {
            List<Member> members = libraryService.getMembers();
            System.out.printf("%-5s %-25s %-30s %-5s%n", "ID", "Name", "Email", "Loans");
            members.forEach(System.out::println);
        } catch (SQLException e) {
            System.out.println("Could not load members: " + e.getMessage());
        }
    }

    private void viewBooks() {
        try {
            List<Book> books = libraryService.getBooks();
            System.out.printf("%-5s %-30s %-20s %-18s %-10s%n", "ID", "Title", "Author", "ISBN", "Available");
            books.forEach(System.out::println);
        } catch (SQLException e) {
            System.out.println("Could not load books: " + e.getMessage());
        }
    }

    private void viewActiveLoans() {
        try {
            printLoans(libraryService.getActiveLoans());
        } catch (SQLException e) {
            System.out.println("Could not load active loans: " + e.getMessage());
        }
    }

    private void viewOverdueBooks() {
        try {
            printLoans(libraryService.getOverdueLoans());
        } catch (SQLException e) {
            System.out.println("Could not load overdue loans: " + e.getMessage());
        }
    }

    private void printLoans(List<Loan> loans) {
        System.out.printf("%-5s %-5s %-22s %-5s %-30s %-12s %-12s%n",
                "ID", "Mem", "Member", "Book", "Title", "Loan Date", "Returned");
        loans.forEach(System.out::println);
    }

    private void runBenchmarks() {
        try {
            performanceEvaluator.runBenchmarks(Path.of("performance_report.csv"));
            System.out.println("Benchmarks completed. Report: performance_report.csv");
        } catch (Exception e) {
            System.out.println("Benchmark failed: " + e.getMessage());
        }
    }

    private int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                int parsed = Integer.parseInt(value);
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Enter a positive whole number.");
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
