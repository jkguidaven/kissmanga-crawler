import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import java.util.StringTokenizer;


public class KissMangaConnector {
	
		private static String KissMangaAddress = "kissmanga.com";
		private static int	 KissMangaWebPort = 80;		
		public static final String CRLF ="\r\n";
		
		private Socket socKissManga;
		private ArrayList<Manga> mangalist = null;
		
		KissMangaCrawlerUI ui = null;
		public KissMangaConnector(KissMangaCrawlerUI ui){
			this.ui = ui;
		}
		
		public void connect() { connect(false); }
		
		public void connect(boolean reconnect){
			if(reconnect)
				disconnect();
			
			try
			{
				if(!reconnect) ui.Trace("connecting to KissManga[" + 
								   KissMangaAddress + ":" + 
								   KissMangaWebPort + "]....");
				
				socKissManga = new Socket(KissMangaAddress,KissMangaWebPort);
				if(!reconnect) ui.Trace("Successful!!");

			}catch(UnknownHostException uhe){
				ui.Trace("KissManga address[" + KissMangaAddress + "] could not be reach!");
			}catch(IOException ioe){
				
			}
		}
		
		public void connect(String address){

				disconnect();
			
			try
			{
				
				socKissManga = new Socket(address,KissMangaWebPort);

			}catch(UnknownHostException uhe){
			}catch(IOException ioe){
				
			}
		}
		
		public void disconnect() {
			if(socKissManga != null){
				try {
					socKissManga.close();
				} catch (IOException e) {}
				socKissManga = null;
			}
		}
		
		public Socket getSocket(){ return socKissManga; }
		
		
		
		private String getMangaListPage(int page){
			return "MangaList?page=" + page;
		}
		
		public void syncMangaList(){
			int page = 135;
			int mangacount = 0;
			ui.clearMangaList();
			ui.Trace("resyncing manga list database..");
			ui.Trace("Recreating Manga local database...");
			
			mangalist = new ArrayList<Manga>();
			
			ui.Trace("Scanning MangaList on host[" + KissMangaAddress + "...");
			
			ui.setToSynchingMangaListSate(true);
			while(true){
				connect(true);
				ui.Trace("Scanning MangaList page " + page + " list...");
				
				try{
					String content = getRespone(getHeaderRequest(getMangaListPage(page)));
					int mangascanned=parseMangaList(content);
					if( mangascanned == 0)
						break;
					else
						mangacount+= mangascanned;
						
				}catch(Exception e){ e.printStackTrace(); }
				page++;
			}
			
			ui.Trace("Done scanning MangaList page.. ");
			ui.Trace("Total Manga = " + mangacount + ".. ");
			ui.setToSynchingMangaListSate(false);
		}
		
		
		public static String getHeaderRequest(String request){
			return getHeaderRequest(request,null,null);
		}
		public static String getHeaderRequest(String request,String referer,String host)
		{
			StringBuffer header = new StringBuffer();
			if(host != null){
				header.append("GET " + request + " HTTP/1.1" + CRLF);
				header.append("Host: " + host + CRLF);
				header.append("Connection: keep-alive" + CRLF);
			}else{
				header.append("GET http://" +KissMangaAddress + "/" + request + " HTTP/1.0" + CRLF);
				header.append("Host: " + KissMangaAddress + CRLF);
			}
			
			header.append("User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17" +CRLF);
			header.append("Accept:  */*"+ CRLF);
			
			if(referer != null){
				header.append("Referer: http://" + KissMangaAddress + "/" + referer + CRLF);
				header.append("Accept-Encoding: gzip,deflate,sdch" + CRLF);
				header.append("Accept-Language: en-US,en;q=0.8" + CRLF);
				header.append("Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3" + CRLF);
			}else{
				header.append("Accept-Language: en-US,en;q=0.8"+ CRLF);
			}
			
			header.append(CRLF);
			return header.toString();
		}
		
		private String getRespone(String header) throws IOException {
			return getRespone(header,false);
		}
		
		private String getRespone(String header,boolean show) throws IOException{
			StringBuffer response = new StringBuffer();
			
			if(show) ui.Trace(header);
			
			socKissManga.getOutputStream().write(header.getBytes());
			socKissManga.getOutputStream().flush();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(socKissManga.getInputStream()));
			String line = "";
			
			while( (line = reader.readLine()) != null){
				if(show) ui.Trace(line);
				response.append(line + "\n");
			}
			
			
			return response.toString();
		}
		
		
		
		public int parseMangaList(String content){
			int parseMangaCount = 0;
			int index = content.indexOf("<table class=\"listing\">");
			
			if(index > -1){
				content = content.substring(index,content.indexOf("</table"));
				content = content.replace("<tr","ô");
				StringTokenizer token = new StringTokenizer(content,"ô");
				String line = token.nextToken();
				
				while( token.hasMoreTokens() ){
					line = token.nextToken().trim();
					
					if(line.charAt(0) == '>' || line.substring(0, line.indexOf("\n")).equals("class=\"odd\">")){
						String name="",link="",status="";
						
						link = line.substring( line.indexOf("href=") + 6);
						link = link.substring(0,link.indexOf("\"")).trim();
						
						try{
						name = line.substring(line.indexOf("'>")+2, line.indexOf("</a>",line.indexOf("'>")+2)).trim();
						
						
						status = line.substring( line.indexOf("<td>",line.indexOf("</td>"))+4,
												 line.indexOf("</td>", line.indexOf("<td>",line.indexOf("</td>")))).trim();
						}catch(Exception e){ System.out.println(line); }
						
						if(status.contains("<a"))
							status = "ongoing";
						
						mangalist.add(new Manga(name,link,status));
						
						
						parseMangaCount++;
						
					}
				}
				
			}
			
			
			return parseMangaCount;
		}
		
		
		public ArrayList<Manga> getMangaList(){
			return mangalist;
		}
		
		public void syncMangaChapters(Manga manga){
			try{
				connect(true);
				String content = getRespone(getHeaderRequest(manga.getLink()));
				manga.setChapters(parseChapterList(content));
			}catch(Exception e){ e.printStackTrace(); }
		}
		
		private ArrayList<Chapter> parseChapterList(String content){
			ArrayList<Chapter> chapters = new ArrayList<Chapter>();

			
			int index = content.indexOf("<table class=\"listing\">");
			if(index > -1){
				content = content.substring(index,content.indexOf("</table"));
				content = content.replace("<td>","ô");
				StringTokenizer token = new StringTokenizer(content,"ô");
				String line = token.nextToken();
				
				while( token.hasMoreTokens() ){
					line = token.nextToken().trim();
					
					if(line.subSequence(0, 2).equals("<a")){
						
						String name="",link="";
						
						link = line.substring( line.indexOf("href=") + 6);
						link = link.substring(0,link.indexOf("\"")).trim();
						
						name = line.substring( line.indexOf("\">") + 2).trim();
						name = name.substring(0,name.indexOf("</a>")).trim();
						
						chapters.add(new Chapter(name,link));
					}
				}
			}
			
			return chapters;
		}
		
		
		public void downloadMangaChapter(String manga,Chapter chapter){
			try{
				connect(true);
				String content = getRespone(getHeaderRequest(chapter.getLink()));
				parseDownloadableChapterPage(content,new File(manga,chapter.getName()),chapter.getLink());
				
			}catch(Exception e){ e.printStackTrace(); }
		}
		
		private void parseDownloadableChapterPage(String content,File dir,String referer){
			int index = content.indexOf("var lstImages = new Array();");
			
			
			if(index > -1){
				content = content.substring(index,content.indexOf("var currImage = 0;",index)).trim();
				content = content.replace("lstImages.push(\"","ô");
				StringTokenizer token = new StringTokenizer(content,"ô");
				String line = token.nextToken();
				
				while(token.hasMoreTokens()){
					line = token.nextToken();
					line = line.replace("\");","").trim();
					line = line.replace("http://2.bp.blogspot.com", "");
					try{
						connect("2.bp.blogspot.com");
						String imagedata = getRespone(getHeaderRequest(line,referer,"2.bp.blogspot.com"));
						
						StringTokenizer tokenImage = new StringTokenizer(imagedata,"\n");
						String line2 = "";
						String filename = "";
						while( tokenImage.hasMoreTokens()){
							line2 = tokenImage.nextToken();
							if(line2.contains("Content-Disposition: inline;filename=\"")){
								filename = line2.substring(38).trim();
							}
							
							ui.Trace(filename);
							if(line2.trim().equals(""))
								break;
						}
						
					}catch(Exception e){ 
						e.printStackTrace(); 
					}	
				}
			}
		}
}
