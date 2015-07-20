package scriptable
import codec.{HeaderDefine, Message}
import io.netty.channel.Channel

class Main extends Script{
	var session:Channel=null
	var recvCount=0
  
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
      	session = connect("172.16.1.244",9999)
		if( session == null ){
            writeConsole("connect fail")
        }
        else{
          for( count<-0 until 5){
              println("write msg")
              val testMsg = Message()
              testMsg.protocolId = 101
              testMsg.writeString("abc")
              session.writeAndFlush(testMsg)
          }
        }
	}

	override def onRead(message:Message) = {
		if( message.protocolId == 102 ) {
			//println(s"onRead = ${message.readString}")
			recvCount+=1
			if(recvCount == 3 || recvCount == 5){
				writeConsole(s"onRead = ${message.readString} index = ${recvCount}")
			}
          	if(recvCount == 5){
				//session.disconnect()    
                //writeConsole("onClose")
            }
          
			writeConsole(s"${recvCount}")
		}
	}
  	
  	override def onDisconnect = {
      writeConsole("onDisconnect")
    }
  
  	override def onException(cause:Throwable) = {
      writeConsole(cause.toString)  
    }
  
}