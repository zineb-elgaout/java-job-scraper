package model;

public class JobOffer {
    private int id;
    private String title;
    private String company;
    private String location;
    private String description;
    private String link;
    private String source;
    private String dateAdded;
    
    // Constructeur
    public JobOffer(String title, String company, String location, String description, String link, String source) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.link = link;
        this.source = source;
    }
    
    // Constructeur avec ID (pour DB)
    public JobOffer(int id, String title, String company, String location, String description, String link, String source, String dateAdded) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.link = link;
        this.source = source;
        this.dateAdded = dateAdded;
    }
    
    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getLink() { return link; }
    public String getSource() { return source; }
    public String getDateAdded() { return dateAdded; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCompany(String company) { this.company = company; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setLink(String link) { this.link = link; }
    public void setSource(String source) { this.source = source; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }
    
    @Override
    public String toString() {
        return title + " - " + company + " (" + source + ")";
    }
}