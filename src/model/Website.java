package model;

public class Website {

    private int id;
    private String name;
    private String url;

    // état temporaire (UI)
    private boolean selected;

    public Website(int id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.selected = false; // par défaut
    }

    // getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public boolean isSelected() { return selected; }

    // setters
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
