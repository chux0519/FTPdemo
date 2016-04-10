package com.example.chuckie.ftpdemo;

/**
 * Created by chuckie on 4/6/16.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;


/**
 *
 * @author chuckie
 */
public class chuxFTPClient {
    private FTPClient ftp= new FTPClient();
    chuxFTPClient(){
    }


    public  boolean tryConnect(String server,int port,String username,String password){
        try {
            ftp.connect(server, port);
            System.out.println("connect to server...");
            String replyString = ftp.getReplyString();
            System.out.println("replyString:"+replyString);

            int replyCode = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                System.out.println("failed to connect");
//                System.exit(1);
                return false;
            }

            System.out.println("connected!");


            ftp.setControlEncoding("UTF-8");
            //Windows主机用GBK

            boolean login = ftp.login(username,password);
            if (login) {
                System.out.println("logged in!");
            } else {
                System.out.println("failed to log in!");
                return false;
            }
        } catch (IOException ex) {
            Logger.getLogger(chuxFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }


    public  List<String> disPlay(String directory) throws IOException{
        FTPListParseEngine engine = ftp.initiateListParsing(directory);
        List<String> folders_ = new ArrayList<>();
        List<String> files_ = new ArrayList<>();

        String tmpName = "";
        while(engine.hasNext()){
            FTPFile[] files = engine.getNext(25);
            for (int i = 0; i < files.length; i++) {
                String getName = files[i].getName();
                if (files[i].isDirectory()) {
//                    System.out.println("/"+getName);
                    tmpName = "/"+getName;
                    folders_.add(tmpName);

                }else{
//                    System.out.println(getName);
                    files_.add(getName);
                }
            }
        }
        List<String> res_ = new ArrayList<String>();
        res_.addAll(folders_);
        res_.addAll(files_);
        return res_;
    }

    public   void upLoad(String dis, String file) throws FileNotFoundException, IOException{
        InputStream in = new FileInputStream(file);
        File tmpFile = new File(file);//获取文件名

        boolean changeDirectory = ftp.changeWorkingDirectory(dis);
        if (changeDirectory) {
            ftp.storeFile(tmpFile.getName(), in);
        } else {
            ftp.makeDirectory(dis);
            if(!ftp.changeWorkingDirectory(dis)){System.out.println("Can't Create new Directory "+dis);}
            else{System.out.println("Create new Directory "+dis);ftp.storeFile(tmpFile.getName(), in);}
        }

        in.close();
    }

    public    void downLoad(String remoteFile, String savePath) throws FileNotFoundException, IOException{
        int start = remoteFile.lastIndexOf("/");
        String localFile = savePath+"/"+remoteFile.substring(start);
        OutputStream out = new FileOutputStream(localFile);
        File tmpFile = new File(localFile);
        boolean getFile = ftp.retrieveFile(remoteFile, out);
        System.out.println("getting file " + remoteFile + " saved as " + localFile);
        if (getFile) {
            System.out.println("Done");
        } else {
            System.out.println("Can't get " + remoteFile);
            tmpFile.delete();
        }
    }

    public   void disConnect() throws IOException{
        boolean logout = ftp.logout();
        if (logout) {
            System.out.println("logged out!");
        }else{
            System.out.println("\n");
        }
        ftp.disconnect();
        System.out.println("disconnected!");
    }

    public String renameFile(String from, String to) throws IOException {
        boolean res = ftp.rename(from,to);
        if(res){String rtr = "Renamed "+from; return rtr;}
        return null;
    }

    public   String delStuff(String stuffName) throws IOException{
        boolean delFile = ftp.deleteFile(stuffName);
        if (delFile) {
            String res = "File " + stuffName + " Deleted";
            System.out.println(res);
            return res;
        }else{

            if(ftp.removeDirectory(stuffName)){
                String res = "Directory " + stuffName + " Deleted";
                System.out.println(res);
                return res;
            }
            else{
                String res = "Can't Delete, Stuff doesn't exists!";
                System.out.println(res);
                return res;

            }

        }
    }

//    public static void main(String[] args) throws FileNotFoundException, IOException{
//        tryConnect("127.0.0.1",2121,"user","12345");
//        upLoad("newFolder", "/home/chuckie/workspace/Java/myFTPClient/src/chuxFTPClient.java");
//        disPlay("");
//        downLoad("chuxFTPClient.java", "/home/chuckie/workspace/hello.java");
//        delStuff("newFolder");
//        disConnect();
//    }
}

