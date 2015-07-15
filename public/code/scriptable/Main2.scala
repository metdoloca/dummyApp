package scriptable
import codec.{HeaderDefine, Message}
import io.netty.channel.Channel

class Main extends Script{
  val session:Channel=connect("172.16.1.244",9999)
  var recvCount=0
  def defineHeader:HeaderDefine	={
    val hd = new HeaderDefine
    hd.beginDataSizeOffset = 4
    hd.headerSize = 8
    hd.typeOfDataSize = 4
    hd.beginProtocolOffset = 0
    hd.typeOfProtocolSize = 4
    hd.isPacketSizeIncludeHeader = false
    return hd
  }

  def start() = {
    // do something on start script
    println( "script start!")
  }

  override def onConnect(): Unit = {
    println("onConnect")
    for( count<-0 until 5){
      println("write msg")
      val testMsg = Message()
      testMsg.protocolId = 101
      testMsg.writeString("abc")
      session.writeAndFlush(testMsg)
    }
  }

  override def onRead(message:Message): Unit = {
    if( message.protocolId == 102 ) {
      //println(s"onRead = ${message.readString}")
      recvCount+=1
      if(recvCount == 3 || recvCount == 5){
        println(s"onRead = ${message.readString} index = ${recvCount}")
      }
    }
  }
}