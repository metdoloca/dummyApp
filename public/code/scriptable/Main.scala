package scriptable
import codec.{HeaderDefine, Message}
import io.netty.channel.Channel

class Main extends Script{
	//var session:Channel=null
	var recvCount=0
  
   	// set server port
    def getPort:Int = 9999
  	// set server addr
    def getHost:String = "172.16.1.244"
  
	def defineHeader:HeaderDefine = {
		val hd = new HeaderDefine
		hd.headerSize = 8
        hd.beginDataSizeOffset = 4
		hd.typeOfDataSize = 4
		hd.beginProtocolOffset = 0
		hd.typeOfProtocolSize = 4
		hd.isPacketSizeIncludeHeader = false
		return hd
	}

  	def onStart = {
		// do something on start script
	}
  
    def onConnected(channel:Channel) = {
      // do something in connected
      writeConsole("onConnect")
      if( channel == null ){
        writeConsole("channel is null")
      }
      else{
        for( count<-0 until 100){
          val testMsg = Message()
          testMsg.protocolId = 101
          testMsg.writeString("abc")
          channel.writeAndFlush(testMsg)
        }
      }
    }
  
  	var tickCount:Int = 0
   	// if return false => tick terminated
    def tick() : Boolean = {
      tickCount+=1
      if( tickCount < 5 ){
      	writeConsole(s"onTick=${tickCount}")
        return true
      }
      return false
    }

	override def onRead(message:Message) = {
		if( message.protocolId == 102 ) {
			//println(s"onRead = ${message.readString}")
			recvCount+=1
            val tempString = message.readString
			writeConsole(s"onRead = ${tempString} index = ${recvCount}")
		}
	}
  	
  	override def onDisconnect = {
      writeConsole("onDisconnect")
    }
  
  	override def onException(cause:Throwable) = {
      writeConsole(cause.toString)  
    }
  
}