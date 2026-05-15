package tribhuwansingh_2341019538.model;

import java.sql.Date;

public class Loan {
    private int loanId;
    private int memberId;
    private int bookId;
    private Date loanDate;
    private Date returnDate;
    private String memberName;
    private String bookTitle;

    public Loan(int loanId, int memberId, int bookId, Date loanDate, Date returnDate) {
        this(loanId, memberId, bookId, loanDate, returnDate, null, null);
    }

    public Loan(int loanId, int memberId, int bookId, Date loanDate, Date returnDate, String memberName, String bookTitle) {
        this.loanId = loanId;
        this.memberId = memberId;
        this.bookId = bookId;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
        this.memberName = memberName;
        this.bookTitle = bookTitle;
    }

    public int getLoanId() {
        return loanId;
    }

    public int getMemberId() {
        return memberId;
    }

    public int getBookId() {
        return bookId;
    }

    public Date getLoanDate() {
        return loanDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    @Override
    public String toString() {
        String name = memberName == null ? "" : memberName;
        String title = bookTitle == null ? "" : bookTitle;
        String returned = returnDate == null ? "ACTIVE" : returnDate.toString();
        return String.format("%-5d %-5d %-22s %-5d %-30s %-12s %-12s",
                loanId, memberId, name, bookId, title, loanDate, returned);
    }
}
