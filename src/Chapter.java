
public class Chapter {

	private String link;
	private String name;
	
	public Chapter(String name,String link){
		if(link.charAt(0) == '/') link = link.substring(1);
		this.link = link;
		this.name = name;
	}
	
	public String getName() { return name; }
	public String getLink() { return link; }
	
	public String toString() {
		return name + " - " + link;
	}
}
