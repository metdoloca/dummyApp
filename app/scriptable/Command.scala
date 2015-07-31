package scriptable

case class Command(op:String, fileName:String, code:String, hostIp:String, sessionKey:String)
case class LogLine(line:String,lineNumber:Int,sessionKey:String)
case class RunCommand(fileName:String, code:String, sessionKey:String, hostIp:String)
case class SaveCommand(fileName:String, code:String)
case class KillCommand(hostIp:String,sessionKey:String)
