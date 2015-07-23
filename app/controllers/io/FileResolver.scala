package controllers.io

import javax.inject.Inject

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import play.api.libs.json._
import play.api.mvc.{Action, BodyParsers, Controller, Session}
import play.cache._
import scriptable.{Command, LogLine, RunCommand}

import scala.collection.mutable
import scala.reflect.io.File


class LocalActor(cache: CacheApi) extends Actor{
  var remoteActor:ActorSelection = null
  var sessionKey:String = ""
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    /*
      Connect to remote actor. The following are the different parts of actor path

      akka.tcp : enabled-transports  of remote_application.conf

      RemoteSystem : name of the actor system used to create remote actor

      127.0.0.1:5150 : host and port

      user : The actor is user defined

      remote : name of the actor, passed as parameter to system.actorOf call

     */
    remoteActor = context.actorSelection("akka.tcp://RemoteSystem@127.0.0.1:5150/user/receiver")
    println("That 's remote:" + remoteActor)
    //remoteActor ! "hi"
  }
  override def receive: Receive = {
    case msg:String => {
      println("got message from remote" + msg)
    }
    case runCommand:RunCommand =>{
      println("OnStartCommand")
      sessionKey = runCommand.sessionKey
      remoteActor ! Command(runCommand.fileName,runCommand.code)
    }
//    case cmd:Command =>{
//      println("OnCommand")
//      remoteActor ! cmd
//    }
    case line:LogLine =>{
      val logLines:mutable.MutableList[LogLine] = cache.get(sessionKey)
      logLines+=LogLine(line.line,logLines.length+1)
    }
  }
}

class FileResolver @Inject() (cache: CacheApi) extends Controller{

  val actor = ActorSystem("receiver").actorOf(Props(new LocalActor(cache)), "receiver")

  def setSessionCache(session:Session):String ={
    var unique:String = session.get("sessionKey").getOrElse("")
    if( unique.isEmpty ) {
      unique = java.util.UUID.randomUUID().toString()
    }

    var lines:mutable.MutableList[LogLine] = cache.get(unique)
    if( lines == null ){
      lines = new mutable.MutableList[LogLine]
    }
    cache.set(unique,lines, 9999)

    println(unique)
    return unique
  }
  def getSessionLogLines(session:Session):mutable.MutableList[LogLine]={
    val unique:String = session.get("sessionKey").getOrElse("")
    cache.get(unique)
  }

  def index() = Action{ request =>
    val unique:String = setSessionCache(request.session)
    Ok(views.html.index("")).withSession("sessionKey"->unique)
  }

  def getFile(fileName:String) = Action{ request=>
    val unique:String = setSessionCache(request.session)
    val source = scala.io.Source.fromFile("public/code/scriptable/" + fileName)
    val lines = try source.mkString finally source.close()
    Ok(lines).withSession("sessionKey"->unique)
  }

  def execute(fileName:String) = Action{ request =>

    val unique:String = request.session.get("sessionKey").getOrElse("")
    if( unique.isEmpty ){
      RequestTimeout("!!!")
    }
    val logLines = getSessionLogLines(request.session)
    if( logLines == null ){
      setSessionCache(request.session)
    }

    val source = scala.io.Source.fromFile("public/code/scriptable/" + fileName)
    val lines = try source.mkString finally source.close()
    actor ! RunCommand(fileName, lines, unique)
    //actor ! RunCommand(fileName, lines, unique)
    Ok("")
  }

  def save() = Action(BodyParsers.parse.json) { request =>
    val fileName = (request.body \ "fileName").get.as[String]
    val codeChunk = (request.body \ "codeChunk").get.as[String]
    File("public/code/scriptable/"+fileName).writeAll(codeChunk)
    Ok("{}").as("test/json")
  }

  def getLog() = Action(BodyParsers.parse.json) { request =>
    //Lines.seq+=LogLine(Random.alphanumeric.take(Random.nextInt%5+5).mkString,Lines.seq.length+1)
    val logLines = getSessionLogLines(request.session)
    if( logLines == null){
      Ok("{}").as("text/json")
    }
    else{
      val json = Json.toJson(logLines)
      Ok(json)
    }
  }

  def clearLog() = Action { request =>
    //Lines.seq+=LogLine(Random.alphanumeric.take(Random.nextInt%5+5).mkString,Lines.seq.length+1)
    val logLines = getSessionLogLines(request.session)
    logLines.clear()
    Ok("")
  }
  implicit val f = Json.format[LogLine]

}

