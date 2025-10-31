package se331.backend.entity;

public class VoteSummaryDTO {
    private long real;
    private long fake;

    public VoteSummaryDTO(long real, long fake) {
        this.real = real;
        this.fake = fake;
    }
    public long getReal() { return real; }
    public void setReal(long real) { this.real = real; }
    public long getFake() { return fake; }
    public void setFake(long fake) { this.fake = fake; }
}