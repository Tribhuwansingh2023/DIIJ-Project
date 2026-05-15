package tribhuwansingh_2341019538.model;

public class Member {
    private int memberId;
    private String name;
    private String email;
    private int activeLoans;

    public Member(int memberId, String name, String email, int activeLoans) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.activeLoans = activeLoans;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getActiveLoans() {
        return activeLoans;
    }

    public void setActiveLoans(int activeLoans) {
        this.activeLoans = activeLoans;
    }

    @Override
    public String toString() {
        return String.format("%-5d %-25s %-30s %-5d", memberId, name, email, activeLoans);
    }
}
