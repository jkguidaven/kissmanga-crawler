import java.util.ArrayList;


public class Manga {
	private String name;
	private String link;
	private String status;
	
	private ArrayList<Chapter> chapters;
	
	
	public Manga(String name,String link,String status){
		this.name = name;
		if(link.charAt(0) == '/') link = link.substring(1);
		this.link = link;
		this.status = status;
		this.chapters = new ArrayList<Chapter>();
	}
	
	public String getName() { return name; }
	public String getLink() { return link; }

	public String getStatus() { return status; }
	
	public ArrayList<Chapter> getChapters() { return chapters; }
	public void setChapters(ArrayList<Chapter> chapters){
		this.chapters = chapters;
	}
	
	public String toString(){
		StringBuffer buff = new StringBuffer();
		
		buff.append("Manga: " + name + " - " + link + " - " + status);
		
		for(Chapter chapter : chapters)
			buff.append( "\n\t - " + chapter);
		
		return buff.toString();
	}
}
