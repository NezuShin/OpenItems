package su.nezushin.openitems.rp.font;
/**
 * Used for resource pack json serialization
 */
public class FontImage {

    private int height, ascent;

    private String file, type = "bitmap";

    private String[] chars = new String[1];

    public FontImage(int height, int ascent, String file) {
        this.height = height;
        this.ascent = ascent;
        this.file = file;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getAscent() {
        return ascent;
    }

    public void setAscent(int ascent) {
        this.ascent = ascent;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getSymbol() {
        return chars[0];
    }

    public void setSymbol(String symbol) {
        this.chars[0] = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
