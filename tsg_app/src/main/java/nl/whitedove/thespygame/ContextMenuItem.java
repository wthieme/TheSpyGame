package nl.whitedove.thespygame;

 class ContextMenuItem {

    private String text;

     ContextMenuItem(String text) {
        super();
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}