/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.biomedis.biomedismair3.utils.Net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Anama
 */
public class SimpleDownloadFile
{
  private final URL url;
  private final File resFile;
  private int size;
  private static final int MAX_BUFFER_SIZE = 1024;
  private final byte[] buff;
  private OutputStream fos;
  private String resString;
  private byte[] resByte;

    /**
     *
     * @param url
     * @param resFile  = если null то данные пойдут в байтовый массив. извлечь можно  getByteData
     */
    public SimpleDownloadFile(URL url, File resFile) 
    {
        this.size = -1;
        this.url = url;
        this.resFile = resFile;
        this.resByte=null;
        this.resString=null;
        buff= new byte[MAX_BUFFER_SIZE];        
       if(resFile!=null) try {fos=new FileOutputStream(resFile);}catch (FileNotFoundException ex) { ex.printStackTrace();}
        else  fos= new ByteArrayOutputStream(1024);
          
        
          
        
    }
  
    /**
     * Вернет Null в случае неудачи
     * @param url
     * @return
     */
    public static HttpURLConnection getConnect(URL url) 
    {
        
     HttpURLConnection cn=null;     
      try {
          cn = (HttpURLConnection) url.openConnection();
      } catch (IOException ex) {}
          return cn; 
       
    }
    public ByteArrayInputStream getInByteStream()
    {
        if(this.resByte==null) return null;
     
      return new ByteArrayInputStream(this.resByte);
      
    }
    public InputStream getInputStream() throws IOException
    {
         HttpURLConnection connection =(HttpURLConnection) url.openConnection();
       connection.connect();
      if (connection.getResponseCode() / 100 != 2){ fos.close();return null;}
      int contentLength = connection.getContentLength();
      if (contentLength < 1) { fos.close();return null;}
      this.size=contentLength;
     return  connection.getInputStream();
    }
    
  public boolean download() throws IOException
  {
       HttpURLConnection connection =(HttpURLConnection) url.openConnection();
       connection.connect();
      if (connection.getResponseCode() / 100 != 2){ fos.close();return false;}
      int contentLength = connection.getContentLength();
      if (contentLength < 1) { fos.close();return false;}
      this.size=contentLength;
      InputStream inputStream = connection.getInputStream();
      int cnt=0;
      while(cnt!=-1)
      {
        
         cnt=  inputStream.read(buff);  
         if(cnt!=-1)  fos.write(buff,0,cnt);
       
         
      }
     if(resFile==null) this.resByte=((ByteArrayOutputStream)fos).toByteArray();
      inputStream.close();
      fos.close();
     
      
      return true;
  }
  
   public String getFileName(URL url)
   {
       String fileName = url.getFile();
       return fileName.substring(fileName.lastIndexOf('/') + 1);
    }
    public int getFileSize(){return this.size;}
    
   public byte[] getByteData()
   {
       return this.resByte;
   }
   public String getStringData()
   {
       resString= this.resByte.toString();
       return resString;
       
   }
    public static  void main(String[] args) 
          {
              SimpleDownloadFile df=null;
      try {
          df=new SimpleDownloadFile(new URL("http://old.advayta.org/_up/021/004/000446.doc"), new File("downloaded.doc"));
      //df=new SimpleDownloadFile(new URL("http://cystatin.ru"), new File("downloaded.html"));
     
          
      } catch (MalformedURLException ex) {
          ex.printStackTrace();
      }
      
      try {
          if(df.download())System.out.println("Скачано");
          else System.err.println("Ошибка соединения");
      } catch (IOException ex) {
         ex.printStackTrace();
          System.err.println("Ошиибка скачки");
      }
      
          }
   
}
