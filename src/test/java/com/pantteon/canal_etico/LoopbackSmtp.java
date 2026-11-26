package com.pantteon.canal_etico;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/** Test-only SMTP sink bound to loopback. No relay, credentials or external network. */
final class LoopbackSmtp implements AutoCloseable {
 private final ServerSocket server;
 private final Thread worker;
 private volatile boolean running=true;
 final AtomicBoolean rejectNext=new AtomicBoolean();
 final List<String> messages=new CopyOnWriteArrayList<>();
 final List<Exception> errors=new CopyOnWriteArrayList<>();
 LoopbackSmtp() throws IOException {
  server=new ServerSocket(0,10,InetAddress.getByName("127.0.0.1"));
  worker=new Thread(this::run,"synthetic-smtp-sink");worker.setDaemon(true);worker.start();
 }
 int port(){return server.getLocalPort();}
 private void run(){
  while(running){
   try(var socket=server.accept()){
    socket.setSoTimeout(5000);
    var reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));
    var writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8));
    reply(writer,"220 localhost synthetic SMTP");
    String line;
    while((line=reader.readLine())!=null){
     String command=line.toUpperCase(java.util.Locale.ROOT);
     if(command.startsWith("EHLO")||command.startsWith("HELO"))reply(writer,"250 localhost");
     else if(command.startsWith("MAIL FROM:")||command.startsWith("RCPT TO:")||command.equals("RSET"))reply(writer,"250 OK");
     else if(command.equals("DATA")){
      if(rejectNext.getAndSet(false)){reply(writer,"451 synthetic temporary failure");continue;}
      reply(writer,"354 End with dot");var message=new StringBuilder();
      while((line=reader.readLine())!=null&&!line.equals("."))message.append(line.startsWith("..")?line.substring(1):line).append("\r\n");
      if(line==null)throw new EOFException("Incomplete synthetic message");
      messages.add(message.toString());reply(writer,"250 Accepted by synthetic sink");
     }else if(command.equals("QUIT")){reply(writer,"221 Bye");break;}
     else reply(writer,"250 OK");
    }
   }catch(Exception error){if(running)errors.add(error);}
  }
 }
 private static void reply(BufferedWriter writer,String line) throws IOException {writer.write(line+"\r\n");writer.flush();}
 @Override public void close() throws Exception {running=false;server.close();worker.join(6000);}
}
